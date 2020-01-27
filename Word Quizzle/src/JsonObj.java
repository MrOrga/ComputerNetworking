import java.lang.reflect.Array;

public class JsonObj
{
	public String op;
	public String username;
	public String friend;
	public String passwd;
	
	public JsonObj(String op, String username, String passwd)
	{
		this.op = op;
		this.username = username;
		this.passwd = passwd;
	}
	
	public JsonObj(String op, String username, String friend, String passwd)
	{
		this.op = op;
		this.username = username;
		this.friend = friend;
		this.passwd = passwd;
	}
	
	public JsonObj(String op)
	{
		this.op = op;
		
	}
	
	/*@Override
	public String toString()
	{
		return "JsonObj{" +
			"op='" + op + '\'' +
			", username='" + username + '\'' +
			", passwd='" + passwd + '\'' +
			'}';
	}*/
}
