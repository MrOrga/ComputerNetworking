public class JsonObj
{
	public String op;
	public String username;
	public String passwd;
	
	public JsonObj(String op, String username, String passwd)
	{
		this.op = op;
		this.username = username;
		this.passwd = passwd;
	}
	
	@Override
	public String toString()
	{
		return "JsonObj{" +
			"op='" + op + '\'' +
			", username='" + username + '\'' +
			", passwd='" + passwd + '\'' +
			'}';
	}
}
