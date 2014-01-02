package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface CallContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends VarContext<Var,State,StmtID> {
	public IfCheckContext<Var,State,StmtID> and();
	public ValueContext<Var,State,StmtID> trackThis();
	public ValueContext<Var,State,StmtID> trackReturnValue();
	public ValueContext<Var,State,StmtID> trackParameter(int paramIndex);
	public ReportError<Var,State,StmtID> reportError(String errorMessage);
}