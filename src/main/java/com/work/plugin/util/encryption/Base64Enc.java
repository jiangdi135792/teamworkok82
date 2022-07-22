package com.work.plugin.util.encryption;


//import com.sun.org.apache.xml.internal.security.utils.Base64;

//import com.sun.org.apache.xml.internal.security.utils.Base64;

//import org.springframework.security.crypto.codec.Base64;


import org.bouncycastle.util.encoders.Base64;

/**
 * <p>
 * BASE64编码解码工具包
 * </p>
 * <p>
 * 依赖javabase64-1.3.1.jar
 * </p>
 */
public class Base64Enc {
 
     
    /**
     * <p>
     * BASE64字符串解码为二进制数据
     * </p>
     * 
     * @param base64
     * @return
     * @throws Exception
     */
    public static byte[] decode(String base64) throws Exception {
        return Base64.decode(base64.getBytes());
    }
     
    /**
     * <p>
     * 二进制数据编码为BASE64字符串
     * </p>
     * 
     * @param bytes
     * @return
     * @throws Exception
     */
    public static String encode(byte[] bytes) throws Exception {
        return new String(Base64.encode(bytes));
    }
     
    /**
     * <p>
     * 将文件编码为BASE64字符串
     * </p>
     * <p>
     * 大文件慎用，可能会导致内存溢出
     * </p>
     * 
     * @param filePath 文件绝对路径
     * @return
     * @throws Exception
     */
    public static String encodeFile(String filePath) throws Exception {
        byte[] bytes = DataUtils.fileToByte(filePath);
        return encode(bytes);
    }
     
    /**
     * <p>
     * BASE64字符串转回文件
     * </p>
     * 
     * @param filePath 文件绝对路径
     * @param base64 编码字符串
     * @throws Exception
     */
    public static void decodeToFile(String filePath, String base64) throws Exception {
        byte[] bytes = decode(base64);
        DataUtils.byteArrayToFile(bytes, filePath);
    } 
}
