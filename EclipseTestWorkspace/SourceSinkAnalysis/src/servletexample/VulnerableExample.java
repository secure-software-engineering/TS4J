package servletexample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class VulnerableExample {
	public String getUserName(HttpServletRequest request){
		String username = request.getParameter("test");
		
		return username;
	}
	public void updateCoffeeSales(HashMap<String, Integer> salesForWeek, HttpServletRequest request)
		    throws SQLException, ClassNotFoundException {
			String username = getUserName( request);
			Connection con = DriverManager.getConnection( "test","test","test" );
		    PreparedStatement updateSales = null;
		    PreparedStatement updateTotal = null;

		    String updateString =
		        "update db.COFFEES " +
		        "set SALES = "+username +"  where COF_NAME = ?";

		    String updateStatement =
		        "update db.COFFEES " +
		        "set TOTAL = TOTAL + ? " +
		        "where COF_NAME = ?";

		    try {
		        con.setAutoCommit(false);
		        updateSales = con.prepareStatement(updateString);
		        updateTotal = con.prepareStatement(updateStatement);

		        for (Map.Entry<String, Integer> e : salesForWeek.entrySet()) {
		            updateSales.setInt(1, e.getValue().intValue());
		            updateSales.setString(2, e.getKey());
		            updateSales.executeUpdate();
		            updateTotal.setInt(1, e.getValue().intValue());
		            updateTotal.setString(2, e.getKey());
		            updateTotal.executeUpdate();
		            con.commit();
		        }
		    } catch (SQLException e ) {
		       
		    } 
		}
}
	