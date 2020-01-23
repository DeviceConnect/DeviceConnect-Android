package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteUtil {

    public static List<Integer> kmp(ByteBuffer src, byte[] pattern){
        List<Integer> indexs = new ArrayList<>();
        final int length = src.remaining();
        if (length < pattern.length) {
            return indexs;
        }

        int[] next = new int[pattern.length];
        next[0] = 0;
        for(int i = 1,j = 0; i < pattern.length; i++){
            while(j > 0 && pattern[j] != pattern[i]){
                j = next[j - 1];
            }
            if(pattern[i] == pattern[j]){
                j++;
            }
            next[i] = j;
        }

        for(int i = 0, j = 0; i < length; i++){
            byte b = src.get(i);
            while(j > 0 && b != pattern[j]){
                j = next[j - 1];
            }
            if (b == pattern[j]){
                j++;
            }
            if (j == pattern.length){
                indexs.add(i-j+1);
                j = 0;
            }
        }
        return indexs;
    }
	
	public static List<Integer> kmp(byte[] src, byte[] pattern){
		List<Integer> indexs = new ArrayList<Integer>();
		if(src.length <0 || pattern.length > src.length)
			return indexs;
		
		//计算next[]
		int[] next = new int[pattern.length];
        next[0] = 0;
        for(int i = 1,j = 0; i < pattern.length; i++){
            while(j > 0 && pattern[j] != pattern[i]){
                j = next[j - 1];
            }
            if(pattern[i] == pattern[j]){
                j++;
            }
            next[i] = j;
        }
        
        for(int i = 0, j = 0; i < src.length; i++){
            while(j > 0 && src[i] != pattern[j]){
                j = next[j - 1];
            }
            if(src[i] == pattern[j]){
                j++;
            }
            if(j == pattern.length){
            	indexs.add(i-j+1);
            	j = 0;
            }
        }
        return indexs;
    }

}