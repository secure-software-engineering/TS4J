public class SingleCallDoProceed {
	
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		doProceed(handler);
	}

	private void doProceed(SslErrorHandler handler) {
		handler.proceed();
	}

}
