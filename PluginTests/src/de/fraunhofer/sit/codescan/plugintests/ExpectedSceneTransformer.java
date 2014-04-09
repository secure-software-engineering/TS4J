package de.fraunhofer.sit.codescan.plugintests;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationClassElem;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;

public class ExpectedSceneTransformer extends SceneTransformer {
	private static  HashMap<String, Set<String>> expected;

	protected void internalTransform(String phaseName,
			Map<String, String> options) {

		expected = new HashMap<String, Set<String>>();
		for (SootClass c : Scene.v().getApplicationClasses()) {

			for (SootMethod m : c.getMethods()) {
				if (m.hasTag("VisibilityAnnotationTag")) {
					VisibilityAnnotationTag tag = (VisibilityAnnotationTag) m
							.getTag("VisibilityAnnotationTag");
					for (AnnotationTag annTag : tag.getAnnotations()) {
						if (annTag.getType().equals(
								"Lannotation/DefinitelyVulnerable;")) {
							for(AnnotationElem elem : annTag.getElems()){
								if(elem instanceof AnnotationClassElem){
									AnnotationClassElem stringElem =(AnnotationClassElem) elem;
									String analysis = stringElem.getDesc();
									if(!analysis.equals("")){
										getOrCreateAnalysisSet(analysis, m.getSignature());
									}
								}
							}
							
						}
					}
				}
			}

		}
	}
	private void getOrCreateAnalysisSet(String analysis, String signature) {
		Set<String> set = expected.get(analysis);
		if(set == null){
			set = new HashSet<String>();
		}
		set.add(signature);
		expected.put(analysis, set);	
	}
	public static HashMap<String, Set<String>> getExpected(){
		return expected;
	}
}
