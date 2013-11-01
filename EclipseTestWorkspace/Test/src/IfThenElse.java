import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class IfThenElse extends WebViewClient {
	
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		if(System.currentTimeMillis()>0)
			handler.proceed();
	}

}
