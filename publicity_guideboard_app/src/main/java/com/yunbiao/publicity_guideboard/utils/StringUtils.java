package com.yunbiao.publicity_guideboard.utils;

import android.util.Log;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	public static String toStringHex(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	public static String hexStringToString(String s) {
		if (s == null || s.equals("")) {
			return null;
		}
		s = s.replace(" ", "");
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "GBK");
			new String();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	public static String hexStringToString(String s,String charsetName) {
		if (s == null || s.equals("")) {
			return null;
		}
		s = s.replace(" ", "");
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, charsetName);
			new String();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	public static String hexStr2Str(String hexStr) {
		hexStr = hexStr.toString().trim().replace(" ", "")
				.toUpperCase(Locale.CHINA);
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int iTmp = 0x00;
		;

		for (int i = 0; i < bytes.length; i++) {
			iTmp = mHexStr.indexOf(hexs[2 * i]) << 4;
			iTmp |= mHexStr.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (iTmp & 0xFF);
		}
		return new String(bytes);
	}

	public static String byte2HexStrBak(byte[] src, int leng){
		    StringBuilder stringBuilder = new StringBuilder("");
		    if (src == null || src.length <= 0) {   
		        return null;   
		    }   
		    for (int i = 0; i < leng; i++) {   
		        int v = src[i] & 0xFF;   
		        String hv = Integer.toHexString(v);
		       if (hv.length() < 2) {   
		            stringBuilder.append(0);   
		        }   
		        stringBuilder.append(hv);   
		    }   
		    return stringBuilder.toString();   
		}

	public static void printHexString( byte[] b, int iLen) {     
	   StringBuffer str = new StringBuffer("");
		for (int i = 0; i < b.length; i++) {    
	     String hex = Integer.toHexString(b[i] & 0xFF);
	     if (hex.length() == 1) {
	       hex = '0' + hex;    
	     }
	     str.append(hex);
	   }
	   Log.i("打印", str.toString().toUpperCase());
	}  

	public static String byte2HexStr(byte[] b, int iLen) {
		printHexString(b, iLen);
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < iLen; n++) {
			sb.append(mChars[(b[n] & 0xFF) >> 4]);
			sb.append(mChars[b[n] & 0x0F]);
			sb.append(' ');
		}
		return sb.toString().trim().toUpperCase(Locale.US);
	}

	/**
	 * 判断字符是否是中文
	 *
	 * @param c 字符
	 * @return 是否是中文
	 */
	public static boolean isChinese ( char c ) {
		Character . UnicodeBlock ub = Character . UnicodeBlock . of ( c ) ;
		if ( ub == Character . UnicodeBlock . CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character . UnicodeBlock . CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character . UnicodeBlock . CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character . UnicodeBlock . GENERAL_PUNCTUATION
				|| ub == Character . UnicodeBlock . CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character . UnicodeBlock . HALFWIDTH_AND_FULLWIDTH_FORMS ) {
			return true ;
		}
		return false ;
	}

	/**
	 * 判断字符串是否是乱码
	 *
	 * @param strName 字符串
	 * @return 是否是乱码
	 */
	public static boolean isMessyCode ( String strName ) {
		Pattern p = Pattern. compile ( "\\s*|t*|r*|n*" ) ;
		Matcher m = p . matcher ( strName ) ;
		String after = m . replaceAll ( "" ) ;
		String temp = after . replaceAll ( "\\p{P}" , "" ) ;
		char [ ] ch = temp . trim ( ) . toCharArray ( ) ;
		float chLength = ch . length ;
		float count = 0 ;
		for ( int i = 0 ; i < ch . length ; i ++ ) {
			char c = ch [ i ] ;
			if ( ! Character . isLetterOrDigit ( c ) ) {
				if ( ! isChinese ( c ) ) {
					count = count + 1 ;
				}
			}
		}
		float result = count / chLength ;
		if ( result > 0.4 ) {
			return true ;
		} else {
			return false ;
		}

	}

	private final static String mHexStr = "0123456789ABCDEF";

	private final static char[] mChars = "0123456789ABCDEF".toCharArray();
}
