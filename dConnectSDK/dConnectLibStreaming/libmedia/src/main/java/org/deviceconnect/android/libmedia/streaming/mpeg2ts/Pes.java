package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

/**
 *  Audio & Video Payload
 *  
 *  @author zhuam
 */
public class Pes extends Ts {
	
	//
	public AdaptationField adaptation_filed;
	
	public int start_code_prefix;
	public int stream_id;				
	public int PES_packet_length;
	
	// Optional PES header, variable length (length >= 9)
	public int PES_scrambling_control;
	public int PES_priority;
	public int data_alignment_indicator;
	public int copyright;
	public int original_or_copy;
	public int PTS_DTS_flags;
	
	//
	public int ESCR_flag;
	public int ES_rate_flag;
	public int DSM_trick_mode_flag;
	public int additional_copy_info_flag;
	public int PES_CRC_flag;
	public int PES_extension_flag;
	public int PES_header_data_length;
	
	public long pts;
	public long dts;
	
	public byte[] es_data;

	@Override
	protected void parseTsPayload(byte[] b) {
		
		int ptr = 3;
		int adaptation_field_length = 0;
		switch( adaptation_field_control ) {
		case 0:		// reserved for future use by ISO/IEC
		case 2:		// 仅含调整字段，无有效负载
			return;
		case 1:		// 无调整字段，仅含有效负载
			break;
		case 3:		// 调整字段后含有效负载
			adaptation_field_length = b[4] & 0xFF;		// 8 bits
			 ptr = ptr + 1 + adaptation_field_length;
			break;
		}
		
		 // Adaptation fields parse
		 // adaptation_field && adaptation_field_length
		 if  ( adaptation_field_control == 2 || adaptation_field_control == 3 ) {
			 
			adaptation_filed = new AdaptationField();
			
			adaptation_filed.discontinuity_indicator 				= (b[5] & 0x80) >> 7;
			adaptation_filed.random_access_indicator 			 	= (b[5] & 0x40) >> 6;
			adaptation_filed.elementary_stream_priority_indicator 	= (b[5] & 0x20) >> 5;
			adaptation_filed.pcr_flag							 	= (b[5] & 0x10) >> 4;
			adaptation_filed.opcr_flag							 	= (b[5] & 0x08) >> 3;
			adaptation_filed.splicing_point_flag 				 	= (b[5] & 0x04) >> 2;
			adaptation_filed.transport_private_data_flag 		 	= (b[5] & 0x02) >> 1;
			adaptation_filed.adaptation_field_extension_flag 	 	= b[5] & 0x01;
	        
	        if (adaptation_filed.pcr_flag == 1) {
	        	
	        	// parse PCR
	        	adaptation_filed.program_clock_reference_base = ((long) (b[6] & 0x0ff) << 25)
						| ((long) (b[7] & 0x0ff) << 17)
						| ((long) (b[8] & 0x0ff) << 9)
						| ((long) (b[9] & 0x0ff) << 1)
						| ((long) (b[10] & 0x0ff) >> 7);
	        	
	        	//use pcr base and ignore the extension
	        	adaptation_filed.program_clock_reference_extension = 0x00;
	        	//((long) (b[ 11 ] & 0x001) << 8) | ((long) (b[ 12 ] & 0x0ff));
	        }
		 }
	
		
		// skip adaptation_field_length
		//int ptr = 4 + adaptation_field_length;
		
		if ( payload_unit_start_indicator == 1 ) {
			
			/*
			 0x000000: 47 41 01 34 01 40 00 00 01 c0 00 bd 80 80 05 21 00 cd 2b 2d ff f1 50 80 16 bf fc 21 66 cf ff ff
			 0x000020: ff a3 61 20 d4 28 12 0a 8c 84 82 11 98 48 22 53 08 85 2e 92 71 9c f9 99 55 c0 df 1b 47 5f f9 8b
			 0x000040: 7c dd 72 fd db 45 7c 0d d1 c6 dc 70 74 69 dc 98 cf d1 ea ba ad a3 53 01 d3 85 7d a5 ce 62 ac d5
			 0x000060: 97 46 e5 ea b9 fb b3 d8 fd eb d4 5e 01 2c c3 41 30 4c 24 23 0a 04 82 83 20 a1 48 48 12 09 84 42
			 0x000080: d6 71 94 b7 8e a5 4c d6 53 59 bb 8b ff a8 6d fe 92 7d 9f 89 e4 af a7 f4 ba 3d 7f 33 a8 76 99 3e
			 0x0000a0: ff e7 1d 6e 82 f0 00 ae 6f aa 5f 63 72 9a bc 18 bd 65 e7 f7 cd 2d 94 70 cb 98 64 a1
			 */

			//+++++++++++++++++++++++++++++++++++++ Parse PES Header ++++++++++++++++++++++++++++++++++++++++++++++++++++
			start_code_prefix 			= ((b[ ptr+1 ] & 0xFF) << 16 | 
								 		  (b[ ptr+2 ] & 0xFF) << 8 | 
								 		  (b[ ptr+3 ] & 0xFF)) & 0xFF;					// 24 bits,
		
			stream_id 					= b[ptr + 4] & 0xFF;							// 8 bits
			PES_packet_length 			= (b[ ptr+5 ] << 8 | b[ ptr+6 ]) & 0xFF;		// 16 bits,
			
			if (stream_id != 0xbc  // program_stream_map
					&& stream_id != 0xbe  // padding_stream
					&& stream_id != 0xbf  // private_stream_2
					&& stream_id != 0xf0  // ECM
					&& stream_id != 0xf1  // EMM
					&& stream_id != 0xff  // program_stream_directory
					&& stream_id != 0xf2  // DSMCC
					&& stream_id != 0xf8)   // H.222.1 type E
			{
			
				PES_scrambling_control 		= ((b[ ptr+7 ] & 0x30) >> 4);
				PES_priority 				= ((b[ ptr+7 ] & 0x08) >> 3);
				data_alignment_indicator 	= ((b[ ptr+7 ] & 0x04) >> 2);
				copyright 					= ((b[ ptr+7 ] & 0x02) >> 1);
				original_or_copy 			= (b[ ptr+7 ]  & 0x01);
				PTS_DTS_flags 				= ((b[ ptr+8 ] & 0xc0) >> 6);
				ESCR_flag 					= ((b[ ptr+8 ] & 0x20) >> 5);
				ES_rate_flag 				= ((b[ ptr+8 ] & 0x10) >> 4);
				DSM_trick_mode_flag 		= ((b[ ptr+8 ] & 0x08) >> 3);
				additional_copy_info_flag 	= ((b[ ptr+8 ] & 0x04) >> 2);
				PES_CRC_flag 				= ((b[ ptr+8 ] & 0x02) >> 1);
				PES_extension_flag 			= (b[ ptr+8 ] & 0x01);
				PES_header_data_length 		= b[ ptr+9 ] & 0xFF;			//  pts & dts length
				
				
				// The PTS/DTS as described in 2.4.3.7 of iso 13813, prefix and marker bits are ignored
				if (PTS_DTS_flags == 2) {

					pts = (((long) b[ptr + 10] & 0x0e) << 29) | 
							((b[ptr + 11] & 0xff) << 22) | 
							(((b[ptr + 12] & 0xff) >> 1) << 15) | 
							((b[ptr + 13] & 0xff) << 7) | 
							((b[ptr + 14] & 0xff) >> 1);

					
				} else if (PTS_DTS_flags == 3) {
					
					 pts = (((long) b[ptr+10] & 0x0e) << 29) | 
							   ((  b[ptr+11] & 0xff) << 22) | 
							   ((( b[ptr+12] & 0xff) >> 1) << 15) | 
							   ((  b[ptr+13] & 0xff) << 7) | 
							   ((  b[ptr+14] & 0xff) >> 1);
					
					
					 dts = (((long) b[ptr+15] & 0x0e) << 29) | 
							((b[ptr+16] & 0xff) << 22) | 
							(((b[ptr+17] & 0xff) >> 1) << 15) | 
							((b[ptr+18] & 0xff) << 7) | 
							((b[ptr+19] & 0xff) >> 1);
				}

				//
				int offset = ptr + 9 + PES_header_data_length + 1;
				
				if ( ESCR_flag == 1 ) {
					offset += 6;
					
				}
				
				if (ES_rate_flag == 1) {
					offset += 3;
				}
				
				int len = b.length - offset;
				
				es_data = new byte[ len ];
				System.arraycopy(b, offset, es_data, 0, len);
			}
			
		} else {

			/*
			 0x000000: 47 01 01 35 aa 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff
			 0x000020: ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff
			 0x000040: ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff
			 0x000060: ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff
			 0x000080: ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff
			 0x0000a0: ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff 53 dd 57 80 7f 9a 04 9e f7 0d 71 14 07
			 */
			
			int offset = ptr + 1;
			int len = b.length - offset;
			
			es_data = new byte[ len ];
			System.arraycopy(b, offset, es_data, 0, len);
		}
		
	}
	
	//
	public static class AdaptationField {

		public int discontinuity_indicator;
		public int random_access_indicator;
		public int elementary_stream_priority_indicator;
		public int pcr_flag;
		public int opcr_flag;
		public int splicing_point_flag;
		public int transport_private_data_flag;
		public int adaptation_field_extension_flag;

		public long program_clock_reference_base;
		public long program_clock_reference_extension;

		public long getPCR() {
			if ( pcr_flag == 0)
				return 0;
			
			long pcr = program_clock_reference_base * 300 + program_clock_reference_extension;
			return pcr;

		}

		public double getPCR_TIME() {
			if ( pcr_flag == 0)
				return 0;
			
			double pcr_time = ((double) program_clock_reference_base / 90000.0f)
					+ ((double) program_clock_reference_extension / 27000000.0f);
			return pcr_time;
		}
	}
	
}
