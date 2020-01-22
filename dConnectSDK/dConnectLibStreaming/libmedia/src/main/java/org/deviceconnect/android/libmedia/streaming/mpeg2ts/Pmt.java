package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.util.ArrayList;
import java.util.List;

/**
 * PMT 节目映像表，它指出了它所描述的节目 其所对应的视频流、音频流、PCR（时间参考信息）的PID
 * 
 * @see ISO/IEC 13818-1  2.4.4.8 Program Map Table
 * 
 * @author zhuam
 *
 */
public class Pmt extends Ts {
	
	public int table_id ;							// 8 bits, 	固定为0x02, 表示PMT表  
	public int section_syntax_indicator;			// 1 bit, 	固定为0x01
	public int zero;								// 1 bit, 	0x01
	public int reserved1;							// 2 bits, 	0x03	
	public int section_length;						// 12 bits, 首先两位bit置为00，它指示段的byte数，由段长度域开始，包含CRC
	public int program_number;						// 16 bits, 指出该节目对应于可应用的Program map PID
	public int reserved2;							// 2 bits,	0x03 
	public int version_number;						// 5 bits,	指出TS流中Program map section的版本号
	public int current_next_indicator;				// 1 bit,   当该位置1时，当前传送的Program map section可用, 
													//			当该位置0时，指示当前传送的Program map section不可用，下一个TS流的Program map section有效
	
	public int section_number;						// 8 bits,	固定为0x00  
	public int last_section_number;					// 8 bits,  固定为0x00  
	public int reserved3;							// 3 bits,	0x07 
	public int PCR_PID;								// 13 bits, TS包的PID值，该TS包含有PCR域
													//			 该PCR值对应于由节目号指定的对应节目,如果对于私有数据流的节目定义与PCR无关，这个域的值将为0x1FFF
	
	public int reserved4;							// 4 bits,	预留为0x0F
	public int program_info_length;					// 12 bits, 前两位bit为00。该域指出跟随其后对节目信息的描述的byte数
					
	public List<Descriptor> descriptors;
	public List<Stream>	streams;
	
	public long CRC_32;								// 32 bits,
	
	@Override
	protected void parseTsPayload(byte[] b) {
		
		// 4 bytes, ts header 
		// 1 byte, 起始指示符 skip

		table_id 						= 	b[ 5 ] & 0xFF;
		section_syntax_indicator 		= 	((b[ 6 ] & 0xFF) >> 7) & 0x01;
		zero 							= 	((b[ 6 ] & 0xFF) >> 6) & 0x01;
		reserved1 						= 	((b[ 6 ] & 0xFF) >> 4) & 0x03;
		section_length 					=   ((b[ 6 ] & 0x0F) << 8) | (b[ 7 ] & 0xFF);
		
		program_number 					=   ((b[ 8 ] << 8) | b[ 9 ] ); 

		reserved2 						= 	((b[ 10 ] & 0xFF) >> 6) & 0x03;
		version_number 					= 	((b[ 10 ] & 0xFF) >> 1) & 0x1F;
		current_next_indicator 			= 	b[ 10 ] & 0x01;
		section_number 					= 	b[ 11 ] & 0xFF;
		last_section_number 			= 	b[ 12 ] & 0xFF;
		reserved3 						= 	((b[ 13 ] & 0xFF) >> 5) & 0x7;
		PCR_PID 						=   ((b[ 13 ] & 0x1F) << 8) | (b[ 14 ] & 0xFF);
		
		reserved4 						= 	((b[ 15 ] & 0xFF) >> 4) & 0xF;
		program_info_length 			= 	((b[ 15 ] & 0x0F) << 8) | (b[ 16 ] & 0xFF);
		
		//
		int ptr = 16;	// 16
		
	
		
		// parse descriptor
		if ( program_info_length > 0 ) {
			
			descriptors = new ArrayList<Descriptor>();
			
			int ptr_end = ptr + program_info_length;
			while( ptr < ptr_end ) {
				
				int tag = b[ptr+1] & 0xFF;
				int len = b[ptr+2] & 0xFF;
				byte[] rawData = new byte[ len ];
				System.arraycopy(b, ptr+3, rawData, 0, len);
				
				ptr = ptr + 2 + len;
				
				Descriptor descriptor = new Descriptor();
				descriptor.tag = tag;
				descriptor.len = len;
				descriptor.rawData = rawData;
				descriptors.add( descriptor );
			}
		}
		
		// reset
		ptr = 16 + program_info_length;

		// parse stream
		streams = new ArrayList<Stream>();
		
		for ( ; ptr <= ( section_length + 2 ) - 4; ) {
			
			Stream stream = new Stream();
			
			stream.stream_type 		= 	b[ ptr+1 ] & 0xFF;
			stream.reserved5 		= 	((b[ ptr+2 ] & 0xFF) >> 5) & 0x7;
			stream.elementary_PID 	=   (b[ ptr+2 ] & 0x1F) << 8 | (b[ ptr+3 ] & 0xFF);
			stream.reserved6 		= 	((b[ptr+4] & 0xFF) >> 4) & 0xF;
			stream.ES_info_length	= 	(b[ ptr+4 ] & 0xF) << 8 | (b[ ptr+5 ] & 0xFF);
					
			
			ptr = ptr + 5;
			if ( stream.ES_info_length  > 0 ) {
				ptr = ptr + stream.ES_info_length;
			}
			
			streams.add(stream);
		}

		CRC_32							= ((b[ptr+1] & 0xFF) << 24 | 
										  (b[ptr+2] & 0xFF) << 16 | 
										  (b[ptr+3] & 0xFF) << 8 | 
										  (b[ptr+4] & 0xFF)) & 0xFF ;
	}
	
	public static class Descriptor {
		public int tag;
		public int len;
		public byte[] rawData;
	}
	
	// 指示特定PID的节目元素包的类型、elementary_PID、ES信息长度  
	public static class Stream {
		public int stream_type;
		public int reserved5;
		public int elementary_PID;
		public int reserved6;
		public int ES_info_length;
		public int descriptor;
	}
	
}
