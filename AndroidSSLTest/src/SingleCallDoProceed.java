import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SingleCallDoProceed extends WebViewClient {
	
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		doProceed(handler);
	}

	private void doProceed(SslErrorHandler handler) {
		handler.proceed();
	}

}
