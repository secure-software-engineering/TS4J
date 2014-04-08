import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Timer;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;

import soot.G;

import com.google.common.base.Joiner;

import de.fraunhofer.sit.codescan.framework.internal.analysis.AnalysisDispatcher;

public class AbstractTest extends TestCase {

	protected static final String SUPER_CLASS = "android.webkit.WebViewClient";
	protected static final String SUB_SIG = "void onReceivedSslError(android.webkit.WebView,android.webkit.SslErrorHandler,android.net.http.SslError)";
	protected Set<String> actual, expected;

	protected final void tearDown() throws Exception {
		G.reset();
		super.tearDown();
	}

	@SuppressWarnings("deprecation")
	protected void run(String... files) throws CoreException {
		String args = "-f none -p cg all-reachable:true -no-bodies-for-excluded -w -pp -cp . "
				+ Joiner.on(" ").join(Arrays.asList(files));
		String[] argsArray = args.split(" ");
		
		IProject project = mock(IProject.class);        
		final IProjectDescription description = mock(IProjectDescription.class);
		when(project.getType()).thenReturn(project.PROJECT);
		when(project.getDescription()).thenReturn(description);
		
		//when(project.hasNature(PMDNature.ID)).thenReturn(false);
	        when(description.getNatureIds()).thenReturn(new String[] {});
		ArrayList<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
			IJavaProject javaProject = JavaCore.create(project);
			javaProjects.add(javaProject);
		IJavaProject[] javaProjectArray = javaProjects
				.toArray(new IJavaProject[0]);

		AnalysisDispatcher.searchAndAnalyze(javaProjectArray);
	}


}
