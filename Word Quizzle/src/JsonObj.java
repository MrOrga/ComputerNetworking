public class JsonObj
{
	private String op;
	private String username;
	private String friend;
	private String passwd;
	
	public void setPasswd(String passwd)
	{
		this.passwd = passwd;
	}
	
	public String getPasswd()
	{
		return passwd;
	}
	
	public String getOp()
	{
		return op;
	}
	
	public void setOp(String op)
	{
		this.op = op;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public String getFriend()
	{
		return friend;
	}
	
	public void setFriend(String friend)
	{
		this.friend = friend;
	}
	
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
	
	public JsonObj(String op, String friend)
	{
		this.op = op;
		this.friend = friend;
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
