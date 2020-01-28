package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.deviceconnect.android.libmedia.BuildConfig.DEBUG;


public class H264TsSegmenter extends AbstractTsSegmenter {
	
	//NAL类型
	private static final int H264NT_SLICE  = 1;
	private static final int H264NT_SLICE_IDR = 5;
	private static final int H264NT_SPS = 7;	//SPS类型值
	private static final int H264NT_PPS = 8;	//PPS类型值
	private static final int H264NT_UNUSED_TYPE = 0; //未使用的类型
	
	//帧类型  
	private static final int FRAME_I    = 15;
	private static final int FRAME_P    = 16;  
	private static final int FRAME_B    = 17;
	private static final int UNSUPPORT  = -1;

	//由于视频数据可能大于65535，所以TsEncode时设置pes_packet_length = 0x0000(仅视频)
	private static final int TWO_FRAME_MAX_SIZE = 65535 * 2;
	
	//如果NALU对应的Slice为一帧的开始，则用4字节表示，即0x00000001；否则用3字节表示，0x000001
	private static final byte[] NAL_DELIMITER = {0x00, 0x00, 0x01};

	private static final byte[] START_CODE = {0x00, 0x00, 0x00, 0x01};

	private boolean isFirstPes = true;
	private boolean waitingIDRFrame = true;				//等待关键帧
	private int currentNalType = 0;						//当前编码的帧类型（只记录I/B/P帧）
	private int numInGop = 0; 							//当前帧组中的第numInGop帧
	
	private RingBuffer framesBuf;						//用于缓存视频流过来的原始数据，可能存在多个帧
	
	private ArrayDeque<AvcFrame> avcFrameCache = new ArrayDeque<AvcFrame>();
	private List<AvcFrame> cacheAvcFrames = new ArrayList<AvcFrame>();
	
	private TsPacketWriter tsWriter;

	public interface BufferListener {
		void onBufferAvailable(byte[] buffer);
	}
	private BufferListener bufferListener;
	
	public H264TsSegmenter() {
		super();
		
		frameNum = (int) (TS_DURATION * this.fps -1);
		ptsIncPerFrame = (long) (1000 / this.fps) * 90;
		pts += ptsIncPerFrame;
		dts = pts - 200;
		tsSegTime = frameNum * ptsIncPerFrame / 1000F;	//默认值
		
		tsWriter = new TsPacketWriter();
		tsWriter.setCallback(new TsPacketWriter.Callback() {
			@Override
			public void onPacket(final byte[] packet) {
				bufferListener.onBufferAvailable(packet);
			}
		});
		
		framesBuf = new RingBuffer(TWO_FRAME_MAX_SIZE);
		
		prepare4NextTs();
	}

	public void setBufferListener(final BufferListener bufferListener) {
		this.bufferListener = bufferListener;
	}

	@Override
	public void initialize(float sampleRate, int sampleSizeInBits, int channels, int fps) {
		this.fps = fps;
		frameNum = (TS_DURATION * this.fps -1);
		ptsIncPerFrame = (long) (1000 / this.fps) * 90;
		pts += ptsIncPerFrame;
		dts = pts - 200;
		tsSegTime = frameNum * ptsIncPerFrame / 1000F;	//默认值
	}
	
	public void resetPts(long pts) {
		this.pts = pts;
		this.ptsBase = 0L;
	}

	public void prepare4NextTs() {
		numInGop = 0;
		tsWriter.reset();
		avcFrameCache.clear();
	}

	public void generatePackets(final ByteBuffer buffer, final long defaultPts) {
		List<Integer> offsets = ByteUtil.kmp(buffer, START_CODE);
		buffer.position(0);
		int totalLength = buffer.remaining();
		List<NALUnit> units = new ArrayList<>();
		for (int i = 0; i < offsets.size(); i++) {
			int unitOffset = offsets.get(i);
			NALUnit unit = new NALUnit();
			unit.offset = unitOffset;
			if (i == offsets.size() - 1) {
				unit.length = totalLength - unitOffset;
			} else {
				int nextOffset = offsets.get(i + 1);
				unit.length = nextOffset - unitOffset;
			}
			unit.type = buffer.get(unitOffset + START_CODE.length) & 0x1F;
			units.add(unit);
		}

		for (NALUnit unit : units) {
			boolean isFrame = unit.type == H264NT_SLICE || unit.type == H264NT_SLICE_IDR;
			long pts = isFrame ? defaultPts /*getPts()*/ : -1;
			long dts = pts;
			buffer.position(unit.offset);
			tsWriter.writeVideoBuffer(isFirstPes, buffer, unit.length, pts, dts);
			isFirstPes = false;
		}
	}

	private static class NALUnit {
		private int offset;
		private int length;
		private int type;
	}

	public byte[] segment(ByteBuffer buffer, long pts) {

		boolean isNalDelimiter4 = false;
		byte nextNalType;
		
		int seekPos = framesBuf.size() >= NAL_DELIMITER.length ? framesBuf.size() - NAL_DELIMITER.length : 0;
		
		framesBuf.add(buffer);
		byte[] src = framesBuf.elements(seekPos, framesBuf.size() - seekPos);

		//NALセパレーターの位置
		List<Integer> delimiters = ByteUtil.kmp(src, NAL_DELIMITER);

		if (DEBUG) {
			StringBuilder sb = new StringBuilder();
			for (Integer delimiter : delimiters) {
				sb.append(delimiter);
				sb.append(", ");
			}
			//Log.d("AAA", "delimiters: " + sb.toString() + " src: " + src.length);
		}
		
		for (int i = 0; i < delimiters.size(); i++) {
			
			//次のフレームのフレームタイプを取得
			if (delimiters.get(i) + NAL_DELIMITER.length < src.length) {
				nextNalType = src[delimiters.get(i) + NAL_DELIMITER.length];
			} else {
				break;
			}

			//現在のフルフレームの終了位置を取得
			int endPos = i==0 ? seekPos + delimiters.get(0) : delimiters.get(i) - delimiters.get(i-1) + (isNalDelimiter4 ? 1: 0);
			
			//セパレーターが 0x00 00 00 01 であるかどうかを決定
			isNalDelimiter4 = ( endPos != 0 && i== 0 && delimiters.get(i) != 0 && src[delimiters.get(i)-1] == 0x00 );
			endPos = isNalDelimiter4 ? endPos - 1 : endPos;
			
			if (waitingIDRFrame) {
				//次のフレームがIDRかどうかを判別
				if (isIDRFrame(nextNalType)) {
					waitingIDRFrame = false;
				} else {
					//Log.d("AAA", "Remove incomplete frame data.");
					//IDRフレームの前に不完全なフレームデータを削除する
					framesBuf.remove(0, endPos);
					continue;
				}
			}

			if (currentNalType == H264NT_SLICE || currentNalType == H264NT_SLICE_IDR) {
				
				//完全なフレームデータを取得する（IDRフレームと、IDRフレームの前のSPS + PPSが全体である場合）
				byte[] avcBuf = framesBuf.remove(0, endPos);
				
				if (avcBuf.length > NAL_DELIMITER.length) {
					boolean isLastFrame = (nextNalType & 0x1F) == H264NT_SPS;
					
					int frameType = H264NalUtil.getPesFrameType(avcBuf);
					List<AvcFrame> encodeAvcFrames = getEncodeAvcFrames( new AvcFrame(avcBuf, frameType, pts, -1), isLastFrame);
					numInGop++;
					for (AvcFrame avcFrame : encodeAvcFrames) {
						tsWriter.writeH264(isFirstPes, avcFrame.payload, avcFrame.payload.length, avcFrame.pts, avcFrame.dts);
						isFirstPes = false;
					}
					
					tsSegTime = numInGop / this.fps;
					if (isLastFrame && tsSegTime >= 10F) {
						waitingIDRFrame = true;
						isFirstPes = true;
						tsSegTime = numInGop / this.fps;
						prepare4NextTs();
					}
				}
				
			}

			//更新currentNalType
			if (((nextNalType & 0x1F) == H264NT_SLICE_IDR) || ((nextNalType & 0x1F) == H264NT_SLICE)) {
				currentNalType = nextNalType & 0x1F;
			}
			
			if (waitingIDRFrame) {
				currentNalType = 0;
			}
		}
		
		return null;
	}
	
	//mixed itf
	public AvcResult process(byte[] rawData) {

		boolean isNalDelimiter4 = false;
		byte nextNalType = H264NT_UNUSED_TYPE;
		
		int seekPos =  framesBuf.size() >= NAL_DELIMITER.length ? framesBuf.size() - NAL_DELIMITER.length : 0;
		
		framesBuf.add(rawData);
		byte[] src = framesBuf.elements(seekPos, framesBuf.size() - seekPos);
		
		List<Integer> delimiters = ByteUtil.kmp(src, NAL_DELIMITER);
		List<AvcFrame> encodeAvcFrames = new ArrayList<AvcFrame>();
		List<AvcFrame> endAvcFrames = new ArrayList<AvcFrame>();
		encodeAvcFrames.addAll(cacheAvcFrames);
		cacheAvcFrames.clear();
		boolean isTailAvc = false;
		
		for(int i = 0; i < delimiters.size(); i++) {
			if(delimiters.get(i) + NAL_DELIMITER.length < src.length)
				nextNalType = src[delimiters.get(i) + NAL_DELIMITER.length];
			else 
				break;
			
			int endPos = i==0 ? seekPos + delimiters.get(0) : delimiters.get(i) - delimiters.get(i-1) + (isNalDelimiter4 ? 1: 0);
			
			isNalDelimiter4 = ( endPos != 0 && i== 0 && delimiters.get(i) != 0 && src[delimiters.get(i)-1] == 0x00 );
			endPos = isNalDelimiter4 ? endPos -1 : endPos;
			
			if(waitingIDRFrame) {
				if(isIDRFrame(nextNalType))
					waitingIDRFrame = false;
				else {
					framesBuf.remove(0, endPos);
					continue;
				}
			}
			
			if(currentNalType == H264NT_SLICE || currentNalType == H264NT_SLICE_IDR) {
				
				byte[] avcBuf = framesBuf.remove(0, endPos);
				
				if(avcBuf != null && avcBuf.length > NAL_DELIMITER.length) {
					
					boolean isLastFrame = (nextNalType & 0x1F) == H264NT_SPS;
					int frameType = H264NalUtil.getPesFrameType(avcBuf);
					
					encodeAvcFrames.addAll(getEncodeAvcFrames( new AvcFrame(avcBuf, frameType, -1, -1), isLastFrame));
					if(!isTailAvc && isLastFrame) {
						endAvcFrames.addAll(encodeAvcFrames);
						encodeAvcFrames.clear();
						isTailAvc = true;
						waitingIDRFrame = true;
					}
					
				}
			}
			if (((nextNalType & 0x1F) == H264NT_SLICE_IDR) || ((nextNalType & 0x1F) == H264NT_SLICE)) {
				currentNalType = nextNalType & 0x1F;
			}
			
			if (waitingIDRFrame)
				currentNalType = 0;
		}
		return endAvcFrames.isEmpty() && encodeAvcFrames.isEmpty() ? null : new AvcResult(isTailAvc ? endAvcFrames : encodeAvcFrames, isTailAvc);
	}
	
	@Override
	public void close() {
	}
	
	private List<AvcFrame> getEncodeAvcFrames(AvcFrame avcFrame, boolean isLastFrame) {
		List<AvcFrame> avcFrames = new ArrayList<AvcFrame>();
		
		switch(avcFrame.frameType) {
		case FRAME_I:
		case FRAME_P:
		case UNSUPPORT:
			if(!avcFrameCache.isEmpty()) {
				AvcFrame avcFrame2 = avcFrameCache.pop();
				avcFrame2.pts = getPts();
				avcFrame2.dts = avcFrame2.pts;
				avcFrames.add(avcFrame2);
				while(!avcFrameCache.isEmpty())
					avcFrames.add(avcFrameCache.pop());
			}
			break;
			
		case FRAME_B:
			avcFrame.pts = getPts();
			avcFrame.dts = avcFrame.pts;
			break;
		}
		
		avcFrameCache.offer(avcFrame);
		
		if(isLastFrame) {
			
			AvcFrame avcFrame2 = avcFrameCache.pop();
			avcFrame2.pts = getPts();
			avcFrame2.dts = avcFrame2.pts;
			avcFrames.add(avcFrame2);
			while(!avcFrameCache.isEmpty())
				avcFrames.add(avcFrameCache.pop());
		}
		
		return avcFrames;
	}
	
	private long getPts() {
		return pts += ptsIncPerFrame;
	}
	
	private long getDts() {
		return dts += ptsIncPerFrame;
	}
	
	private boolean isIDRFrame(byte nalType) {
		return (nalType & 0x1F) == H264NT_SPS ||(nalType & 0x1F) == H264NT_PPS || (nalType & 0x1F) == H264NT_SLICE_IDR;
	}
	
	static class AvcResult {
		
		List<TsPacketWriter.FrameData> avcFrames = new ArrayList<TsPacketWriter.FrameData>();
		boolean isTailAvc;
		
		AvcResult(List<AvcFrame> avcFrames, boolean isTailAvc) {
			for(AvcFrame frame : avcFrames) {
				TsPacketWriter.FrameData frameData = new TsPacketWriter.FrameData();
				frameData.buf = frame.payload;
				frameData.pts = frame.pts;
				frameData.dts = frame.dts;
				frameData.isAudio = false;
				this.avcFrames.add(frameData);
			}
			this.isTailAvc = isTailAvc;
		}
	}
	
	static class AvcFrame {
		
		byte[] payload;
		int frameType;
		long pts = -1;
		long dts = -1;
		
		AvcFrame(byte[] payload, int frameType, long pts, long dts) {
			this.payload = payload;
			this.frameType = frameType;
			this.pts = pts;
			this.dts = dts;
		}
	}
	
    static class RingBuffer {
    	private static final int capaStepLen = 5000; //扩容步长 
    	private int capacity = 0;
    	private int headPtr = 0;
    	private int tailPtr = 0;
    	
    	private byte[] elementData;
    	
    	RingBuffer(int capacity) {
    		this.capacity = capacity;
    		elementData = new byte[capacity];
    		for(int i = 0; i<capacity; i++) {
    			elementData[i] = (byte)0xFF;
    		}
    	}
    	
    	int size() {
    		return (tailPtr - headPtr + capacity) % capacity;
    	}
    	
    	boolean isEmpty() {
            return size() == 0;  
        } 
    	
        boolean isFull() {
    		return (tailPtr + 1) % capacity == headPtr;  
    	}
    	
    	private void add(byte element) {
    		if(isFull())
    			expandCapacity();

    		elementData[tailPtr] = element;
    		tailPtr = (tailPtr + 1) % capacity;
    	}
    	
    	private byte remove() {
    		if(isEmpty())
    			throw new NoSuchElementException("The buffer is already empty");
    		byte element = elementData[headPtr];
    		headPtr = (headPtr + 1) % capacity;
    		return element;
    	}
    	
    	void add(byte[] elements) {
    		for(byte element : elements) {
    			add(element);
    		}
    	}

    	void add(ByteBuffer buffer) {
    		while (buffer.remaining() > 0) {
    			add(buffer.get());
			}
		}
    	
    	byte[] remove(int len) {
    		if(len > size())
    			return null;
    		
    		byte[] elements = new byte[len];
    		for(int i =0; i< len; i++) {
    			byte element = remove();
    			elements[i] = element;
    		}
    		return elements;
    	}
    	
    	//[0-from)的数据丢弃，再移除len位数据并返回,  from从0计数
		byte[] remove(int from, int len) {
    		if(from > size())
    			return null;
    		
			headPtr = (headPtr + from) % capacity;
			return remove(len);
    	}
    	
    	public byte[] elements() {
    		byte[] elements = new byte[size()];
    		for(int i=0; i<size(); i++) {
    			elements[i] = elementData[(headPtr + i) % capacity];
    		}
    		return elements;
    	}
    	
    	//from从0计算
		byte[] elements(int from, int len) {
    		if(from + len > size())
    			return null;
    		byte[] elements = new byte[len];
    		for(int i=0; i<len; i++) {
    			elements[i] = elementData[(headPtr + from + i) % capacity];
    		}
    		return elements;
    	}
    	
    	//pos从0计算
    	public byte get(int pos) {
    		return elementData[(headPtr + pos) % capacity];
    	}
    	
    	private void expandCapacity() {
    		byte[] copy = Arrays.copyOf(elementData, capacity+capaStepLen);
    		if(tailPtr < headPtr) {
    			System.arraycopy(elementData, headPtr, copy, headPtr + capaStepLen, capacity - headPtr);
    			headPtr += capaStepLen;
    		}
    		capacity += capaStepLen;
    		elementData = copy;
    	}
    	
    }
	
}
