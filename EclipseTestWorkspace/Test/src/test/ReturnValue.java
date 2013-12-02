package test;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import annotation.FalseNegative;


public class ReturnValue extends WebViewClient {
	
	//We currently cannot identify this case for the same reason we cannot handle the Aliasing
	//test case: after the id-call, copy and handler are aliases but the proceed-call kills only one of them
	@FalseNegative	
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		SslErrorHandler copy = id(handler);
		copy.proceed();
	}

	private SslErrorHandler id(SslErrorHandler h) {
		return h;
	}

}
