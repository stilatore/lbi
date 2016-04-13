import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class TCLose
{

	public static void main(String[] args) throws IOException, SQLException
	{
		if(args.length != 5)
		{
			System.out.println("Usage: java TClose table t A B C");
			System.exit(0);
		}
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Statement stmt = con.createStatement();
        
        String table = args[0];
        int t = Integer.parseInt(args[1]);
        String colA = args[2];
        String colB = args[3];
        String colC = args[4];
        
        ArrayList<String> atts = new ArrayList<String>();
        int n1,n2,p1,p2;
        HashMap<String,Integer> bin2perc = new HashMap<String,Integer>();
        int tot = 0;
        HashMap<String,ArrayList<Bin2Perc>> tuple = new HashMap<String,ArrayList<Bin2Perc>>();
                
        String myQuery = "SELECT " + colC + ", COUNT(*) as N FROM " + table + " GROUP BY " + colC;
        ResultSet rs = stmt.executeQuery(myQuery);       
        while(rs.next())
        {
        	String name = rs.getString(1);
        	atts.add(name);
        	int n = rs.getInt(2);
        	tot += n;
        	bin2perc.put(name,n);
        }
        for(String b : bin2perc.keySet())
        	bin2perc.put(b, (bin2perc.get(b)*100)/tot);
             
                
        myQuery = "SELECT " + colA + ", " + colB + ", " + colC + ", COUNT(*) as k "
        		+ "FROM " + table + " GROUP BY " + colA + ", " + colB + ", " + colC + " ORDER BY " + colA + ", " + colB;
        rs = stmt.executeQuery(myQuery);
        while(rs.next())
        {
        	String ab = rs.getString(1) + "," + rs.getString(2);
        	String c = rs.getString(3);
        	int n = rs.getInt(4);
        	if(tuple.containsKey(ab))
        		tuple.get(ab).add(new Bin2Perc(c,n));
        	else
        		tuple.put(ab, new ArrayList<Bin2Perc>(){{add(new Bin2Perc(c,n));}});
        }
        boolean tclose = true;
        for(String s : tuple.keySet())
        {
        	String class1 = tuple.get(s).get(0).getName();
        	int num1 = tuple.get(s).get(0).getN();
        	String class2 = (class1 == atts.get(1)) ? atts.get(2) : atts.get(1);
        	int num2 = 0;
        	try
        	{
        		num2 = tuple.get(s).get(1).getN();
        	}
        	catch(IndexOutOfBoundsException e)
        	{
        		num2 = 0;
        	}
        	int perc1 = (num1*100)/(num1+num2);
        	int perc2 = 100 - perc1;
        	if(perc1 < bin2perc.get(class1) - t || perc1 > bin2perc.get(class1) + t)
        	{
        		System.out.println("no");
        		tclose = false;
        		break;
        	}
        	if(perc2 < bin2perc.get(class2) - t || perc2 > bin2perc.get(class2) + t)
        	{
        		System.out.println("no");
        		tclose = false;
        		break;
        	}
        }
        if(tclose)
        	System.out.println("yes");
        con.close();
        
	}
}

class Bin2Perc
{
	private String attr;
	private int n;
	
	public Bin2Perc(String attr, int n)
	{
		this.attr = attr;
		this.n = n;
	}
	
	public String getName()
	{
		return this.attr;
	}
	
	public int getN()
	{
		return this.n;
	}
}
