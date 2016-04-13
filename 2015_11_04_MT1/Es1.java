import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeMap;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class Deviation
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
        
        HashMap<Integer,Double> time_sales = new HashMap<Integer,Double>();
        double totalsales = 0;
        int ndays = 0;
        double avgsales = 0;
        TreeMap<Integer,Double> time2devsales = new TreeMap<Integer,Double>();
        
        String myQuery = "SELECT * FROM sales_fact";
        ResultSet rs = stmt.executeQuery(myQuery);
        while(rs.next())
        {
        	int time_id = rs.getInt(3);
        	double store_sales = rs.getDouble(7);
        	totalsales += store_sales;
        	if(time_sales.containsKey(time_id))
        	{
        		double temp = time_sales.remove(time_id);
        		time_sales.put(time_id, temp + store_sales);
        	}
        	else
        		time_sales.put(time_id, store_sales);
        }
        con.close();
        ndays = time_sales.size();
        avgsales = totalsales/ndays;
        
        for(Integer i : time_sales.keySet())
        {
        	double sales = time_sales.get(i);
        	double dev = sales - avgsales;
        	time2devsales.put(i, dev);
        }
        
        double max_sum = 0;
        double sum = 0;
        int start_time = 0;
        int end_time = 0;
        for(Integer i : time2devsales.keySet())
        {
        	double val = time2devsales.get(i);
        	if(sum > 0)
        		sum += val;
        	else
            {
                sum = val;
                start_time = i;
            }
        	if(sum > max_sum)
        	{
        		max_sum = sum;
        		end_time = i;
        	}
        }
        
        System.out.println("start_time: " + start_time + "\n" + "end_time: " + end_time + "\n" + "maxsum: " + max_sum);
        
	}

}
