
public class JustProceed {
	
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		handler.proceed();
	}

}
