package test;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import annotation.DefinitelyVulnerable;


public class Aliasing extends WebViewClient {
	
	//This is currently not being recognized as vulnerable, as the "handler" variable
	//reaches the end of the method without seeing a proceed()-call on it.
	//The analysis does not recognize that handler and handler2 must be the same object.
	@DefinitelyVulnerable
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		SslErrorHandler handler2 = handler;
		handler2.proceed();
	}

}
