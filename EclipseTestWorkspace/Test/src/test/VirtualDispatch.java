package test;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class VirtualDispatch extends WebViewClient {
	
	abstract class A {
		abstract void doProceed(SslErrorHandler handler);
	}
	
	class SubDoesProceed extends A {
		public void doProceed(SslErrorHandler handler) {
			handler.proceed();
		}
	}
	
	class SubNoProceed extends A {
		public void doProceed(SslErrorHandler handler) {
		}
	}

	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		A a = System.currentTimeMillis()>0 ? new SubDoesProceed() : new SubNoProceed();
		a.doProceed(handler);
	}

}
