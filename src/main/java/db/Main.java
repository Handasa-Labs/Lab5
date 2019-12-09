package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.net.ssl.SSLException;

public class Main {
	static private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

	// update USER, PASS and DB URL according to credentials provided by the website:
	// https://remotemysql.com/
	// in future move these hard coded strings into separated config file or even better env variables
	static private final String DB = "RGx8X2ZYtW";
	static private final String DB_URL = "jdbc:mysql://remotemysql.com/"+ DB + "?useSSL=false";
	static private final String USER = "RGx8X2ZYtW";
	static private final String PASS = "IULy36aruU";

	public static void main(String[] args) throws SSLException {
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();
			
			System.out.println("\t============");

			// a 
			PreparedStatement update_flight = conn.prepareStatement("UPDATE flights SET price=? WHERE num=?");
			
			update_flight.setInt(1, 2019);
			update_flight.setInt(2, 387);
			update_flight.execute();
			
			// b
			ResultSet rs_ = stmt.executeQuery("SELECT price FROM flights WHERE num=387");
			while(rs_.next()) {
				int price_ = rs_.getInt("price");
				System.out.format("The new price of flight 387 is %5d\n", price_);
			}
			// c
			Statement my_stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet uprs = my_stmt.executeQuery("SELECT * FROM flights WHERE distance > 1000");
			while(uprs.next()) {
				uprs.updateInt("price", uprs.getInt("price")+100);
				uprs.updateRow();
			}
			uprs = my_stmt.executeQuery("SELECT * FROM flights WHERE price < 300");
			while(uprs.next()) {
				uprs.updateInt("price", uprs.getInt("price")-25);
				uprs.updateRow();
			}			
			
			
			
			// d
			PreparedStatement update_distant_flight_prices = conn.prepareStatement("UPDATE flights SET price=price+? WHERE distance > ?");
			PreparedStatement update_cheap_flight_prices = conn.prepareStatement("UPDATE flights SET price=price-? WHERE price < ?");

			update_distant_flight_prices.setInt(1, 100);
			update_distant_flight_prices.setInt(2, 1000);
			update_cheap_flight_prices.setInt(1, 25);
			update_cheap_flight_prices.setInt(2, 300);
			
			update_distant_flight_prices.execute();
			update_cheap_flight_prices.execute();

			//d
			String sql = "SELECT * FROM flights";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int num = rs.getInt("num");
				String origin = rs.getString("origin");
				String destination = rs.getString("destination");
				int distance = rs.getInt("distance");
				int price = rs.getInt("price");

				System.out.format("Number %5s Origin %15s destinations %18s Distance %5d Price %5d\n", num, origin, destination, distance, price);
			}

			System.out.println("\t============");

			sql = "SELECT origin, destination, distance, num FROM flights";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String origin = rs.getString("origin");
				String destination = rs.getString("destination");
				int distance = rs.getInt("distance");

				System.out.print("From: " + origin);
				System.out.print(",\tTo: " + destination);
				System.out.println(",\t\tDistance: " + distance);
			}

			System.out.println("\t============");
			
			sql = "SELECT origin, destination FROM flights WHERE distance > ?";
			PreparedStatement prep_stmt = conn.prepareStatement(sql);
			prep_stmt.setInt(1, 200);
			rs = prep_stmt.executeQuery();
			while (rs.next()) {
				String origin = rs.getString("origin");
				System.out.println("From: " + origin);
			}
			
			rs.close();
			stmt.close();
			conn.close();

		} catch (SQLException se) {
			se.printStackTrace();
			System.out.println("SQLException: " + se.getMessage());
            System.out.println("SQLState: " + se.getSQLState());
            System.out.println("VendorError: " + se.getErrorCode());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
}
