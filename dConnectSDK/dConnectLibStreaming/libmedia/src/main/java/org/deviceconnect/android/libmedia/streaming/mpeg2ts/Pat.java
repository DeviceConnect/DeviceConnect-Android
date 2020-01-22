package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

/**
 * Program Association Table (PAT)
 * 
 * @author zhuam
 *
 */
public class Pat extends Ts {
	
	public int table_id;							// 8 bits,  固定为0x00 ，标志是该表是PAT表  
	public int section_syntax_indicator;			// 1 bit,   段语法标志位，固定为1  
	public int zero;								// 1 bit,	0
	public int reserved1;							// 2 bits,	保留位
	public int section_length;						// 12 bits,	表示从下一个字段开始到CRC32(含)之间有用的字节数
	public int transport_stream_id;					// 16 bits, 该传输流的ID，区别于一个网络中其它多路复用的流 
	public int reserved2;							// 2 bits,	保留位
	public int version_number;						// 5 bits,  范围0-31，表示PAT的版本号 
	public int current_next_indicator;				// 1 bit,	发送的PAT是当前有效还是下一个PAT有效
	public int section_number;						// 8 bits,	分段的号码。PAT可能分为多段传输，第一段为00，以后每个分段加1，最多可能有256个分段
	public int last_section_number;					// 8 bits,	最后一个分段的号码  
	public int program_number;						// 16 bits,
	public int reserved_3;							// 3 bits,	保留位  
	public int network_PID;							// 13 bits, 网络信息表（NIT）的PID,节目号为0时对应的PID为network_PID
	
	public int[] program_map_PID;					// 13 bits, 有效的PMT的PID, 可通过这个PID值去查找PMT包
	public long CRC_32;								// 32 bits, CRC32校验码  
	
	
	@Override
	protected void parseTsPayload(byte[] b) {
		
		if (payload_unit_start_indicator == 1 && PID == PAT_PID && adaptation_field_control == 1) {

			table_id 					= b[5] & 0xFF;
			section_syntax_indicator 	= ((b[6] & 0xFF) >> 7) & 0x01;
			zero 						= ((b[6] & 0xFF) >> 6) & 0x01;
			reserved1 					= ((b[6] & 0xFF) >> 4) & 0x03;
			section_length 				= (((b[6] << 8) | (b[7] & 0xFF)) & 0xFFFF) & 0xFFF;
			section_length -= 5;
			
			transport_stream_id 		= ((b[8] << 8) | (b[9] & 0xFF)) & 0xFFFF;
			reserved2 					= ((b[10] & 0xFF) >> 6) & 0x03;
			version_number 				= ((b[10] & 0xFF) >> 1) & 0x1F;
			current_next_indicator 		= (b[10] & 0xFF) & 0x01;
			section_number 				= b[11] & 0xFF;
			last_section_number 		= b[12] & 0xFF;
			
			int ptr = 12;
			
			// 4:CRC
			int pmt_pid_count = (section_length - 4) / 4;
			if ( pmt_pid_count > 0 ) {				
				program_map_PID = new int[pmt_pid_count];

				for (int j = 0; j < pmt_pid_count; j++) {
					int programNum  = ((b[ ptr+1 ] << 8) | ( b[ptr+2] & 0xff)) & 0xffff;
					if ( programNum != 0 ) {
						program_map_PID[j] = ((b[ptr+3] << 8) | ( b[ptr+4] & 0xff)) & 0x1fff;
					}
					
					ptr = ptr + 4;
				}
			}

			CRC_32 	= 	((  b[ ptr+1 ] & 0xff) << 24
	                    | (b[ ptr+2 ] & 0xff) << 16
	                    | (b[ ptr+3 ] & 0xff) << 8
	                    | (b[ ptr+4 ] & 0xff)) & 0xFF;

		}
	}
	
}