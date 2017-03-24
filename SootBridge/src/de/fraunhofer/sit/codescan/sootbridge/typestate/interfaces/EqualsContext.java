package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

import java.util.List;

import de.fraunhofer.sit.codescan.sootbridge.IAnalysisContext;

public interface EqualsContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public CallContext<Var,State,StmtID> equalsThis();
	public CallContext<Var,State,StmtID> equalsReturnValue();
	public EqualsContext<Var,State,StmtID> not();
	public CallContext<Var,State,StmtID> equalsParameter(int paramIndex);
	public CallContext<Var,State,StmtID> equalsConstant(Class<?> type);
	public CallContext<Var,State,StmtID> eachEqualsInstance(Class<?> type);
	public CallContext<Var, State, StmtID> equalsString(String string);
	public CallContext<Var, State, StmtID> startsWith(String string);
	public CallContext<Var, State, StmtID> doesNotContain(String par);
	public CallContext<Var, State, StmtID> startsWiths(List<String> par);
	CallContext<Var, State, StmtID> contains(String par);
}