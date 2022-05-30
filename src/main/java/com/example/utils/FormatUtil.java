package com.example.utils;

public class FormatUtil {

    /**
     * 判断文件后缀类型是否合法
     * @param suffix
     * @return
     */
    public static boolean judgeFormat(String suffix) {
//            ESconsts.legalFormat legalFormat = ESconsts.legalFormat.valueOf(suffix.toLowerCase());
//        ESconsts.legalFormat[] values = ESconsts.legalFormat.values();
//        for (ESconsts.legalFormat value : values) {
//            if (value.toString().equals(suffix)) {
//                return true;
//            }
//        }
//        return false;
        for (int i = 0; i < ESconsts.legalFormat.values().length; i++) {
            if (ESconsts.legalFormat.values()[i].toString().equals(suffix)){
                return true;
            }
        }
        return false;
    }
}
