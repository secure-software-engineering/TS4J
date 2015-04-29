package aesexamples;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class Case1 {
	public static void main(String... args) throws NoSuchAlgorithmException, NoSuchPaddingException{
//		String t = "AES";
//		String r = "DES";
		String t = "DES";
		Cipher instance = Cipher.getInstance(t);
		System.out.println(instance);
	}
}
