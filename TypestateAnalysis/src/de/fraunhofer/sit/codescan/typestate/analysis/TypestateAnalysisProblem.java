package de.fraunhofer.sit.codescan.typestate.analysis;

import heros.FlowFunction;
import heros.FlowFunctions;
import soot.SootMethod;
import soot.Unit;
import de.fraunhofer.sit.codescan.framework.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.framework.IFDSAnalysisManager;

public class TypestateAnalysisProblem extends AbstractIFDSAnalysisProblem<Abstraction> {

	public TypestateAnalysisProblem(IFDSAnalysisManager manager) {
		super(manager);
	}

	protected Abstraction createZeroValue() {
		return new Abstraction();
	}

	@Override
	protected FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			public FlowFunction<Abstraction> getCallFlowFunction(Unit arg0, SootMethod arg1) {
				return null;
			}

			public FlowFunction<Abstraction> getCallToReturnFlowFunction(
					Unit arg0, Unit arg1) {
				return null;
			}

			public FlowFunction<Abstraction> getNormalFlowFunction(Unit arg0,
					Unit arg1) {
				return null;
			}

			public FlowFunction<Abstraction> getReturnFlowFunction(Unit arg0,
					SootMethod arg1, Unit arg2, Unit arg3) {
				return null;
			}
		};
	}

}
