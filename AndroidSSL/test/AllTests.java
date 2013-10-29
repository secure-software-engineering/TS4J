import org.junit.Test;


public class AllTests extends AbstractTest {

	@Test
	public void testAliasing() {
		run("Aliasing");
	}
	
	@Test
	public void testExceptions() {
		run("ExceptionHandling");
	}
	
	@Test
	public void testFields() {
		run("Fields");
	}
	
	@Test
	public void testIfThenElse() {
		run("IfThenElse");
	}

	@Test
	public void testJustProceed() {
		run("JustProceed");
	}

	@Test
	public void testReturnValue() {
		run("ReturnValue");
	}
	
	@Test
	public void testSingleCallDoNothing() {
		run("SingleCallDoNothing");
	}

	@Test
	public void testSingleCallDoProceed() {
		run("SingleCallDoProceed");
	}

	@Test
	public void testVirtualDispatch() {
		run("VirtualDispatch");
	}

	@Test
	public void testVirtualDispatchAllProceed() {
		run("VirtualDispatchAllProceed");
	}
}
