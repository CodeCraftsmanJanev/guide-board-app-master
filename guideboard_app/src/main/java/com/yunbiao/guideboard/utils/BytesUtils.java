package com.yunbiao.guideboard.utils;

import java.math.BigInteger;

public class BytesUtils {
    /**
     * 16进制转10禁止
     * @param hexadecimalStr
     * @return
     */
    public static int hex16to10(String hexadecimalStr){
        int getDataDecimal = 0;//转化得到的目标数据
        //16进制代表数据 4位数字
        try {
            if (hexadecimalStr.length() == 4) {
                int bit1Num = Integer.parseInt(hexadecimalStr.substring(0, 1), 16);//获取第一位。判断是正数还是负数
                if (bit1Num < 8) { //小于8是正数
                    getDataDecimal = Integer.parseInt(hexadecimalStr, 16);
                } else { //负数
                    hexadecimalStr = "FFFF" + hexadecimalStr; //先不全八位
                    getDataDecimal = new BigInteger(hexadecimalStr, 16).intValue();
                }
            }else {
                getDataDecimal=Integer.parseInt(hexadecimalStr, 16);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return getDataDecimal;
    }
    /**
     * 字节转十六进制
     * @param b 需要进行转换的byte字节
     * @return  转换后的Hex字符串
     */
    public static String byteToHex(byte b){
        String hex = Integer.toHexString(b & 0xFF);
        if(hex.length() < 2){
            hex = "0" + hex;
        }
        return hex;
    }

    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return  转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Hex字符串转byte
     * @param inHex 待转换的Hex字符串
     * @return  转换后的byte
     */
    public static byte hexToByte(String inHex){
        return (byte)Integer.parseInt(inHex,16);
    }

    /**
     * hex字符串转byte数组
     * @param inHex 待转换的Hex字符串
     * @return  转换后的byte数组结果
     */
    public static byte[] hexToByteArray(String inHex){
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1){
            //奇数
            hexlen++;
            result = new byte[(hexlen/2)];
            inHex="0"+inHex;
        }else {
            //偶数
            result = new byte[(hexlen/2)];
        }
        int j=0;
        for (int i = 0; i < hexlen; i+=2){
            result[j]=hexToByte(inHex.substring(i,i+2));
            j++;
        }
        return result;
    }

    /// 截取字节数组
    /// </summary>
    /// <param name="srcBytes">要截取的字节数组</param>
    /// <param name="startIndex">开始截取位置的索引</param>
    /// <param name="length">要截取的字节长度</param>
    /// <returns>截取后的字节数组</returns>
    public static byte[] SubByte(byte[] srcBytes, int startIndex, int length) {
        byte[] bytes=new byte[length];
        System.arraycopy(srcBytes,startIndex,bytes,0,length);
        return bytes;
    }
}
