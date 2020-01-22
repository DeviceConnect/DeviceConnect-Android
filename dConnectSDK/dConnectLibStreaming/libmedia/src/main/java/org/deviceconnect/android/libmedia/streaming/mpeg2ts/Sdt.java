package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import org.deviceconnect.android.libmedia.streaming.mpeg2ts.util.TsUtil;

/**
 * Service Description Table
 * 
 * @author zhuam
 *
 */
public class Sdt extends Ts {
	
	// skip
	
	@Override
	protected void parseTsPayload(byte[] b) {
		
		TsUtil.hexString(b, 0, b.length);
	}

}
