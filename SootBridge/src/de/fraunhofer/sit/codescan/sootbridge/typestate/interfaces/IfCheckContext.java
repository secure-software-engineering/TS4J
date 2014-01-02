package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface IfCheckContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends VarContext<Var,State,StmtID> {
	public EqualsContext<Var,State,StmtID> ifValueBoundTo(Var var);
	public CallContext<Var,State,StmtID> always();
	public CallContext<Var,State,StmtID> ifInState(State s);
}