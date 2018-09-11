package cn.cloudwiz.dalian.commons.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class UUIDUtils {

	private static final String[] CHARS = new String[] {
		"a" , "b" , "c" , "d" , "e" , "f" , "g" , "h" ,  
        "i" , "j" , "k" , "l" , "m" , "n" , "o" , "p" , "q" , "r" , "s" , "t" ,  
        "u" , "v" , "w" , "x" , "y" , "z" , "0" , "1" , "2" , "3" , "4" , "5" ,  
        "6" , "7" , "8" , "9" , "A" , "B" , "C" , "D" , "E" , "F" , "G" , "H" ,  
        "I" , "J" , "K" , "L" , "M" , "N" , "O" , "P" , "Q" , "R" , "S" , "T" ,  
        "U" , "V" , "W" , "X" , "Y" , "Z"  
	}; 
	
	public static String fromString(String str){
		String[] result = new String[4];
		str = DigestUtils.md5Hex(str);
		for(int i=0;i<result.length;i++){
			long v = Long.parseLong(str.substring(i*8, (i+1)*8), 16);
			StringBuffer buff = new StringBuffer();
			for (int j = 0; j < 6; j++) {
				// 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引
				long index = 0x0000003D & v;
				// 把取得的字符相加
				buff.append(CHARS[(int)index]);
				// 每次循环按位右移 5 位
				v = v >> 5;
			}		
			result[i] = buff.toString();
		}
		return result[(int)(Math.random()*4)];
	}
	
	public static String randomShortID(){
		return fromString(randomUUID());
	}
	
	public static String randomUUID(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
