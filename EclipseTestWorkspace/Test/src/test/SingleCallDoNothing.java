package test;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class SingleCallDoNothing extends WebViewClient {
	
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		doNothing(handler);
	}

	private void doNothing(SslErrorHandler handler) {
	}

}
