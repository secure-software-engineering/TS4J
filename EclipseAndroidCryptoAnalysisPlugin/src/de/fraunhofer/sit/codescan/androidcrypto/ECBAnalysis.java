package de.fraunhofer.sit.codescan.androidcrypto;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import de.fraunhofer.sit.codescan.framework.IMethodBasedAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IAnalysisContext;

public class ECBAnalysis implements IMethodBasedAnalysisPlugin {

	public void analyzeMethod(SootMethod m, IAnalysisContext manager) {
		if(m.hasActiveBody()) {
			Body b = m.getActiveBody();
			ConstantPropagatorAndFolder.v().transform(b);
			for(Unit u: b.getUnits()) {
				Stmt s = (Stmt)u;
				if(s.containsInvokeExpr()) {
					InvokeExpr ie = s.getInvokeExpr();
					SootMethod callee = ie.getMethod();
					if(callee.getName().equals("getInstance") &&
					   callee.getDeclaringClass().getName().equals("javax.crypto.Cipher")) {
						Value firstArgument = ie.getArg(0);
						if(firstArgument instanceof StringConstant) {
							StringConstant constant = (StringConstant) firstArgument;
							if(!constant.value.contains("/") || /*ECB is default*/
								constant.value.contains("/ECB/")) {
								//found violation
								return;
							}
						}
					}
				}
			}			
		}
		manager.markMethodAsBenign();		
	}

}
