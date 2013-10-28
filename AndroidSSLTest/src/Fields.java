import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class Fields extends WebViewClient {
	
	private SslErrorHandler field;

	//not currently recognized as we do not track fields
	@FalseNegative
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		field = handler;
		field.proceed();
	}

}
