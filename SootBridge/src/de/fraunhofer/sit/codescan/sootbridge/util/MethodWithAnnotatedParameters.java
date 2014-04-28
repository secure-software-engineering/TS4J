package de.fraunhofer.sit.codescan.sootbridge.util;

import java.util.ArrayList;
import java.util.List;

public class MethodWithAnnotatedParameters {
	private ArrayList<Integer> sinkIndices;
	private String methodSignature;
	public MethodWithAnnotatedParameters(String string){
		methodSignature = string.replaceAll("@[A-Za-z]* +", "");
		string = string.substring(string.indexOf("("), string.indexOf(")"));
		string = string.replaceAll("[^@,]", "");
		String[] split = string.split(",");
		sinkIndices = new ArrayList<Integer>();
		for(int i = 0; i < split.length; i++){
			if(!split[i].equals("")){
				sinkIndices.add(i);
			}
		}
	}
	public String getMethodSignature() {
		return methodSignature;
	}
	public String toString(){
		return "SinkMethod " + methodSignature + " Parameters: " + getAnnotatedParameterIndices().toString();
	}
	public ArrayList<Integer> getAnnotatedParameterIndices() {
		return sinkIndices;
	}
}
