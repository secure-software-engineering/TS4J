import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class JustProceed extends WebViewClient {
	
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		handler.proceed();
	}

}
