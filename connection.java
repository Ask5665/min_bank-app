package bankingApp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connection {


	static Connection con;

	public static Connection getConnection() {

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url="jdbc:mysql://localhost:3306/bank";
			String pass="root";
			String user="root";
			con=DriverManager.getConnection(url,user,pass);
			System.out.println("Connection is Succesfully Established");
		}
		catch(SQLException e) {
			System.out.println("Error "+e.getMessage());
		}
		catch(ClassNotFoundException e) {
			System.out.println("Error "+e.getMessage());
		}

		return con;
	}
	
//	public static void main(String[] agrs)
//	{
//		getConnection();
//	}
}

