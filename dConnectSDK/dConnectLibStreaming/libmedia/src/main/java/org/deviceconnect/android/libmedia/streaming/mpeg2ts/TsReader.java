package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import android.util.Log;

/**
 * 
 * Ts reader
 * 
 * @author zhuam
 *
 */
public class TsReader {

	//
	public static final int TS_PACKET_SIZE = 188;

	private static final String TAG = "MPEG2-TS";

	private long mReadPacketNum;

	public Ts[] parseTsPacket(byte[] buf) {
		
		if ( buf.length % TS_PACKET_SIZE != 0 ) {
			return null;
		}
		
		//
		Ts[] tsArr = new Ts[ buf.length / TS_PACKET_SIZE ];
		Ts beforeTs = null;
		int tsIndex = 0;
		
		for( int offset = 0; offset < buf.length; offset+=TS_PACKET_SIZE, mReadPacketNum++ ) {
			
			Ts ts = null;
			String error = null;
			
			byte[] tsBuf = new byte[ TS_PACKET_SIZE ];
			System.arraycopy(buf, offset, tsBuf, 0, tsBuf.length);
			
			// First byte of each TS packet
			if ( (tsBuf[0] & 0xff) != Ts.SYNC_BYTE ) {
				tsIndex++;
				continue;
			}
			
//			if( tsIndex == 3 ) {
//				System.out.println("tsIndex=" + tsIndex);
//			}
			
			// 在PES分包后，table_id 不能作为类型包检测的依据 
			int payload_unit_start_indicator =  ((tsBuf[1] & 0xFF) >> 6) & 0x01;	
			int adaptation_field_control 	 = 	((tsBuf[3] & 0xFF) >> 4) & 0x03;
			
			if ( beforeTs != null && payload_unit_start_indicator == 0) {
				ts = new Pes();
				
			} else {
				
				int ptr = 3;
				int adaptation_field_length = 0;
				switch( adaptation_field_control ) {
				case 0:		// reserved for future use by ISO/IEC
				case 2:		// 仅含调整字段，无有效负载
					break;
				case 1:		// 无调整字段，仅含有效负载
					break;
				case 3:		// 调整字段后含有效负载
					adaptation_field_length = tsBuf[ptr+1] & 0xFF;
					ptr = ptr + 1 + adaptation_field_length;
					break;
				}

				// 检测音视频流
				if ( tsBuf[ptr+1] == 0x00 && tsBuf[ptr+2] == 0x00 &&  tsBuf[ptr+3] == 0x01 && (  (tsBuf[ptr+4] & 0xff) == 0xc0 ||  (tsBuf[ptr+4] & 0xff) == 0xe0 )  ) {
					ts = new Pes();
					
				} else {

					// 检测 PAT & PMT & SDT
					if ( tsBuf[4] == 0x00 && tsBuf[5] == Ts.PAT_TABLE_ID) {
						ts = new Pat();
						
					} else if (tsBuf[4] == 0x00 && tsBuf[5] == Ts.PMT_TABLE_ID) {
						ts = new Pmt();
						
					} else if (tsBuf[4] == 0x00 && tsBuf[5] == Ts.SDT_TABLE_ID) {
						ts = new Sdt();
		
					} else {
						error = "Unknown table";

					}
					
				}

			}
			
			if ( ts != null ) {
				
				ts.parse( tsBuf );
				tsArr[tsIndex++] = ts;
				
				beforeTs = ts;
			} else {
				Log.d(TAG, "TsReader: error = " + error + ", num = " + mReadPacketNum);
			}
		}
		
		return tsArr;
	}

}
