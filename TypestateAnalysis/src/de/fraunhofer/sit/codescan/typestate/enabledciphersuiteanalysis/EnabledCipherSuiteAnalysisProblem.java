package de.fraunhofer.sit.codescan.typestate.enabledciphersuiteanalysis;

import static de.fraunhofer.sit.codescan.typestate.enabledciphersuiteanalysis.EnabledCipherSuiteAnalysisProblem.State.INIT_SOCKET;
import static de.fraunhofer.sit.codescan.typestate.enabledciphersuiteanalysis.EnabledCipherSuiteAnalysisProblem.State.SET_CIPHERSUITES;
import static de.fraunhofer.sit.codescan.typestate.enabledciphersuiteanalysis.EnabledCipherSuiteAnalysisProblem.Var.SOCKET;

import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateForwardAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.typestate.enabledciphersuiteanalysis.EnabledCipherSuiteAnalysisProblem.State;
import de.fraunhofer.sit.codescan.typestate.enabledciphersuiteanalysis.EnabledCipherSuiteAnalysisProblem.StmtID;
import de.fraunhofer.sit.codescan.typestate.enabledciphersuiteanalysis.EnabledCipherSuiteAnalysisProblem.Var;


public class EnabledCipherSuiteAnalysisProblem extends AbstractJimpleTypestateForwardAnalysisProblem<Var, State, StmtID>
{

	public EnabledCipherSuiteAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	
	private static final String ERROR_MESSAGE_1 = "Error! You don't set a ciphersuit.";

	//CORRECT INVOKES
	private static final String INVOKE_CREATESOCKET = "<javax.net.ssl.SSLSocketFactory: java.net.Socket createSocket(java.lang.String,int)>";
	private static final String INVOKE_SET_CIPHERSUITES = "<javax.net.ssl.SSLSocket: void setEnabledCipherSuites(java.lang.String[])>";	
	private static final String START_HANDSHAKE = "<javax.net.ssl.SSLSocket: void startHandshake()>";
	
	
	enum Var {SOCKET};
	enum State {INIT_SOCKET,SET_CIPHERSUITES};
	enum StmtID {}
	
	@Override
	protected Done<Var, State, StmtID> atCallToReturn(AtCallToReturn<Var, State, StmtID> d) {
		return d.atCallTo(INVOKE_CREATESOCKET).always().trackReturnValue().as(SOCKET).toState(INIT_SOCKET)
				.orElse().atCallTo(INVOKE_SET_CIPHERSUITES).ifValueBoundTo(SOCKET).equalsThis().toState(SET_CIPHERSUITES)
				.orElse().atCallTo(START_HANDSHAKE).ifValueBoundTo(SOCKET).equalsThis().and().ifInState(INIT_SOCKET).reportError(ERROR_MESSAGE_1).here();
	}

	@Override 
	protected Done<Var, State, StmtID> atReturn(AtReturn<Var, State, StmtID> d) {
		return null;
	}

	@Override
	protected Done<Var, State, StmtID> atNormalEdge(AtNormalEdge<Var, State, StmtID> d) {
		return null;
	}
	

}
