package com.sinloingok.app.g4;

/**
 * @Author 吴世俊
 * @Data 2021/4/13 11:37
 * @Version 1.0
 * @Description 通用工具类
 */
public class CommonTools {

    public static final int FIXEDLENGTHFRAME_LENGTH = 256;//定长解码器FixedLengthFrameDecoder对应的消息长度

    public static final int LINEBASEDFRAME_LENGTH = 64;//行解码器LineBasedFrameDecoder对应的消息最大长度

    public static final int DELIMITERBASEDFRAME_LENGTH = 64;//分隔符解码器DelimiterBasedFrameDecoder对应的消息最大长度

    /**
     *
     * @param str
     * @param assignlength
     * @return
     * 生成指定长度的字符串,不足位右补空格,否则还回原字符串
     */
    public static String formatString(String str,int assignlength){
        int intStrLen = 0;

        if(str != null){
            intStrLen = str.length();
        }

        if(intStrLen >= assignlength){
            return str;
        }else{
            //右补空格
            String strSpace = "";
            for (int i = 0,num = assignlength - intStrLen; i < num; i++){

                strSpace = strSpace + " ";
            }
            return str + strSpace;
        }
    }
}
