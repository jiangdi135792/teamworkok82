package com.work.plugin.util.encryption;

/*
 * 非对称加密
 */
public class CryptUtils {
	private static String publicKey = "";
	private static String privateKey = "";
	private static String publicKey1 = "";
	private static String privateKey1 = "";
	/*
	 * 构造函数
	 * 初始化加载公钥和私钥
	 */
	static{
		try {
		//	publicKey = DataUtils.fileToString(CryptConf.pukFilePath);
		//	privateKey = DataUtils.fileToString(CryptConf.prkFilePath);
				//publicKey1 = DataUtils.fileToString(CryptConf.pukFilePath);
				//privateKey1 = DataUtils.fileToString(CryptConf.prkFilePath);
			    privateKey = "";
				 publicKey= "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnYgO2E/DQ4yv/fbNbXTfimvMYo/sXpUhTfKK/+XTjX1smeSbbLprBHqjRXrc1T4fXXpovC7oHQF7a0tWcWzgMXbIBwmMZEEAU5NRxNuB3T8xuaR5c2O2xDucYn5k3Y6Vd1hs/3S5qdvgRYq94dKhgcuvwEaSgiaVGt8hSjf7tnQIDAQAB";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * 加密数据。RSA 算法加密
	 */
	public static byte[] decrypt(byte[] data){
		byte[] encrypted = null;
		try {
				encrypted = RSACrypt.decryptByPublicKey(data, publicKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encrypted;
	}
	
	/*
	 * 解密数据。RSA 算法加密
	 */
	public static byte[] encrypt(byte[] data){
		byte[] decrypted = null;
		try {
				decrypted = RSACrypt.encryptByPrivateKey(data, privateKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decrypted;
	}
	
	/*
	 * 数字签名。
	 */
	public static String sign(byte[] data){
		String signed = null;
		try {
			signed = RSACrypt.sign(data, privateKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return signed;
	}
	
	/*
	 * 验证数字签名。
	 */
	public static boolean verifySign(byte[] data, String sign){
		Boolean succeed = false;
		try {
			succeed = RSACrypt.verify(data, publicKey, sign);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return succeed;
	}
}
