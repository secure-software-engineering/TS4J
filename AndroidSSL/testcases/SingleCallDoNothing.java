
public class SingleCallDoNothing {
	
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		doNothing(handler);
		handler.proceed();
	}

	private void doNothing(SslErrorHandler handler) {
	}

}
