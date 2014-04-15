package servletexample;


public class Servlet {
	public void doGet(HttpServletRequest request){
		int nece = 10;
		nece++;
		String user = request.getParam("jaas");
		
		ServletResponse resp = new ServletResponse();
		resp.addHeader("here",user);
	}
	


}
