import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class FPW
{

	public static void main(String[] args) throws IOException, SQLException
	{
		PrintWriter output = new PrintWriter("FPW.txt");
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Statement stmt = con.createStatement();
        
        HashMap<Integer,HashSet<String>> cust2fpw = new HashMap<Integer,HashSet<String>>();
        
        String myQuery = "SELECT customer_id, s.time_id "
        		+ "FROM sales_fact_1998 s JOIN time_by_day t ON s.time_id = t.time_id "
        		+ "WHERE the_day IN ('Saturday','Sunday') "
        		+ "ORDER BY customer_id";
        ResultSet rs = stmt.executeQuery(myQuery);
        while(rs.next())
        {
        	int c_id = rs.getInt(1);
        	int t_id = rs.getInt(2);
        	
        	if(cust2fpw.containsKey(c_id))
        	{
        		HashSet<String> temp = cust2fpw.remove(c_id);
        		temp.add(c_id + "," + t_id);
        		cust2fpw.put(c_id, temp);
        	}
        	else
        		cust2fpw.put(c_id, new HashSet<String>(){{add(c_id + "," + t_id);}});
        }
        con.close();
        
        for(Integer i : cust2fpw.keySet())
        	output.println(i + "," + cust2fpw.get(i).size());
        output.close();
	}

}
