import org.junit.Test;


public class AllTests extends AbstractTest {

	@Test
	public void testIfThenElse() {
		expectAsVulnerable();
		run("IfThenElse");
	}

	@Test
	public void testJustProceed() {
		expectAsVulnerable("JustProceed");
		run("JustProceed");
	}

	@Test
	public void testSingleCallDoNothing() {
		expectAsVulnerable();
		run("SingleCallDoNothing");
	}

	@Test
	public void testSingleCallDoProceed() {
		expectAsVulnerable("SingleCallDoProceed");
		run("SingleCallDoProceed");
	}

	@Test
	public void testVirtualDispatch() {
		expectAsVulnerable();
		run("VirtualDispatch");
	}

	@Test
	public void testVirtualDispatchAllProceed() {
		expectAsVulnerable("VirtualDispatchAllProceed");
		run("VirtualDispatchAllProceed");
	}
}
