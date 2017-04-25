package de.fraunhofer.sit.codescan.typestate.enabledprotocolanalysis;

import static de.fraunhofer.sit.codescan.typestate.enabledprotocolanalysis.EnabledProtocolAnalysisProblem.State.INIT_SOCKET;
import static de.fraunhofer.sit.codescan.typestate.enabledprotocolanalysis.EnabledProtocolAnalysisProblem.State.SET_PROTOCOLS;
import static de.fraunhofer.sit.codescan.typestate.enabledprotocolanalysis.EnabledProtocolAnalysisProblem.Var.SOCKET;

import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateForwardAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.typestate.enabledprotocolanalysis.EnabledProtocolAnalysisProblem.State;
import de.fraunhofer.sit.codescan.typestate.enabledprotocolanalysis.EnabledProtocolAnalysisProblem.StmtID;
import de.fraunhofer.sit.codescan.typestate.enabledprotocolanalysis.EnabledProtocolAnalysisProblem.Var;


public class EnabledProtocolAnalysisProblem extends AbstractJimpleTypestateForwardAnalysisProblem<Var, State, StmtID>
{

	public EnabledProtocolAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	
	private static final String ERROR_MESSAGE_1 = "setEnabledProtocols(String[]) isn't used.";

	//CORRECT INVOKES
	
	private static final String INVOKE_CREATESOCKET = "<javax.net.ssl.SSLSocketFactory: java.net.Socket createSocket(java.lang.String,int)>";
	private static final String INVOKE_SET_PROTOCOLS = "<javax.net.ssl.SSLSocket: void setEnabledProtocols(java.lang.String[])>";	
	private static final String START_HANDSHAKE = "<javax.net.ssl.SSLSocket: void startHandshake()>";
	
	
	enum Var {SOCKET};
	enum State {INIT_SOCKET,SET_PROTOCOLS};
	enum StmtID {}
	
	@Override
	protected Done<Var, State, StmtID> atCallToReturn(AtCallToReturn<Var, State, StmtID> d) {
		return d.atCallTo(INVOKE_CREATESOCKET).always().trackReturnValue().as(SOCKET).toState(INIT_SOCKET)
				.orElse().atCallTo(INVOKE_SET_PROTOCOLS).ifValueBoundTo(SOCKET).equalsThis().toState(SET_PROTOCOLS)
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
