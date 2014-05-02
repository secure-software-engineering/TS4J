package ivexamples;

import javax.crypto.spec.IvParameterSpec;

public class InterproceduralSimpleConstant {
	public static void main(String[] args){
		byte[] iv = getIV();
		IvParameterSpec constant = new IvParameterSpec(iv);
	}
	public static byte[] getIV(){
		byte[] bytes = {1,2,0,2,21,1,2,8,1,2,3,2,1,9,9,1};
		return bytes;
	}
}
