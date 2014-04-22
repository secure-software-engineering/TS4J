package servletexample;


public class Servlet {
	public void doGet(HttpServletRequest request){
		int nece = 10;
		nece++;
		String user = request.getParam("test");
		
		ServletResponse resp = new ServletResponse();
		resp.addHeader(user, user);
	}
}
