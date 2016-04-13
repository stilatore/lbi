import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class ValuableDay
{

	public static void main(String[] args) throws IOException, SQLException
	{
		PrintWriter output = new PrintWriter("ValuableDay.txt");
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Statement stmt = con.createStatement();
        
        HashMap<CustDay,Double> custday2sale = new HashMap<CustDay,Double>();
        HashMap<Integer,Double> cust2max = new HashMap<Integer,Double>();
        
        String myQuery = "SELECT customer_id, the_day, store_sales "
        		+ "FROM sales_fact_1998 s JOIN time_by_day t ON s.time_id = t.time_id";
        ResultSet rs = stmt.executeQuery(myQuery);
        while(rs.next())
        {
        	int c_id = rs.getInt(1);
        	String day = rs.getString(2);
        	double sales = rs.getDouble(3);
        	CustDay cd = new CustDay(c_id,day);        	
        	if(custday2sale.containsKey(cd))
        	{
        		double temp = custday2sale.remove(cd);
        		custday2sale.put(cd, temp+sales);
        	}
        	else
        		custday2sale.put(cd,sales);
        }
        con.close();
        
        for(CustDay cd : custday2sale.keySet())
        {
        	double s = custday2sale.get(cd);
        	if(cust2max.containsKey(cd.getId()))
        	{
        		if(s > cust2max.get(cd.getId()))
        			cust2max.put(cd.getId(), s);
        	}
        	else
        		cust2max.put(cd.getId(), s);
        }
        
        for(Integer i : cust2max.keySet())
        	for(CustDay cd : custday2sale.keySet())
        	{
        		double s = custday2sale.get(cd);
        		if(i == cd.getId() && cust2max.get(i) == s)
        			output.println(i + "  " + cd.getDay() + "  " + cust2max.get(i));
        	}
        
        output.close();
	}

}

class CustDay
{
	private int id;
	private String day;
	
	public CustDay(int i, String d)
	{
		this.id = i;
		this.day = d;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public String getDay()
	{
		return this.day;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null)
			return false;
		if(o.getClass() != this.getClass())
			return false;
		final CustDay c = (CustDay) o;
		if(this.id != c.id)
			return false;
		if(!this.day.equals(c.day) ) 
			return false;
		return true;
	}
	@Override
	public int hashCode()
	{
		return this.id;
	}
}
