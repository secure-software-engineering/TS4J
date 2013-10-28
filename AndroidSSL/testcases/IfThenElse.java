
public class IfThenElse {
	
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		if(System.currentTimeMillis()>0)
			handler.proceed();
	}

}
