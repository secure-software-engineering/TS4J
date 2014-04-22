package de.fraunhofer.sit.codescan.sourcesinkanalysis;


import static de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.State.FOUND_SINK;
import static de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.State.SANITIZED;
import static de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.Var.INPUT_SINK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.io.Resources;

import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.sootbridge.util.SinkMethod;
import de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.State;
import de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.StatementId;
import de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.Var;

public class SourceToSinkAnalysisProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var,State,StatementId> {
	
	private static final String SANITIZER_ANNOTATION = "Lservletexample/Sanitizer;";
	private static final String SOURCE_ANNOTATION = "Lservletexample/Source;";
	private static final String SINK_ANNOTATION = "Lservletexample/Sink;";
	private HashSet<String> sources;
	private HashSet<SinkMethod> sinks;

	enum Var { INPUT_SINK};
	enum State { FOUND_SINK, SANITIZED };
	enum StatementId { }


	public SourceToSinkAnalysisProblem(IIFDSAnalysisContext context){
		super(context);
		getSinks();
		getSources();
		
	}

	private void getSinks() {
		sinks = new HashSet<SinkMethod>();
		try{

			Bundle bundle = FrameworkUtil.getBundle(getClass());
			URL url = bundle.getResource("sinks.txt");
			File file = new File(FileLocator.resolve(url).toURI());
			FileReader fr = new FileReader(file);
			BufferedReader read = new BufferedReader(fr);
			
			String line = "";
			while((line =  read.readLine()) != null){
				if(!line.equals(""))
					sinks.add(new SinkMethod(line));
			}
			read.close();
		} catch(Exception e){
			e.printStackTrace();
			//throw new RuntimeException("The file could not be accessed or opened");
		}
	}
	

	private void getSources() {
		sources = new HashSet<String>();
		try{

			Bundle bundle = FrameworkUtil.getBundle(getClass());
			URL url = bundle.getResource("sources.txt");
			File file = new File(FileLocator.resolve(url).toURI());
			FileReader fr = new FileReader(file);
			BufferedReader read = new BufferedReader(fr);
			
			String line = "";
			while((line =  read.readLine()) != null){
				if(!line.equals(""))
					sources.add(line);
			}
			read.close();
		} catch(Exception e){
			e.printStackTrace();
			//throw new RuntimeException("The file could not be accessed or opened");
		}
	}

	@Override
	protected Done<Var, State, StatementId> atCallToReturn(
			AtCallToReturn<Var, State, StatementId> d) {
		
		return d.atMethodFromListWithParameter(sinks).as(INPUT_SINK).toState(FOUND_SINK)
					.orElse().atMethodFromList(sources).ifValueBoundTo(INPUT_SINK).equalsReturnValue().and().ifInState(FOUND_SINK).reportError("NOTYETSANT").here()
					.orElse().atCallToMethodWithAnnotation(SANITIZER_ANNOTATION).ifValueBoundTo(INPUT_SINK).equalsReturnValue().toState(SANITIZED);
			
		//return d.atCallToMethodWithAnnotation(SINK_ANNOTATION).always().trackParameter(1).as(INPUT_SINK).toState(FOUND_SINK)
		//		.orElse().atCallToMethodWithAnnotation(SOURCE_ANNOTATION).ifValueBoundTo(INPUT_SINK).equalsReturnValue().and().ifInState(FOUND_SINK).reportError("NOTYETSANT").here()
			//	.orElse().atCallToMethodWithAnnotation(SANITIZER_ANNOTATION).ifValueBoundTo(INPUT_SINK).equalsReturnValue().toState(SANITIZED);
	}

	@Override
	protected Done<Var, State, StatementId> atReturn(
			AtReturn<Var, State, StatementId> d) {
		return	null;
	}

	@Override
	protected Done<Var, State, StatementId> atNormalEdge(
			AtNormalEdge<Var, State, StatementId> d) {
		return null;
	}


}
