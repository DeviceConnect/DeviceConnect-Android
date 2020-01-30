package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import android.util.Log;

import com.google.common.primitives.Bytes;

import java.nio.ByteBuffer;

import org.deviceconnect.android.libmedia.streaming.mpeg2ts.util.TsUtil;

import static org.deviceconnect.android.libmedia.BuildConfig.DEBUG;

public class TsPacketWriter {
	
	// Transport Stream packets are 188 bytes in length
	public static final int TS_PACKET_SIZE 				= 188;	
	public static final int TS_HEADER_SIZE				= 4;
	public static final int TS_PAYLOAD_SIZE 			= TS_PACKET_SIZE - TS_HEADER_SIZE;
	
	// Table 2-29 – Stream type assignments. page 66
	public static final byte STREAM_TYPE_AUDIO_AAC 		= 0x0f;
	public static final byte STREAM_TYPE_AUDIO_MP3 		= 0x03;
	public static final byte STREAM_TYPE_VIDEO_H264 	= 0x1b;


	public static final int TS_PAT_PID 					= 0x0000;	// 0
	public static final int TS_PMT_PID 					= 0x1000;	// 4096
	public static final int TS_AUDIO_PID 				= 0x101;	// 257
	public static final int TS_VIDEO_PID 				= 0x100;	// 256
	
	// Transport Stream Description Table
	public static final int TS_PAT_TABLE_ID 			= 0x00;
	public static final int TS_PMT_TABLE_ID 			= 0x02;

	
	// H264 Nalu
	public static byte[] H264_NAL = { 0x00, 0x00, 0x00, 0x01, 0x09, (byte)0xf0 };
	
	// cC
	private byte mAudioContinuityCounter = 0;
	private byte mVideoContinuityCounter = 0;
	private int mPatContinuityCounter = 0;
	private int mPmtContinuityCounter = 0;

	private Packet mPacket = new Packet();

	private static class Packet {
		private final byte[] mData = new byte[TS_PACKET_SIZE];
		private int mOffset = 0;

		void add(byte b) {
			mData[mOffset++] = b;
		}

		void add(ByteBuffer buffer, int len) {
			for (int i = 0; i < len; i++) {
				add(buffer.get());
			}
		}

		void reset(final byte b) {
			for (int i = 0; i < TS_PACKET_SIZE; i++) {
				mData[i] = b;
			}
			mOffset = 0;
		}
	}

	public interface Callback {
		void onPacket(final byte[] packet);
	}

	private Callback mCallback;

	public void setCallback(final Callback callback) {
		mCallback = callback;
	}

	private void writePacket(byte b) {
		mPacket.add(b);
	}

	private void writePacket(ByteBuffer buffer, int len) {
		mPacket.add(buffer, len);
	}

	private void writePacket(byte[] buffer, int offset, int length) {
		for (int i = 0; i < length; i++) {
			mPacket.add(buffer[offset + i]);
		}
	}

	private void resetPacket(final byte b) {
		mPacket.reset(b);
	}

	private void notifyPacket() {
		if (mCallback != null) {
			mCallback.onPacket(mPacket.mData);
		}
	}

	private void write_ts_header(int pid, int continuity_counter) {
	 	byte sync_byte = 0x47;
	 	int transport_error_indicator = 0;
	 	int payload_unit_start_indicator = 1;
	 	int transport_priority = 0;
	 	int transport_scrambling_control = 0;
	 	int adaptation_field_control = 1;
		    
	    writePacket(sync_byte);
		writePacket((byte) ((transport_error_indicator << 7) | (payload_unit_start_indicator << 6) | (transport_priority << 5) | ((pid >> 8) & 0x1F)));
		writePacket((byte) (pid & 0xff));
		writePacket((byte) ((transport_scrambling_control << 6) | (adaptation_field_control << 4) | (continuity_counter & 0x0F)));
		writePacket((byte) 0x00);	//起始指示符
	}

	private void write_pat() {
		resetPacket((byte) 0xFF);
		
		// header
		write_ts_header(TS_PAT_PID, mPatContinuityCounter);
		mPatContinuityCounter = (mPatContinuityCounter + 1) & 0x0F; //包递增计数器(0-15)
		
		// PAT body
		int section_syntax_indicator = 1;
	    int zero = 0;
	    int reserved_1 = 3;
	    int section_length = 13; 
	    int transport_stream_id = 1;
	    int reserved_2 = 3;
	    int version_number = 0;
	    int current_next_indicator = 1;
	    int section_number = 0;
	    int last_section_number = 0;
	    int program_number = 1;
	    int reserved_3 = 7;
	    int program_id = TS_PMT_PID;
	    
	    writePacket((byte) TS_PAT_TABLE_ID);
	    writePacket((byte) ((section_syntax_indicator << 7) | (zero << 6) | (reserved_1 << 4) | ((section_length >> 8) & 0x0F)));
	    writePacket((byte) (section_length & 0xFF));
	    writePacket((byte) ((transport_stream_id >> 8) & 0xFF));
	    writePacket((byte) (transport_stream_id & 0xFF));
	    writePacket((byte) ((reserved_2 << 6) | (version_number << 1) | (current_next_indicator & 0x01)));
	    writePacket((byte) (section_number & 0xFF));
	    writePacket((byte) (last_section_number & 0xFF));
	    writePacket((byte) ((program_number >> 8) & 0xFF));
	    
	    writePacket((byte) (program_number & 0xFF));
	    writePacket((byte) ((reserved_3 << 5) | ((program_id >> 8) & 0x1F)));
	    writePacket((byte) (program_id & 0xFF));
	    
	    // set crc32
	    long crc = TsUtil.mpegts_crc32(mPacket.mData, 5, 12);
	    writePacket((byte) ((crc >> 24) & 0xFF));
	    writePacket((byte) ((crc >> 16) & 0xFF));
	    writePacket((byte) ((crc >> 8) & 0xFF));
	    writePacket((byte) ((crc) & 0xFF));
	}
	
	
	/*
	  only audio , section_length = 18 
	  audio & video mix, section_length = 23
	 */
	private void write_pmt(FrameDataType fType) {
		resetPacket((byte) 0xFF);
		
		// header
		write_ts_header(TS_PMT_PID, mPmtContinuityCounter );
		mPmtContinuityCounter = (mPmtContinuityCounter + 1) & 0x0F; //包递增计数器(0-15)
		
		 // PMT body
	    int table_id = TS_PMT_TABLE_ID;
	    int section_syntax_indicator = 1;
	    int zero = 0;
	    int reserved_1 = 3;
	    int section_length = (fType == FrameDataType.MIXED) ? 23 : 18;
	    int program_number = 1;
	    int reserved_2 = 3;
	    int version_number = 0;
	    int current_next_indicator = 1;
	    int section_number = 0;
	    int last_section_number = 0;
	    int reserved_3 = 7;
	    int pcr_pid = (fType == FrameDataType.AUDIO) ? TS_AUDIO_PID : TS_VIDEO_PID;	
	    int reserved_4 = 15;
	    int program_info_length = 0;
	    
	    writePacket((byte) table_id);
	    writePacket((byte) ((section_syntax_indicator << 7) | (zero << 6) | (reserved_1 << 4) | ((section_length >> 8) & 0x0F)));
	    writePacket((byte) (section_length & 0xFF));
	    writePacket((byte) ((program_number >> 8) & 0xFF));
	    writePacket((byte) (program_number & 0xFF));
	    writePacket((byte) ((reserved_2 << 6) | (version_number << 1) | (current_next_indicator & 0x01)));
	    writePacket((byte) section_number);
	    writePacket((byte) last_section_number);
	    writePacket((byte) ((reserved_3 << 5) | ((pcr_pid >> 8) & 0xFF)));
	    writePacket((byte) (pcr_pid & 0xFF));
	    writePacket((byte) ((reserved_4 << 4) | ((program_info_length >> 8) & 0xFF)));
	    writePacket((byte) (program_info_length & 0xFF));
	    
	    
	    // set video stream info
	    if ( fType == FrameDataType.VIDEO || fType == FrameDataType.MIXED ) {
	    	 int stream_type = 0x1b;
	    	 int reserved_5 = 7;
	    	 int elementary_pid = TS_VIDEO_PID;
	    	 int reserved_6 = 15;
	    	 int ES_info_length = 0;

	    	 writePacket((byte) stream_type);
	         writePacket((byte) ((reserved_5 << 5) | ((elementary_pid >> 8) & 0x1F)));
	         writePacket((byte) (elementary_pid & 0xFF));
	         writePacket((byte) ((reserved_6 << 4) | ((ES_info_length >> 4) & 0x0F)));
	         writePacket((byte) (ES_info_length & 0xFF));
	    }
	    
	    
	    // set audio stream info
	    if ( fType == FrameDataType.AUDIO || fType == FrameDataType.MIXED ) {
	    	
			int stream_type = 0x0f;
			int reserved_5 = 7;
			int elementary_pid = TS_AUDIO_PID;
			int reserved_6 = 15;
			int ES_info_length = 0;

			writePacket((byte) stream_type);
			writePacket((byte) ((reserved_5 << 5) | ((elementary_pid >> 8) & 0x1F)));
			writePacket((byte) (elementary_pid & 0xFF));
			writePacket((byte) ((reserved_6 << 4) | ((ES_info_length >> 4) & 0x0F)));
			writePacket((byte) (ES_info_length & 0xFF));
	    }
	    
	    
	    // set crc32
	    long crc =  TsUtil.mpegts_crc32(mPacket.mData, 5,  (fType == FrameDataType.MIXED) ? 22: 17);
	    writePacket((byte) ((crc >> 24) & 0xFF));
	    writePacket((byte) ((crc >> 16) & 0xFF));
	    writePacket((byte) ((crc >> 8) & 0xFF));
	    writePacket((byte) ((crc) & 0xFF));
	}	
	
	
	/**
	 * write a PTS or DTS
	 * 
	 * @see //https://github.com/kynesim/tstools/blob/master/ts.c
	 * @see //https://www.ffmpeg.org/doxygen/0.6/mpegtsenc_8c-source.html
	 */
	private void write_pts_dts(int guard_bits, long value) {
		int pts1 = (int) ((value >> 30) & 0x07);
		int pts2 = (int) ((value >> 15) & 0x7FFF);
		int pts3 = (int) (value & 0x7FFF);

		writePacket((byte) ((guard_bits << 4) | (pts1 << 1) | 0x01));
		writePacket((byte) ((pts2  & 0x7F80) >> 7));
		writePacket((byte) (((pts2 & 0x007F) << 1) | 0x01));
		writePacket((byte) ((pts3  & 0x7F80) >> 7));
		writePacket((byte) (((pts3 & 0x007F) << 1) | 0x01));
	}

	void writeVideoBuffer(boolean isFirstPes, ByteBuffer buffer, int length, long pts, long dts, boolean isFrame) {

		FrameDataType frameDataType = FrameDataType.VIDEO;

		// write pat table
		write_pat();
		notifyPacket();

		// write pmt table
		write_pmt( frameDataType );
		notifyPacket();

		boolean isFirstTs = true;
		boolean isAudio = false;
		byte[] frameBuf = new byte[length];
		buffer.get(frameBuf);
		int frameBufSize = frameBuf.length;
		int frameBufPtr = 0;
		int pid = isAudio ? TS_AUDIO_PID : TS_VIDEO_PID;

		while (frameBufPtr < frameBufSize) {
			int frameBufRemaining = frameBufSize - frameBufPtr;
			boolean isAdaptationField = (isFirstTs || ( frameBufRemaining < TS_PAYLOAD_SIZE ));

			resetPacket((byte) 0x00);

			// write ts header
			writePacket((byte) 0x47); // sync_byte
			writePacket((byte) ((isFirstTs ? 0x40 : 0x00) | ((pid >> 8) & 0x1f)));
			writePacket((byte) (pid & 0xff));
			writePacket((byte) ((isAdaptationField ? 0x30 : 0x10) | ((isAudio ? mAudioContinuityCounter++ : mVideoContinuityCounter++) & 0xF)));

			if (isFirstTs) {
				if (isFrame) {
					writePacket((byte) 0x07); // adaptation_field_length
					writePacket((byte) (isFirstPes ? 0x50 : (isAudio && frameDataType == FrameDataType.MIXED ? 0x50 : 0x10)));
					// flag bits 0001 0000 , 0x10
					// flag bits 0101 0000 , 0x50

					/* write PCR */
					long pcr = pts;
					writePacket((byte) ((pcr >> 25) & 0xFF));
					writePacket((byte) ((pcr >> 17) & 0xFF));
					writePacket((byte) ((pcr >> 9) & 0xFF));
					writePacket((byte) ((pcr >> 1) & 0xFF));
					writePacket((byte) 0x00); //(byte) (pcr << 7 | 0x7E); // (6bit) reserved， 0x00
					writePacket((byte) 0x00);
				} else {
					writePacket((byte) 0x01); // adaptation_field_length
					writePacket((byte) (isFirstPes ? 0x40 : (isAudio && frameDataType == FrameDataType.MIXED ? 0x40 : 0x00)));
					// flag bits 0001 0000 , 0x10
					// flag bits 0100 0000 , 0x40
				}

				/* write PES HEADER */
				writePacket((byte) 0x00);
				writePacket((byte) 0x00);
				writePacket((byte) 0x01);
				writePacket(isAudio ? (byte) 0xc0 : (byte) 0xe0);

				int header_size = 5 + 5;

				// PES パケット長
				if (isAudio) {
					int pes_size = frameBufSize + header_size + 3;
					writePacket((byte) ((pes_size >> 8) & 0xFF));
					writePacket((byte) (pes_size & 0xFF));
				} else {
					writePacket((byte) 0x00); // 为0表示不受限制
					writePacket((byte) 0x00); // 16:
				}

				// PES ヘッダーの識別
				byte PTS_DTS_flags = isFrame ? (byte) 0xc0 : (byte) 0x00;
				writePacket((byte) 0x80); 			// 0x80 no flags set,  0x84 just data alignment indicator flag set
				writePacket(PTS_DTS_flags); 		// 0xC0 PTS & DTS,  0x80 PTS,  0x00 no PTS/DTS

				// write pts & dts
				if ( PTS_DTS_flags == (byte)0xc0 ) {
					writePacket((byte) 0x0A);

					write_pts_dts(3, pts);
					write_pts_dts(1, dts);
				} else if ( PTS_DTS_flags == (byte)0x80 ) {
					writePacket((byte) 0x05);
					write_pts_dts(2, pts);
				} else {
					writePacket((byte) 0x00);
				}


				// H264 NAL
				if ( !isAudio && Bytes.indexOf(frameBuf, H264_NAL ) == -1 ) {
					writePacket(H264_NAL, 0, H264_NAL.length);
				}

			}  else {

				// has adaptation
				if ( isAdaptationField ) {
					writePacket((byte) 1);
					writePacket((byte) 0x00);

				} else {
					// no adaptation
					// ts_header + ts_payload
				}

			}


			// fill data
			int tsBufRemaining = TS_PACKET_SIZE - mPacket.mOffset;
			if (frameBufRemaining >= tsBufRemaining) {
				writePacket(frameBuf, frameBufPtr, tsBufRemaining);
				frameBufPtr += tsBufRemaining;
			} else {

				int paddingSize = tsBufRemaining - frameBufRemaining;
				byte[] tsBuf = mPacket.mData;
				int offset = mPacket.mOffset;

				// 0x30  0011 0000
				// 0x10  0001 0000
				// has adaptation
				if ( isAdaptationField ) {

					int adaptationFieldLength = (tsBuf[4] & 0xFF);
					int start = TS_HEADER_SIZE + adaptationFieldLength + 1;
					int end = offset - 1;

					// move
					for (int i = end; i >= start; i--) {
						tsBuf[i + paddingSize] = tsBuf[i];
					}

					// fill data, 0xff
					for (int i = 0; i < paddingSize; i++) {
						tsBuf[start + i] = (byte) 0xff;
					}

					tsBuf[4] += paddingSize;

					// no adaptation
				} else {

					// set adaptation
					tsBuf[3] |= 0x20;
					tsBuf[4] = (byte) paddingSize;
					tsBuf[5] = 0;

					for (int i = 0; i < paddingSize; i++) {
						tsBuf[6 + i] = (byte) 0xFF;
					}
				}

				System.arraycopy(frameBuf, frameBufPtr, tsBuf, offset + paddingSize, frameBufRemaining);
				frameBufPtr += frameBufRemaining;

			}

			isFirstTs = false;
			notifyPacket();
		}
	}
	
	public void reset() {
		mPatContinuityCounter = 0;
		mPmtContinuityCounter = 0;
		mAudioContinuityCounter = 0;
		mVideoContinuityCounter = 0;
	}

	public enum FrameDataType {
		AUDIO,
		VIDEO,
		MIXED
	}

	public static class FrameData {
		public boolean isAudio;			// h264 | aac
		public byte[] buf;
		public long pts;
		public long dts;
	}
	
}
