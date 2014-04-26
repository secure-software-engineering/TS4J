package de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis;

import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.UnitPrinter;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.NopStmt;
import soot.jimple.internal.JNopStmt;
import soot.tagkit.AnnotationConstants;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Host;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.util.Switch;

public class NopTransformer extends SceneTransformer {

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		for(SootClass c:Scene.v().getClasses()){
			for(SootMethod m: c.getMethods()){
				if(m.hasActiveBody()){
					Body b = m.getActiveBody();
					PatchingChain<Unit> units = b.getUnits();
					units.addFirst(new JNopStmt());
				}
			}
		}
	}
}
