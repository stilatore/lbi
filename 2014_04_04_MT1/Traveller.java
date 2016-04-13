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

public class Traveller
{

	public static void main(String[] args) throws IOException, SQLException
	{
		PrintWriter output = new PrintWriter("Traveller.csv");
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Statement stmt = con.createStatement();
        
        HashMap<Integer,Customer> customers  = new HashMap<Integer,Customer>();
        HashMap<Integer,String> stores = new HashMap<Integer,String>();
        HashMap<Integer,Sales> sales = new HashMap<Integer,Sales>();
        
        String myQuery = "SELECT customer_id, lname, fname, city FROM customer";
        ResultSet rs = stmt.executeQuery(myQuery);
        while(rs.next())
        	customers.put(rs.getInt(1), new Customer(rs.getString(2) + " " + rs.getString(3),rs.getString(4)));
        
        
        myQuery = "SELECT store_id, store_city FROM store";
        rs = stmt.executeQuery(myQuery);
        while(rs.next())
        	stores.put(rs.getInt(1), rs.getString(2));
        
        
        myQuery = "SELECT customer_id, store_id, store_sales FROM sales_fact_1998";
        rs = stmt.executeQuery(myQuery);
        while(rs.next())
        {
        	int cust_id = rs.getInt(1);
        	int store_id = rs.getInt(2);
        	double sale = rs.getDouble(3);
        	
        	String cust_city = customers.get(cust_id).getCity();
        	String store_city = stores.get(store_id);
        	
        	if(!sales.containsKey(cust_id))
        		sales.put(cust_id, new Sales(new Customer(customers.get(cust_id).getName(),cust_city)));
        	
        	if(!cust_city.equals(store_city))
        		sales.get(cust_id).updateTravel(sale);
        	sales.get(cust_id).updateTotal(sale);
        }
        
        for(Sales s : sales.values())
        	output.println(s.getCustName() + "," + s.getTotal() + "," + (s.getTravel()/s.getTotal()));
        
        output.close();
        con.close();
        
	}

}

class Customer
{
	private String name;
	private String city;
	
	public Customer(String n, String c)
	{
		this.name = n;
		this.city = c;
	}
	
	public void setName(String n)
	{
		this.name = n;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setCity(String c)
	{
		this.city = c;
	}
	
	public String getCity()
	{
		return this.city;
	}
}

class Sales
{
	private Customer cust;
	private double travel_sales;
	private double total_sales;
	
	public Sales(Customer c)
	{
		this.cust = c;
		travel_sales = 0.0;
		total_sales = 0.0;
	}
	
	public String getCustName()
	{
		return this.cust.getName();
	}
	
	public double getTravel()
	{
		return this.travel_sales;
	}
	
	public double getTotal()
	{
		return this.total_sales;
	}
	
	public void updateTravel(double d)
	{
		this.travel_sales += d;
	}
	
	public void updateTotal(double d)
	{
		this.total_sales += d;
	}
}
