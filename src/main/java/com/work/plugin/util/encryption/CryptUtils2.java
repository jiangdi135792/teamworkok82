package com.work.plugin.util.encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class CryptUtils2 {
	
	/*//KeyGenerator 提供对称密钥生成器的功能，支持各种算法
	private static KeyGenerator keygen;*/
	
	//SecretKey 负责保存对称密钥
	private static SecretKey deskey;
	
	//Cipher负责完成加密或解密工作
	private static Cipher c;
	//该字节数组负责保存加密的结果
	private static byte[] cipherByte;
	
	static{
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
		/*//实例化支持DES算法的密钥生成器(算法名称命名需按规定，否则抛出异常)
		keygen = KeyGenerator.getInstance("DES");*/
		//获取密钥
		deskey = DataUtils.readKey(CryptConf.keyFileName);
		//生成Cipher对象,指定其支持的DES算法
		try {
			c = Cipher.getInstance("DES");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 对二进制数据加密
	 * 
	 * @param data 加密数据
	 * @param deskey 加密密钥
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] encryt(byte[] data) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		// 根据密钥，对Cipher对象进行初始化，ENCRYPT_MODE表示加密模式
		c.init(Cipher.ENCRYPT_MODE, deskey);
		//byte[] src = str.getBytes();
		// 加密，结果保存进cipherByte
		cipherByte = c.doFinal(data);
		return cipherByte;
	}

	/**
	 * 对字符串解密
	 * 
	 * @param buff
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] decrypt(byte[] buff) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		// 根据密钥，对Cipher对象进行初始化，DECRYPT_MODE表示加密模式
		c.init(Cipher.DECRYPT_MODE, deskey);
		cipherByte = c.doFinal(buff);
		return cipherByte;
	}
}
