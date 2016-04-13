import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class Boxplot
{

	public static void main(String[] args) throws IOException, SQLException
	{
		PrintWriter output = new PrintWriter("Boxplot.txt");
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Statement stmt = con.createStatement();
        
        ArrayList<Integer> ages = new ArrayList<Integer>();
        
        String myQuery = "SELECT age FROM census";
        ResultSet rs = stmt.executeQuery(myQuery);
        while(rs.next())
        {
        	int a = rs.getInt(1);
        	ages.add(a);
        }
        
        Collections.sort(ages);
        int n = ages.size();
        int iq1 = (int) n/4;
        int q1 = ages.get(iq1);
        int iq3 = n - iq1;
        int q3 = ages.get(iq3);
        int outMin = (int)(q3 + (1.5 * (q3-q1)));
        int outMax = (int)(q3 - (1.5 * (q3-q1)));
        
        myQuery = "SELECT * FROM census";
        rs = stmt.executeQuery(myQuery);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while(rs.next())
        {
        	int a = rs.getInt(2);
        	if(a > outMin || a < outMax)
        	{
        		for(int i = 1; i <= columnsNumber; i++)
        			output.print(rs.getObject(i) + ",");
        		output.println();
        	}
        }
        
        con.close();
        output.close();
	}

}
