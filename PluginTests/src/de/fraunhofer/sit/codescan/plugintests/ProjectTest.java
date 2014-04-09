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
import org.junit.Test;

import soot.G;
import de.fraunhofer.sit.codescan.framework.AnalysisConfiguration;
import de.fraunhofer.sit.codescan.framework.internal.analysis.AnalysisDispatcher;
import de.fraunhofer.sit.codescan.framework.internal.analysis.AnalysisJob;
import de.fraunhofer.sit.codescan.sootbridge.ErrorMarker;

public class ProjectTest extends TestCase {

	protected final void tearDown() throws Exception {
		G.reset();
		super.tearDown();
	}
	
	@Test
	public void testCase() throws InterruptedException{

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

		Set<String> errors = new HashSet<String>();
		Map<AnalysisConfiguration, Set<ErrorMarker>> results= job.getResults();
		Map<String, Set<String>> analysisToErrorMethodSignatures= new HashMap<String, Set<String>>();
		for (Entry<AnalysisConfiguration, Set<ErrorMarker>> analysisAndErrorMarkers : results.entrySet()) {
			AnalysisConfiguration analysisConfiguration = analysisAndErrorMarkers.getKey();
			Set<ErrorMarker> errorMarkers = analysisAndErrorMarkers.getValue();

			Set<String> methodSignatureSet = new HashSet<String>();
			for (ErrorMarker errorMarker : errorMarkers) {
				methodSignatureSet.add(errorMarker.getMethodSignature());
			}
			analysisToErrorMethodSignatures.put(toAnnotationComparableString(analysisConfiguration.getAnalysisClass()), methodSignatureSet);
		}
		System.out.println(ExpectedSceneTransformer.getExpected());
		System.out.println(analysisToErrorMethodSignatures);
		assertEquals( analysisToErrorMethodSignatures, ExpectedSceneTransformer.getExpected() );
	}

	/** Through a Annotation @DefinatelyVulnerable(foo.bar.Plugin.class) we receive the class again via Lfoo/bar/Plugin; 
	 * This is what we do here. 	
	 * 
	 * @param analysisClass
	 * @return
	 */
	private String toAnnotationComparableString(String analysisClass) {

		return "L" + analysisClass.replace(".", "/") +";";
	}

}
