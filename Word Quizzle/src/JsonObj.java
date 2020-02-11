import java.util.Vector;

public class JsonObj
{
	private String op;
	private String username;
	private String friend;
	private String passwd;
	private Vector<String> friendlist;
	private Vector<Integer> scores;
	private String word;
	private Integer points;
	
	public int getPoints()
	{
		return points;
	}
	
	public void setPoints(int challengePoints)
	{
		this.points = challengePoints;
	}
	
	public String getWord()
	{
		return word;
	}
	
	public void setWord(String word)
	{
		this.word = word;
	}
	
	public JsonObj(String username, Vector<String> friendlist)
	{
		this.username = username;
		this.friendlist = friendlist;
	}
	
	public Vector<String> getFriendlist()
	{
		return friendlist;
	}
	
	public void setFriendlist(Vector<String> friendlist)
	{
		this.friendlist = friendlist;
	}
	
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
	
	public Vector<Integer> getScores()
	{
		return scores;
	}
	
	public void setScores(Vector<Integer> scores)
	{
		this.scores = scores;
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
