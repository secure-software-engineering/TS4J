package ivexamples;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CompleteExample {
	public static void main(String[] args) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{

		byte[] iv = {1,2,1,2,1,1,8,8,1,6,1,2,1,9,9,1};
		IvParameterSpec constant = new IvParameterSpec(iv);

		String keyString = "tsest";
		Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec key = new SecretKeySpec(keyString.getBytes(), "AES");
		// Initialize the Cipher swsith key and parameters
		encryptCipher.init(Cipher.ENCRYPT_MODE, key, constant);
		encryptCipher.doFinal("isssnpust".getBytes());
	}

}
