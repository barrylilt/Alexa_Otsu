package otsuka;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class ConnectionUtil {

	private static final String driver = "com.mysql.jdbc.Driver";
 
	private static final String connectionURL = "jdbc:mysql://clinicaltrials1.coh9pufwidlw.us-east-1.rds.amazonaws.com:3306/Saama_CT1";

	private static final String username = "Saama1";

	private static final String password = "Saama!2345";

	private Connection con = null;

	
	private  Connection getDBConnection() {
		if (con == null) {

			try {
				Class.forName(driver);
				
				con = DriverManager.getConnection(connectionURL, username, password);
			} catch (SQLException e) {
				
				e.printStackTrace();
				
			} catch (ClassNotFoundException e) {
				
				e.printStackTrace();
			}
		}
		return con;
	}

	
	public ConnectionUtil(){
		con = getDBConnection();
	}
	
	public  String executeQuery(String sql) {
		String res = "0.0";
	
		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				res = rs.getString(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}

	/*public  void closeConnection(){
		if(con != null){
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}*/
	
	
}
