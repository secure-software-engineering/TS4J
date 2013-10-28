public class VirtualDispatchAllProceed {
	
	abstract class A {
		abstract void doProceed(SslErrorHandler handler);
	}
	
	class SubDoesProceed extends A {
		public void doProceed(SslErrorHandler handler) {
			handler.proceed();
		}
	}
	
	class SubDoesProceed2 extends A {
		public void doProceed(SslErrorHandler handler) {
			handler.proceed();
		}
	}

	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		A a = System.currentTimeMillis()>0 ? new SubDoesProceed() : new SubDoesProceed2();
		a.doProceed(handler);
	}

}
