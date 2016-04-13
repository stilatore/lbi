import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class Alive
{

	public static void main(String[] args) throws IOException, SQLException
	{	
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String urlDest = props.getProperty("jdbcDestURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Connection conDest = DriverManager.getConnection(urlDest, user, pwd);
        Statement stmt = con.createStatement();
        Statement stmtDest = conDest.createStatement();
        
        String myQuery = "WITH v1 AS( "
        				+ "SELECT customer_id, MAX(month_of_year) AS max_month "
        				+ "FROM sales_fact s JOIN time_by_day t ON s.time_id = t.time_id "
        				+ "GROUP BY customer_id ) "
        				+ "SELECT customer_id, the_month "
        				+ "FROM v1 RIGHT JOIN time_by_day ON month_of_year <= max_month "
        				+ "GROUP BY customer_id, month_of_year, the_month "
        				+ "ORDER BY customer_id, month_of_year";
        
        String createQuery = "CREATE TABLE [Alive_JAVA] ([customer_id] int,[the_month] nvarchar(15))";
        stmtDest.executeUpdate(createQuery);
        
        ResultSet rs = stmt.executeQuery(myQuery);
        while(rs.next())
        {
        	stmtDest.executeUpdate("INSERT INTO Alive_JAVA VALUES (" + rs.getInt(1) + ", '" + rs.getString(2) + "')");
        	//System.out.println(rs.getInt(1) + " " + rs.getString(2));
        }
        
        con.close();
        conDest.close();
	}

}
