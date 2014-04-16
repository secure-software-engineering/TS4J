package de.fraunhofer.sit.codescan.plugintests;

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
	private static HashMap<String, Map<String, Set<String>>> expected;

	protected void internalTransform(String phaseName,
			Map<String, String> options) {

		expected = new HashMap<String, Map<String, Set<String>>>();

		for (SootClass c : Scene.v().getApplicationClasses()) {

			for (SootMethod m : c.getMethods()) {
				if (m.hasTag("VisibilityAnnotationTag")) {
					VisibilityAnnotationTag tag = (VisibilityAnnotationTag) m
							.getTag("VisibilityAnnotationTag");
					for (AnnotationTag annTag : tag.getAnnotations()) {
						for (AnnotationElem elem : annTag.getElems()) {
							if (elem instanceof AnnotationStringElem) {
								AnnotationStringElem stringElem = (AnnotationStringElem) elem;
								String analysisClass = stringElem.getValue();
								if (!analysisClass.equals("")) {
									String type = annTag.getType();
									if (type.equals("Lannotation/DefinitelyVulnerable;")
											|| type.equals("Lannotation/FalseNegative;")) {

										getOrCreateAnalysisSet(analysisClass,
												annTag.getType(),
												m.getSignature());
									}
								}
							}
						}

					}
				}
			}

		}
	}

	private void getOrCreateAnalysisSet(String analysisClass,String errorType, String signature) {
		Map<String, Set<String>> errorTypesToSignatures = expected.get(analysisClass);
		if(errorTypesToSignatures == null){
			errorTypesToSignatures = new HashMap<String, Set<String>>();
		}
		Set<String> set = errorTypesToSignatures.get(errorType);
		if (set == null) {
			set = new HashSet<String>();
		}
		set.add(signature);
		errorTypesToSignatures.put(errorType, set);
		expected.put(analysisClass, errorTypesToSignatures);
	}

	public static HashMap<String, Map<String, Set<String>>> getExpected() {
		return expected;
	}

	/** Through a Annotation @DefinatelyVulnerable(foo.bar.Plugin.class) we receive the class again via Lfoo/bar/Plugin; 
	 * So we replace / via . and remove the preceeding L.
	 * 
	 * @param analysisClass
	 * @return
	 */
	private String toAnnotationComparableString(String analysisClass) {
		return analysisClass.replace("/", ".").substring(1, analysisClass.length() - 1);
	}

}
