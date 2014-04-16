package de.fraunhofer.sit.codescan.plugintests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import soot.G;
import de.fraunhofer.sit.codescan.framework.AnalysisConfiguration;
import de.fraunhofer.sit.codescan.framework.internal.analysis.AnalysisDispatcher;
import de.fraunhofer.sit.codescan.framework.internal.analysis.AnalysisJob;
import de.fraunhofer.sit.codescan.sootbridge.ErrorMarker;

public class AbstractTest extends TestCase {

	protected HashMap<String, Set<String>> analysisToErrorMethodSignatures;
	protected HashMap<String, Map<String, Set<String>>> expectedAnalaysisClassToErrorTypes;

	protected final void tearDown() throws Exception {
		G.reset();
		super.tearDown();
	}

	public void analysis() throws InterruptedException {

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		ArrayList<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
		for (IProject p : projects) {
			IJavaProject javaProject = JavaCore.create(p);
			javaProjects.add(javaProject);
		}
		IJavaProject[] javaProjectArray = javaProjects
				.toArray(new IJavaProject[0]);
		AnalysisJob job = AnalysisDispatcher.searchAndAnalyze(javaProjectArray);

		job.join();

		Map<AnalysisConfiguration, Set<ErrorMarker>> results = job.getResults();
		analysisToErrorMethodSignatures = new HashMap<String, Set<String>>();
		for (Entry<AnalysisConfiguration, Set<ErrorMarker>> analysisAndErrorMarkers : results
				.entrySet()) {
			AnalysisConfiguration analysisConfiguration = analysisAndErrorMarkers
					.getKey();
			Set<ErrorMarker> errorMarkers = analysisAndErrorMarkers.getValue();

			Set<String> methodSignatureSet = new HashSet<String>();
			for (ErrorMarker errorMarker : errorMarkers) {
				methodSignatureSet.add(errorMarker.getMethodSignature());
			}
			String analysisClass = analysisConfiguration.getAnalysisClass();
			analysisToErrorMethodSignatures.put(analysisClass.substring(analysisClass.lastIndexOf(".")+1,analysisClass.length())
					,
					methodSignatureSet);
		}
		expectedAnalaysisClassToErrorTypes = ExpectedSceneTransformer
				.getExpected();
	}

	public void checkFalseNegatives(String analysisClass)
			throws InterruptedException {
		analysis();
		Set<String> foundErrorMethodSignatures = analysisToErrorMethodSignatures
				.get(analysisClass);
		Set<String> set = expectedAnalaysisClassToErrorTypes.get(analysisClass)
				.get("Lannotation/FalseNegative;");
		if (set != null) {
			for (String s : set) {
				assertFalse(foundErrorMethodSignatures.contains(s));
			}
		}
		// analysisToErrorMethodSignatures
	}

	public void checkDefinitvlyVulnerable(String analysisClass)
			throws InterruptedException {
		analysis();
		Set<String> foundErrorMethodSignatures = analysisToErrorMethodSignatures
				.get(analysisClass);
		assertEquals(
				foundErrorMethodSignatures,
				expectedAnalaysisClassToErrorTypes.get(analysisClass).get(
						"Lannotation/DefinitelyVulnerable;"));
	}
}
