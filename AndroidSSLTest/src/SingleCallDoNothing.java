import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class SingleCallDoNothing extends WebViewClient {
	
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		doNothing(handler);
		handler.proceed();
	}

	private void doNothing(SslErrorHandler handler) {
	}

}
