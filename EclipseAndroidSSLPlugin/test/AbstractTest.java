import java.util.ArrayList;
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

	protected static final String SUPER_CLASS = "android.webkit.WebViewClient";
	protected static final String SUB_SIG = "void onReceivedSslError(android.webkit.WebView,android.webkit.SslErrorHandler,android.net.http.SslError)";
	protected Set<String> actual, expected;

	protected final void tearDown() throws Exception {
		G.reset();
		super.tearDown();
	}

	protected void run(String... files) throws InterruptedException{

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

		
		
		Map<AnalysisConfiguration, Set<ErrorMarker>> results= job.getResults();
		for (Entry<AnalysisConfiguration, Set<ErrorMarker>> analysisAndErrorMarkers : results.entrySet()) {
			//AnalysisConfiguration analysisConfiguration = analysisAndErrorMarkers.getKey();
			Set<ErrorMarker> errorMarkers = analysisAndErrorMarkers.getValue();
			for (ErrorMarker errorMarker : errorMarkers) {
			}
		}
	}

}
