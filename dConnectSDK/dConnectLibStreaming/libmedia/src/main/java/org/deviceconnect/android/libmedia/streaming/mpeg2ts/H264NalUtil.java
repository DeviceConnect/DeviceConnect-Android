package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.util.Arrays;
import java.util.List;

/**
 * 判断H264帧类型
 * 
 * @author xuwenfeng@variflight.com
 * @see http://blog.csdn.net/dittychen/article/details/55509718
 */
public class H264NalUtil {
	
	/* 
		    数组中的元素用二进制表示如下： 
		
		    假设：初始为0，已写入为+，已读取为- 
		     
		    字节:       1       2       3       4 
		         00000000 00000000 00000000 00000000      下标 
		
		    0x00:                           00000000      x[0] 
		
		    0x01:                           00000001      x[1] 
		    0x03:                           00000011      x[2] 
		    0x07:                           00000111      x[3] 
		    0x0f:                           00001111      x[4] 
		
		    0x1f:                           00011111      x[5] 
		    0x3f:                           00111111      x[6] 
		    0x7f:                           01111111      x[7] 
		    0xff:                           11111111      x[8]    1字节 
		
		   0x1ff:                      0001 11111111      x[9] 
		   0x3ff:                      0011 11111111      x[10]   i_mask[s->i_left] 
		   0x7ff:                      0111 11111111      x[11] 
		   0xfff:                      1111 11111111      x[12]   1.5字节 
		
		  0x1fff:                  00011111 11111111      x[13] 
		  0x3fff:                  00111111 11111111      x[14] 
		  0x7fff:                  01111111 11111111      x[15] 
		  0xffff:                  11111111 11111111      x[16]   2字节 
		
		 0x1ffff:             0001 11111111 11111111      x[17] 
		 0x3ffff:             0011 11111111 11111111      x[18] 
		 0x7ffff:             0111 11111111 11111111      x[19] 
		 0xfffff:             1111 11111111 11111111      x[20]   2.5字节 
		
		0x1fffff:         00011111 11111111 11111111      x[21] 
		0x3fffff:         00111111 11111111 11111111      x[22] 
		0x7fffff:         01111111 11111111 11111111      x[23] 
		0xffffff:         11111111 11111111 11111111      x[24]   3字节 
		
		0x1ffffff:    0001 11111111 11111111 11111111      x[25] 
		0x3ffffff:    0011 11111111 11111111 11111111      x[26] 
		0x7ffffff:    0111 11111111 11111111 11111111      x[27] 
		0xfffffff:    1111 11111111 11111111 11111111      x[28]   3.5字节 
		
		0x1fffffff:00011111 11111111 11111111 11111111      x[29] 
		0x3fffffff:00111111 11111111 11111111 11111111      x[30] 
		0x7fffffff:01111111 11111111 11111111 11111111      x[31] 
		0xffffffff:11111111 11111111 11111111 11111111      x[32]   4字节 
		
	*/  
	
	//帧类型  
	private static final int FRAME_I  = 15;
	private static final int FRAME_P  = 16;  
	private static final int FRAME_B  = 17;
	private static final int UNSUPPORT  = -1;
	
	private static final byte[] NAL_DELIMITER = {0x00, 0x00, 0x01};
	
	private static int[] i_mask ={0x00,  
             0x01,      0x03,      0x07,      0x0f,  
             0x1f,      0x3f,      0x7f,      0xff,  
             0x1ff,     0x3ff,     0x7ff,     0xfff,  
             0x1fff,    0x3fff,    0x7fff,    0xffff,  
             0x1ffff,   0x3ffff,   0x7ffff,   0xfffff,  
             0x1fffff,  0x3fffff,  0x7fffff,  0xffffff,  
             0x1ffffff, 0x3ffffff, 0x7ffffff, 0xfffffff,  
             0x1fffffff,0x3fffffff,0x7fffffff,0xffffffff};  
	
	public static int getPesFrameType(byte[] avcData) {
		List<Integer> delimiters = ByteUtil.kmp(avcData, NAL_DELIMITER);
		if(!delimiters.isEmpty()) {
			int pos = delimiters.get(delimiters.size() -1) + NAL_DELIMITER.length + 1;
			if(avcData.length < pos + 4 && delimiters.size() >= 2) {
				pos = delimiters.get(delimiters.size() -2) + NAL_DELIMITER.length + 1;
			}
			if(avcData.length > pos + 4)
				return  getFrameType(Arrays.copyOfRange(avcData, pos, pos+4));
		}
		return -1;
	}
	
	private static int getFrameType(byte[] buff) {
		NauBst s = new NauBst();
		int frame_type = 0;
		s.buff = buff;
		s.buffPtr = 0;
		s.left = 8;

		/* i_first_mb */
		bs_read_ue(s);
		/* picture type */
		frame_type = bs_read_ue(s);
		switch (frame_type) {
		case 0:
		case 5: /* P */
			return FRAME_P;
		case 1:
		case 6: /* B */
			return FRAME_B;
		case 3:
		case 8: /* SP */
			return FRAME_P;
		case 2:
		case 7: /* I */
			return FRAME_I;
		case 4:
		case 9: /* SI */
			return FRAME_I;
		}

		return UNSUPPORT;
	}
	
	private static int bs_read(NauBst payload, int count) {

		int shr;
		int result = 0;

		while (count > 0) {
			if (payload.buffPtr >= payload.buff.length) { 
				break;
			}

			if ((shr = payload.left - count) >= 0) {
				result |= (payload.buff[payload.buffPtr] >> shr) & i_mask[count];// “|=”:按位或赋值，A |= B 即 A = A|B
				payload.left -= count;
				if (payload.left == 0) {
					payload.buffPtr++;
					payload.left = 8;
				}
				return (result);
			} else {
				result |= (payload.buff[payload.buffPtr] & i_mask[payload.left]) << -shr;
				count -= payload.left;
				payload.buffPtr++;
				payload.left = 8;
			}
		}

		return (result);
	}
	  
	private static int bs_read1(NauBst payload) {

		if (payload.buffPtr < payload.buff.length) {
			int result;

			payload.left--;
			// 把要读的比特移到当前字节最右，然后与0x01:00000001进行逻辑与
			result = (payload.buff[payload.buffPtr] >> payload.left) & 0x01;
			// 当前字节剩余未读位数是0，即是说当前字节全读过了
			if (payload.left == 0) {
				payload.buffPtr++; 					// 指针s->p 移到下一字节
				payload.left = 8; 					// 新字节中，未读位数当然是8位
			}
			return result;
		}

		return 0;
	}
	  
	private static int bs_read_ue( NauBst payload ) {  
	    int i = 0;  
	    //条件为：读到的当前比特=0，指针未越界，最多只能读32比特  
	    while( bs_read1( payload ) == 0 && payload.buffPtr < payload.buff.length && i < 32 )
	        i++;
	    
	    return( ( 1 << i) - 1 + bs_read( payload, i ) );      
	}
	
}

class NauBst {
	byte[] buff;                //缓冲区
	int buffPtr;				
    int left;                 // p所指字节当前还有多少 “位” 可读写 count number of available(可用的)位   
}
