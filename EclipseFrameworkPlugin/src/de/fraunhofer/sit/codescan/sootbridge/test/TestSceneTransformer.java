package de.fraunhofer.sit.codescan.sootbridge.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fraunhofer.sit.codescan.framework.VulnerableMethodTag;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;

public class TestSceneTransformer extends SceneTransformer {
	protected void internalTransform(String phaseName,
			Map<String, String> options) {

		Set<String> actual = new HashSet<String>();
		Set<String> expected = new HashSet<String>();
		for (SootClass c : Scene.v().getApplicationClasses()) {

			for (SootMethod m : c.getMethods()) {

				if (m.hasTag(VulnerableMethodTag.class.getName())) {
					actual.add(m.getDeclaringClass().getName());
				}
				if (m.hasTag("VisibilityAnnotationTag")) {
					VisibilityAnnotationTag tag = (VisibilityAnnotationTag) m
							.getTag("VisibilityAnnotationTag");
					for (AnnotationTag annTag : tag.getAnnotations()) {
						if (annTag.getType().equals(
								"Lannotation/DefinitelyVulnerable;")) {
							expected.add(m.getDeclaringClass().getName());
							break;
						}
					}
				}
			}
		
		}
		System.out.println(actual);
		System.out.println(expected);
	}
}
