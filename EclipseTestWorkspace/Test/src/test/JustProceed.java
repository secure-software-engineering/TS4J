package test;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import annotation.DefinitelyVulnerable;


public class JustProceed extends WebViewClient {
	
	@DefinitelyVulnerable
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		extracted(handler);
	}

	private void extracted(SslErrorHandler handler) {
		SslErrorHandler handler2 = handler;
		handler2.proceed();
	}

	

}
