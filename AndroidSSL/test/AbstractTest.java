import static de.ecspride.sslanalysis.Main.SUBSIG;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Assert;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;

import com.google.common.base.Joiner;

import de.ecspride.sslanalysis.Main;
import de.ecspride.sslanalysis.VulnerableMethodTag;


public class AbstractTest extends TestCase {

	protected Set<String> actual, expected;
	
	protected final void setUp() throws Exception {
		actual = new HashSet<String>();
		expected = new HashSet<String>();
		PackManager.v().getPack("wjap").add(new Transform("wjap.sslanalysis.reporter", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				for(SootClass c: Scene.v().getApplicationClasses()) {
					if(!Scene.v().getFastHierarchy().isSubclass(c, Scene.v().getSootClass("android.webkit.WebViewClient"))) continue;
					if(!c.declaresMethod(SUBSIG)) continue;

					SootMethod m = c.getMethod(SUBSIG);

					if(m.hasTag(VulnerableMethodTag.class.getName())) {
						actual.add(m.getDeclaringClass().getName());
					}
				}
			}

		}));
		super.setUp();
	}

	protected final void tearDown() throws Exception {
		G.reset();
		super.tearDown();
		Assert.assertEquals(expected, actual);
	}
	
	protected void expectAsVulnerable(String ... expected) {
		this.expected.addAll(Arrays.asList(expected));
	}
	
	protected void run(String ... files) {
		String args = "-f none -p cg all-reachable:true -no-bodies-for-excluded -w -pp -cp . "+Joiner.on(" ").join(Arrays.asList(files));
		String[] argsArray = args.split(" ");
		Main.main(argsArray);		
	}

}
