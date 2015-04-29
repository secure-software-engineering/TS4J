package de.fraunhofer.sit.codescane.transformers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.NumericConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JNopStmt;
import soot.jimple.internal.JimpleLocal;

public class PreparationTransformer extends BodyTransformer {
	private int replaceCounter = 1;
	private Body body;
	private void transformConstantInInvokes() {
		Set<Unit> cwnc = getStmtsWithConstants();
		for (Unit u : cwnc) {
			if (u instanceof ReturnStmt && !(u instanceof ReturnVoidStmt)) {
				ReturnStmt returnStmt = (ReturnStmt) u;
				ValueBox opBox = returnStmt.getOpBox();
				Value value = opBox.getValue();
				String label = "varReplacer" + new Integer(replaceCounter).toString();
				Local paramVal = new JimpleLocal(label, value.getType());
				replaceCounter++;
				AssignStmt newUnit = new JAssignStmt(paramVal, opBox.getValue());
				body.getLocals().add(paramVal);
				body.getUnits().insertBefore(newUnit, u);
				opBox.setValue(paramVal);
			} else {
				InvokeExpr ie = ((Stmt) u).getInvokeExpr();
				List<ValueBox> useBoxes = ie.getUseBoxes();
				for (ValueBox vb : useBoxes) {
					Value v = vb.getValue();
					if (v instanceof Constant) {

						String label = "varReplacer" + new Integer(replaceCounter).toString()
								+ "i" + useBoxes.indexOf(vb);
						replaceCounter++;
						Local paramVal = new JimpleLocal(label, v.getType());
						AssignStmt newUnit = new JAssignStmt(paramVal,
								vb.getValue());
						body.getLocals().add(paramVal);
						body.getUnits().insertBefore(newUnit, u);
						vb.setValue(paramVal);
					}
				}
			}
		}
	}



	@Override
	protected void internalTransform(Body b, String arg1,
			Map<String, String> arg2) {
		this.body = b;
		addNopStmt();
		transformConstantInInvokes();
	}	
	
	private void addNopStmt() {
		PatchingChain<Unit> units = body.getUnits();
		units.addFirst(new JNopStmt());
	}



	private Set<Unit> getStmtsWithConstants() {
		Set<Unit> retMap = new LinkedHashSet<Unit>();
		for (Unit u : body.getUnits()) {
			if (u instanceof ReturnStmt) {
				if (((ReturnStmt) u).getOp() instanceof Constant) {
					retMap.add(u);
				}
			} else if (((Stmt) u).containsInvokeExpr()) {
				InvokeExpr ie = ((Stmt) u).getInvokeExpr();
				for (Value arg : ie.getArgs()) {
					if (arg instanceof StringConstant || arg instanceof NumericConstant) {
						retMap.add(u);
						break;
					}
				}
			}
		}
		return retMap;
	}

}
