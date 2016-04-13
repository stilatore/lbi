import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class Rollup
{

	public static void main(String[] args) throws IOException, SQLException
	{
		PrintWriter output = new PrintWriter("Rollup.csv");
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Statement stmt = con.createStatement();
        
        String col = args[0];
        String cols[] = col.split(",");
        int ncols = cols.length;
        
        String myQuery = "SELECT " + col + ",COUNT(*) as total FROM sales_fact GROUP BY " + col;
        for(int i = ncols+1; i > 1;)
        {
        	System.out.println(myQuery);
        	ResultSet rs = stmt.executeQuery(myQuery);
        	while(rs.next())
            {
            	for(int j = 1; j <= ncols; j++)
            		output.print(rs.getObject(j) + ",");
            	output.println(rs.getObject(ncols+1));
            }
        	--i;
        	myQuery = "SELECT ";
        	for(int k = 1; k <= ncols; k++)
        		myQuery += (k < i)? (cols[k-1] + ",") : "null,";
        	myQuery += "COUNT(*) as total FROM sales_fact GROUP BY ";
        	for(int k = 1; k <= ncols; k++)
        		myQuery += (k < i)? (cols[k-1] + ",") : "";
        	myQuery = myQuery.substring(0, myQuery.length()-1);
        }
        
        
        
        output.close();
        con.close();
	}

}
