import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class Lookup
{

	public static void main(String[] args) throws IOException, SQLException
	{
		PrintWriter output = new PrintWriter("Lookup.csv");
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String urlA = props.getProperty("ConA");
		String urlB = props.getProperty("ConB");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        String tableA = props.getProperty("TableA");
        String tableB = props.getProperty("TableB");
        String columnA = props.getProperty("ColumnA");
        String columnB = props.getProperty("ColumnB");
        Connection conA = DriverManager.getConnection(urlA, user, pwd);
        Statement stmtA = conA.createStatement();
        Connection conB = DriverManager.getConnection(urlB, user, pwd);
        Statement stmtB = conB.createStatement();
        
        ArrayList<String> listA = new ArrayList<String>();
        ArrayList<String> listB = new ArrayList<String>();
        
        String myQueryA = "SELECT * FROM " + tableA;
        String myQueryB = "SELECT * FROM " + tableB;
        
        ResultSet rs = stmtA.executeQuery(myQueryA);
        while(rs.next())
        	listA.add(rs.getObject(columnA).toString());
        conA.close();
        
        rs = stmtB.executeQuery(myQueryB);
        while(rs.next())
        	listB.add(rs.getObject(columnB).toString());
        conB.close();
        
        for(String a : listA)
        	for(String b : listB)
        		if(a.equals(b))
        			output.println(a + "," + b);
        
        
	}

}
