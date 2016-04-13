import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class MPD_
{
	public static void main(String[] args) throws IOException, SQLException
	{
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Statement stmt = con.createStatement();
        
        HashMap<Integer,String> customers = new HashMap<Integer,String>();
        HashMap<Integer,String> time = new HashMap<Integer,String>();
        HashMap<String,Integer> nonsun = new HashMap<String,Integer>();
        HashMap<String,Double> sales = new HashMap<String,Double>();
        
        //CUSTOMER TABLE
        String queryCustomers = "SELECT * FROM customer";
        ResultSet rs = stmt.executeQuery(queryCustomers);
        while(rs.next())
        	customers.put(rs.getInt("customer_id"), rs.getString("lname") + " " + rs.getString("fname"));
        
        //TIME BY DAY TABLE + NONSUNDAY
        String queryTime = "SELECT * FROM time_by_day";
        rs = stmt.executeQuery(queryTime);
        while(rs.next())
        {
        	time.put(rs.getInt("time_id"), rs.getString("the_month") + " " + rs.getInt("the_year"));
        	if(!rs.getString("the_day").equals("Sunday"))
        	{
        		if(!nonsun.containsKey(rs.getString("the_month") + " " + rs.getInt("the_year")))
        			nonsun.put(rs.getString("the_month") + " " + rs.getInt("the_year"), 1);
        		else
        		{
        			int count;
        			count = nonsun.remove(rs.getString("the_month") + " " + rs.getInt("the_year"));
        			nonsun.put(rs.getString("the_month") + " " + rs.getInt("the_year"), ++count);
        		}
        	}
        }
        
        //customer_id, the_year, the_month, sum(store_sales)
        String queryFact = "SELECT * FROM sales_fact";
        rs = stmt.executeQuery(queryFact);
        while(rs.next())
        {
        	String key = rs.getInt("customer_id") + " " + time.get(rs.getInt("time_id"));
        	if(sales.containsKey(key))
        	{
        		Double temp = sales.get(key) + rs.getDouble("store_sales");
        		sales.put(key, temp);
        	}
        	else
        		sales.put(key, rs.getDouble("store_sales"));
        }
        
        
        for(String s : sales.keySet())
        {
        	double MPD = sales.get(s) / nonsun.get(s.substring(s.indexOf(" ")+1, s.length()));
        	System.out.println(customers.get(Integer.parseInt(s.substring(0, s.indexOf(" ")))) + " " + sales.get(s) + " " + nonsun.get(s.substring(s.indexOf(" ")+1, s.length())) + " " + MPD);
        }
	}
}
