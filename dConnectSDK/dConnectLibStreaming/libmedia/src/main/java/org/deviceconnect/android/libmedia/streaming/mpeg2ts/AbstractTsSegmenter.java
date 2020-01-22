package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

/**
	显示时间戳。音频可以设置为0； 当视频中包含B帧时，dts应小于pts，故此处pts初始化为1
	在ISO/IEC13818-1中制定90k Hz 的时钟，如果编码帧频是30，那么时间戳间隔就该是90000 / 30 = 3000

	视频：pts = inc++ *(1000/fps);  其中inc是一个静态的，初始值为0，每次打完时间戳inc加1
	音频：pts = inc++ * (frame_size * 1000 / sample_rate)

 */
public abstract class AbstractTsSegmenter {
	
	// TS段的持续时间, 秒
	public static final int TS_DURATION = 5;        
	
	// video
	public int fps; 				//帧数
	
	// audio
	public float sampleRate;		//采样率
	public int sampleSizeInBits;	//单样本位数
	public int channels; 			//声道数
	

	// pts & dts
	protected long pts = 1L;
	protected long ptsBase = 0L;

	protected long dts = 0L;
	
	/* 
		单个ts时间段包含的帧数。
		音频: 指定frameNum = ts_duration * sampleRate >> 10;
		视频：frameNum = ts_duration * fps;
	 */
	protected int frameNum = 0;	 

	/* 
	 	pts的步长
	 	音频：一个AAC帧对应的采样样本的个数/采样频率（单位s）
	 	视频：1000/fps(单位ms)
	 	pts、dts与毫秒的转化：按照h264的设定是90HZ, 所以PTS/DTS到毫秒的转换公式是：ms=pts/90
	 */
	protected long ptsIncPerFrame = 0;
	
	//媒体段的持续时间
	protected float tsSegTime = 0F;
	
	public AbstractTsSegmenter() {
		
		this.sampleRate = 8000F;
		this.sampleSizeInBits = 16;
		this.channels = 1;
		
		this.fps = 25;
	}
	
	public int getFrameNum() {
		return frameNum;
	}
	
	public float getTsSegTime() {
		return tsSegTime;
	}
	
	public long getPtsIncPerFrame() {
		return ptsIncPerFrame;
	}
	
    public int calcTsNum (int length) {
        int rawFrameLen = (length >> 11) + ((length & 0x7FF) == 0 ? 0 : 1);
        return rawFrameLen / frameNum + (rawFrameLen % frameNum == 0 ? 0 : 1);
    }
    
    public float calcTsSegTime(float sampleRate) {
    	int rawFramenum = (TS_DURATION * (int)sampleRate) >> 10;
    	return 1.0F * (rawFramenum << 10) / sampleRate;
    }

	public void initialize(float sampleRate, int sampleSizeInBits, int channels, int fps) {
		
		this.sampleRate = sampleRate;
		this.sampleSizeInBits = sampleSizeInBits;
		this.channels = channels;
		
		this.fps = fps;
	}
	
	public abstract void close();
	
	public abstract void prepare4NextTs();
	
}