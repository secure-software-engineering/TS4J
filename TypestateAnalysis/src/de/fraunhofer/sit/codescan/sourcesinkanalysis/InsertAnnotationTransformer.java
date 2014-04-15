package de.fraunhofer.sit.codescan.sourcesinkanalysis;

import java.util.Map;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationConstants;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;

public class InsertAnnotationTransformer extends SceneTransformer {

	@Override
	protected void internalTransform(String phaseName,
			Map<String, String> options) {

		for(SootClass c : Scene.v().getApplicationClasses()){
			for(SootMethod m: c.getMethods()){
				if(m.getSignature().equals("<servletexample.ServletResponse: void addHeader(java.lang.String,java.lang.String)>")){
					VisibilityAnnotationTag visibilityAnnotationTag = new VisibilityAnnotationTag(AnnotationConstants.SOURCE_VISIBLE);
					visibilityAnnotationTag.addAnnotation(new AnnotationTag("Lservletexample/Sink;"));
					m.addTag(visibilityAnnotationTag);
				} else if (m.getSignature().equals("<servletexample.HttpServletRequest: java.lang.String getParam(java.lang.String)>")){
					VisibilityAnnotationTag visibilityAnnotationTag = new VisibilityAnnotationTag(AnnotationConstants.SOURCE_VISIBLE);
					visibilityAnnotationTag.addAnnotation(new AnnotationTag("Lservletexample/Source;"));
					m.addTag(visibilityAnnotationTag);
				}
			}
		}

	}

}
