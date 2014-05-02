package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface EqualsContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public CallContext<Var,State,StmtID> equalsThis();
	public CallContext<Var,State,StmtID> equalsReturnValue();
	public EqualsContext<Var,State,StmtID> not();
	public CallContext<Var,State,StmtID> equalsParameter(int paramIndex);
	public CallContext<Var,State,StmtID> equalsConstant(Class<?> type);
	public CallContext<Var,State,StmtID> eachEqualsInstance(Class<?> type);
}