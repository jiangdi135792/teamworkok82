package com.work.plugin.util.encryption;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.work.plugin.util.license.license.PRLInfo;
import org.junit.Test;
//import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class test {
	private Gson gson = new Gson();
	public static void main(String[] args){
		//test1();

	}

	 // ------------------------非对称加密-------------------------------


	@Test
	public void test1() {
		try {
			File path=new File("readme.class");

		long tim=	path.lastModified();

			List<PRLInfo> lstPRLInfo1=new ArrayList<PRLInfo>();
		List<PRLInfo> lstPRLInfo=new ArrayList<PRLInfo>();
			PRLInfo pRLInfo=new PRLInfo("-1","teamwork","-1","5","-1","1","-1","1");
			lstPRLInfo.add(pRLInfo);
			pRLInfo=new PRLInfo("中国人名 大公司","fineTimeSheet","2.0","-1","20171025","12","20171025","12");
			lstPRLInfo.add(pRLInfo);
			String data  = gson.toJson(lstPRLInfo); // {"name":"ZhangSan","age":24}
			//String data = "[{p:teamwork;v:-1;user:5;appstart:-1;appmonthlen:1;svrstart:-1;svrmonthlen:1},{p:fineTimeSheet;v:2.0;user:-1;appstart:20171025;appmonthlen:12;svrstart:20171025;svrmonthlen:12}[";
			System.out.println("非对称加密数据：" + data);

			// -----------------私钥加密--------------------------------			
		//	byte[] encrypted = CryptUtils.encrypt(data.getBytes());
		//	System.out.println("加密后: " + new String(encrypted));
		//	String keyBytes = Base64Enc.encode(encrypted);
			String keyBytes=null;
			System.out.println("加密后encode: " + keyBytes);
			keyBytes ="FUweqe4NRIV/ASLcY+qOmYXWAK2Ry70QF1H3izY8bHehl3xlEWSYsIMSQvqaYgi4o5SuqgG8V5Sw\n" +
					"yU6tRyN5kqPJwTI/Dxvfv8seFjE7/PPaNmEhJPzkDXwP+uamaM2RM7WmLef7PHrxC+H+UAf3pHuC\n" +
					"V4iaLTk46oU4lBvwn5OZb5bgPbdSc+CGai9ill+eg1g9C9Ntrtfw3piLk7QI4s7HmupQDppMEe1j\n" +
					"TQhsSP+KNvA5YWmEL5WJZYA0bm4YSRXwSxb2fJqpuHaMZRUG/59tnfz+DaMjAYeN1XmfJk++0PSt\n" +
					"jaaCbvEblO3jI36tX9sZaT1nRkfQ1JJeHWjk0Qdr8byuJBM0uLjQbBg8wTMntnnp9gRYXiI8q8dj\n" +
					"vpS5Uke962pd4ZlQsIU9Eth3kYfbgORDhJttPvFoctv9zwyI5N6tgrFthJWgfwPmdZsdkgNQA6Mw\n" +
					"spZahZYp/VTBxNv9KiShhDX6aKKmZplQThZCLMBb+PJBc6NzEgDjQcWF";
			// ------------------签名-----------------------------------
		//	String signed = CryptUtils.sign(encrypted);
		//	System.out.println("签名后：" + signed);
			
			// -----------------公钥解密--------------------------------
			byte[] encrypted = Base64Enc.decode(keyBytes);
			byte[] decrypted = CryptUtils.decrypt(encrypted);
			long seriesno=0;
			for(int i=0;i<decrypted.length;i++)seriesno=seriesno+decrypted[i];
			System.out.println("init 2 ok ：");
			lstPRLInfo1= gson.fromJson(new String(decrypted),(new TypeToken<List<PRLInfo>>() {
			}).getType());
			// -----------------验证签名--------------------------------
		//	Boolean verified = CryptUtils.verifySign(encrypted, signed);
		//	System.out.println("验签结果：" + verified);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 // ----------------------------------对称加密--------------------------


	@Test
	public void test2(){
		String data = "This is a RSA encryption test!";	
		System.out.println("对称加密数据：" + data);

		try {			
			// ---------------------------加密----------------------------
			byte[] encryted = CryptUtils2.encryt(data.getBytes());
			System.out.println("加密结果：" + new String(encryted));
			
			
			// ---------------------------解密----------------------------
			byte[] decrypted = CryptUtils2.decrypt(encryted);
			System.out.println("解密结果：" + new String(decrypted));
		} catch (InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void testx(){
		try {
			// 私钥加密
			Map<String, Object> keyPair = RSACrypt.genKeyPair();
			DataUtils.stringToFile(RSACrypt.getPrivateKey(keyPair), "D:\\prk.key");
			DataUtils.stringToFile(RSACrypt.getPublicKey(keyPair), "D:\\puk.key");

			String data = "This is a RSA encryption test!";
			byte[] encrypted = RSACrypt.encryptByPrivateKey(data.getBytes(), RSACrypt.getPrivateKey(keyPair));
			// 签名
			String signed = RSACrypt.sign(encrypted, RSACrypt.getPrivateKey(keyPair));

			System.out.println(new String(encrypted));

			// 公钥解密
			byte[] decrypted = RSACrypt.decryptByPublicKey(encrypted, RSACrypt.getPublicKey(keyPair));
			System.out.println(new String(decrypted));

			// 验证签名
			Boolean verified = RSACrypt.verify(encrypted, RSACrypt.getPublicKey(keyPair), signed);
			System.out.println(verified);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
