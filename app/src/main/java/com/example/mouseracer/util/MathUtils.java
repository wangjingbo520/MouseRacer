package com.example.mouseracer.util;

import java.util.Random;

/**
 * @author bobo
 * @date 2018/9/28
 * describe
 */
public class MathUtils {
    private static final char[] bcdLookup = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'};

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String makeChecksum(String data) {
        int total = 0;
        int len = data.length();
        int num = 0;
        while (num < len) {
            String s = data.substring(num, num + 2);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }
        /**
         * 用65535求余最大是65534，即16进制的FFFF
         */
        int mod = total % 65535;
        String hex = Integer.toHexString(mod& 0xff);
        len = hex.length();
        // 如果不够校验位的长度，补0
        switch (len) {
            case 1:
                hex = "0" + hex;
                break;
//            case 2:
//                hex = "00" + hex;
//                break;
//            case 3:
//                hex = "0" + hex;
//                break;
            default:
                break;
        }
        return hex;
    }

    /**
     * 将十进制转成十六进制
     *
     * @param value
     * @return
     */
    public static String convertDecimalToBinary(int value) {
        String str;
        String a = Integer.toHexString(value);
        if (a.length() == 1) {
            str = "0" + a;
        } else {
            str = a;
        }
        return str;
    }

    /**
     * KEY0+KEY1+KEY2+KEY3+KEY4+KEY5  十六进制字符串随机数
     *
     * @param len
     * @return
     */
    public static String randomHexString(int len) {
        try {
            StringBuffer result = new StringBuffer();
            for (int i = 0; i < len; i++) {
                result.append(Integer.toHexString(new Random().nextInt(16)));
            }
            return result.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




}
