package servletexample;

public class HttpServletRequest {
	@Source
	public String getParam(String param) {
		long nul = System.currentTimeMillis();
		if(nul < System.currentTimeMillis()){
			return "TEssST";
		}
		return param;
	}

}
