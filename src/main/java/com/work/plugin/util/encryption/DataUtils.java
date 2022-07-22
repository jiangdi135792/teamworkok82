package com.work.plugin.util.encryption;

import javax.crypto.SecretKey;
import java.io.*;

public class DataUtils {

    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;
    
    /**
     * <p>
     * 文件转换为字符串
     * </p>
     * 
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    public static String fileToString(String filePath) throws Exception{
    	byte[] data = fileToByte(filePath);
    	return new String(data, "utf-8");
    }
    
    /**
     * <p>
     * 字符串写入文件
     * </p>
     * 
     * @param data 字符串数据
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    public static void stringToFile(String data, String filePath) throws Exception{
    	byte[] bytes = data.getBytes("utf-8");
    	byteArrayToFile(bytes, filePath);
    }
    
    /**
     * <p>
     * 文件转换为二进制数组
     * </p>
     * 
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    public static byte[] fileToByte(String filePath) throws Exception {
        byte[] data = new byte[0];
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
            byte[] cache = new byte[CACHE_SIZE];
            int nRead = 0;
            while ((nRead = in.read(cache)) != -1) {
                out.write(cache, 0, nRead);
                out.flush();
            }
            out.close();
            in.close();
            data = out.toByteArray();
         }
        return data;
    }
     
    /**
     * <p>
     * 二进制数据写文件
     * </p>
     * 
     * @param bytes 二进制数据
     * @param filePath 文件生成目录
     */
    public static void byteArrayToFile(byte[] bytes, String filePath) throws Exception {
        InputStream in = new ByteArrayInputStream(bytes);   
        File destFile = new File(filePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        destFile.createNewFile();
        OutputStream out = new FileOutputStream(destFile);
        byte[] cache = new byte[CACHE_SIZE];
        int nRead = 0;
        while ((nRead = in.read(cache)) != -1) {   
            out.write(cache, 0, nRead);
            out.flush();
        }
        out.close();
        in.close();
    }

	/*
	 * 保存密钥
	 */
	public static void saveKey(SecretKey key, String keyFileName){
		try {
			ObjectOutputStream out = new ObjectOutputStream
					(new FileOutputStream(keyFileName));
			out.writeObject(key);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * 读取密钥
	 */
	public static SecretKey readKey(String keyFileName){
		SecretKey key = null;
		try {
			ObjectInputStream in = new ObjectInputStream
					(new FileInputStream(keyFileName));
			key = (SecretKey)in.readObject();
			in.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return key;
	}
}
