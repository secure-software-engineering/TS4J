package test;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import annotation.DefinitelyVulnerable;


public class ExceptionHandling extends WebViewClient {
	
	//here we want to flag an error because we don't want to rely on exceptions being thrown
	//as the only means to be secure
	@DefinitelyVulnerable
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		try {
			hashCode();	//some arbitrary virtual call that might throw an exception		
			handler.proceed();
		} catch(Throwable t) {			
		}
	}

}
