package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface VarContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends Done<Var,State,StmtID> {
	public Done<Var,State,StmtID> toState(State s);
}