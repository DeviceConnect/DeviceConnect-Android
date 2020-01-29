package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;


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

	public void generatePackets(final ByteBuffer buffer) {
		List<Integer> offsets = ByteUtil.kmp(buffer, START_CODE);
		buffer.position(0);
		int totalLength = buffer.remaining();
		for (int i = 0; i < offsets.size(); i++) {
			final int unitOffset = offsets.get(i);
			final int unitLength;
			if (i == offsets.size() - 1) {
				unitLength = totalLength - unitOffset;
			} else {
				int nextOffset = offsets.get(i + 1);
				unitLength = nextOffset - unitOffset;
			}
			int type = buffer.get(unitOffset + START_CODE.length) & 0x1F;

			boolean isFrame = type == H264NT_SLICE || type == H264NT_SLICE_IDR;
			long pts = getPts();
			buffer.position(unitOffset);
			tsWriter.writeVideoBuffer(isFirstPes, buffer, unitLength, pts, pts, isFrame);
			isFirstPes = false;
		}
	}
	
	@Override
	public void close() {
	}

	
	private long getPts() {
		return pts += ptsIncPerFrame;
	}
	
	private long getDts() {
		return dts += ptsIncPerFrame;
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
