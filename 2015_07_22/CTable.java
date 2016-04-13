import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import lbi.utils.DBUtil;
import lbi.utils.IOUtil;

public class CTable
{

	public static void main(String[] args) throws IOException, SQLException
	{
		PrintWriter output = new PrintWriter("CTable.csv");
		Properties props = IOUtil.readProps("myJDBCdef.props");
		DBUtil.loadJDBC(props.getProperty("jdbcDriver"));
		String url = props.getProperty("jdbcURL");
		String user = props.getProperty("userName");
        String pwd = props.getProperty("userPWD");
        Connection con = DriverManager.getConnection(url, user, pwd);
        Statement stmt = con.createStatement();
        
        String table = args[0];
        String colA = args[1];
        String colB = args[2];
        
        //Output contingency table
        HashSet<Contingency> cTable = new HashSet<Contingency>();
        //for table scan
        ArrayList<Row> fact = new ArrayList<Row>();
        
        String myQuery = "SELECT " + colA + "," + colB + " FROM " + table;
        ResultSet rs = stmt.executeQuery(myQuery);
        while(rs.next())
        	fact.add(new Row(rs.getObject(1),rs.getObject(2)));
        con.close();
        
        //Maps values of colA to n1
        Map<Object,Long> ccA = fact.parallelStream().collect(Collectors.groupingBy(x -> (Object)(x.a), Collectors.counting()));
        //Maps values of colB to m1
        Map<Object,Long> ccB = fact.parallelStream().collect(Collectors.groupingBy(x -> (Object)(x.b), Collectors.counting()));
        //Compute a
        Map<Object,Long> computeA = fact.parallelStream().collect(Collectors.groupingBy(x -> (Object)(x.a + "," + x.b), Collectors.counting()));
        
        for(Object o : computeA.keySet())
        {
        	String va = o.toString().split(",")[0];
        	String vb = o.toString().split(",")[1];
        	Long n1 = ccA.get(va);
        	Long m1 = ccB.get(vb);
        	int n = fact.size();
        	Long a = computeA.get(o);
        	Long b = n1 - a;
        	Long c = m1 - a;
        	Long d = n + a - m1 - n1;
        	cTable.add(new Contingency(va,vb,a,b,c,d,n1,m1,n));
        }
        
        for(Contingency ct : cTable)
        	output.println(ct.va + "," + ct.vb + "," + ct.a + "," + ct.b + "," + ct.c + "," + ct.d);
        	
        output.close();
	}

}


//non ho voglia di fare le get e set
class Row
{
	public Object a;
	public Object b;
	
	public Row(Object o1, Object o2)
	{
		this.a = o1;
		this.b = o2;
	}
}

class Contingency
{
	public Object va;
	public Object vb;
	public Long a;
	public Long b;
	public Long c;
	public Long d;
	public Long n1;
	public Long m1;
	public int n;
	
	public Contingency(Object o1, Object o2, Long a, Long b, Long c, Long d, Long n1, Long m1, int n)
	{
		this.va = o1;
		this.vb = o2;
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.n1 = n1;
		this.m1 = m1;
		this.n = n;
	}
}
