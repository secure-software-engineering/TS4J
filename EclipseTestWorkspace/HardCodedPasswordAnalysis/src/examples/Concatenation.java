package examples;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class Concatenation {
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void main(String[] args) {
		int nec;
		String a = "Schluessel Part 1";
		String b = "Scshslussesssel Part 2";
		b  = a + b; //Teil1Teil2
		byte[] keyBytes = b.getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}
	 /* uses badsource and badsink */
    public void bad() throws Throwable
    {
        String data;

        /* FLAW: Set data to a hardcoded value */
        data = "23 ~j;asn!@#/>as";

        if (data != null)
        {
            String stringToEncrypt = "Super secret Squirrel";
            byte[] byteStringToEncrypt = stringToEncrypt.getBytes("UTF-8");
            /* POTENTIAL FLAW: Use data as a cryptographic key */
            SecretKeySpec secretKeySpec = new SecretKeySpec(data.getBytes("UTF-8"), "AES");
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] byteCipherText = aesCipher.doFinal(byteStringToEncrypt);
        }

    }
}
