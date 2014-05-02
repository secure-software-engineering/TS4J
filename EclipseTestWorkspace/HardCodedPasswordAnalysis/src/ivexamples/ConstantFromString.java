package ivexamples;

import javax.crypto.spec.IvParameterSpec;

public class ConstantFromString {
	public static void main(String[] args){
		String iv = "should lead also lead to an error";
		IvParameterSpec constant = new IvParameterSpec(iv.getBytes());
	}


}
