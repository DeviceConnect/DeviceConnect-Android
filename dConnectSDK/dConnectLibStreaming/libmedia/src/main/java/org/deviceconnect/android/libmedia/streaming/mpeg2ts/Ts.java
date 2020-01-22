package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

/**
 * MPEG-TS packet
 * 
 * @author zhuam
 *
 */
public abstract class Ts {
	
	// Transport Stream Description Table
	public static final int PAT_TABLE_ID = 0x00;
	public static final int PMT_TABLE_ID = 0x02;
	public static final int SDT_TABLE_ID = 0x42;
	
	//
	public static final int PAT_PID = 0;
	
	public static final int AUDIO_STREAM_ID = 0xc0;
	public static final int VIDEO_STREAM_ID = 0xe0;
	
	//
	public static final int SYNC_BYTE = 0x47; 

	// 4 bytes, packet header
	public byte sync_byte; 								// 8 bits, 同步字节
	public int transport_error_indicator; 				// 1 bit, 错误指示信息,
	public int payload_unit_start_indicator; 			// 1 bit, 负载单元开始标志, 注：packet不满188字节时需填充
														// 注意， payload_unit_start_indicator == 1， 一个 PES 开始
	public int transport_priority; 						// 1 bit, 传输优先级标志
	public int PID; 									// 13 bits, PID是TS流中唯一识别标志
	public int transport_scrambling_control; 			// 2 bits, 加密标志
	public int adaptation_field_control; 				// 2 bits, 附加区域控制
	public int continuity_counter; 						// 4 bits, 包递增计数

	// 184 bytes, packet data
	
	public byte[] full_data;
	
	private void parseTsHeader(byte[] b) {
		
		// First byte of each TS packet
		if ( (b[0] & 0xff) != SYNC_BYTE ) {
			return;
		}
		
		// header
		sync_byte = b[0];
		transport_error_indicator 		= 	((b[1] & 0xFF) >> 7) & 0x01;						
		payload_unit_start_indicator 	=   ((b[1] & 0xFF) >> 6) & 0x01;						
		transport_priority 				= 	((b[1] & 0xFF) >> 5) & 0x01;						
		PID 							= 	((b[1] << 8) | (b[2] & 0xFF))  & 0x1FFF;			
		transport_scrambling_control 	= 	((b[3] & 0xFF) >> 6) & 0x03;						
		adaptation_field_control 		= 	((b[3] & 0xFF) >> 4) & 0x03;					
		continuity_counter 				=	(b[3] & 0xFF) & 0xF;		
	}
	
	protected abstract void parseTsPayload(byte[] b);
	
	public void parse(byte[] b) {
		
		full_data = b;
		
		parseTsHeader(b);
		parseTsPayload(b);
	}

}