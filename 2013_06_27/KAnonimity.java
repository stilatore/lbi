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

public class KAnonimity
{

	public static void main(String[] args) throws IOException, SQLException
	{
		if(args.length != 5)
		{
			System.out.println("Usage: java KAnonimity table k A B C");
			System.exit(0);
		}
		PrintWriter output = new PrintWriter("KAnonimity.csv");
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Statement stmt = con.createStatement();
        
        String table = args[0];
        String K = args[1];
        String colA = args[2];
        String colB = args[3];
        String colC = args[4];
        
        String myQuery = "SELECT " + colA + ", " + colB + ", " + colC + ", COUNT(*) as k "
        		+ "FROM " + table + " GROUP BY " + colA + ", " + colB + ", " + colC
                + " HAVING COUNT(*) < " + K;
        
        ResultSet rs = stmt.executeQuery(myQuery);
        if(rs.next() == false)
        {
        	System.out.println("yes");
			output.println("yes");
        }
        else
        {
        	System.out.println("no");
            output.println("no");
        }
        output.close();
        con.close();
	}
}
