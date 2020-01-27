import java.util.Vector;

public class User
{
	private String username;
	private String password;
	private Vector<String> friend;
	private int point;
	private transient boolean isLogged = false;
	
	public String getUsername()
	{
		return username;
	}
	
	public void setFriend(String friend)
	{
		this.friend.add(friend);
	}
	
	public Vector<String> getFriend()
	{
		return friend;
	}
	
	public int getPoint()
	{
		return point;
	}
	
	public boolean checkFriend(String friend)
	{
		
		if (friend.contains(friend))
			return true;
		else
			return false;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public User(String username, String password)
	{
		friend = new Vector<String>();
		this.username = username;
		this.password = password;
		point = 0;
	}
	
	public void login()
	{
		this.isLogged = true;
	}
	
	public void logout()
	{
		this.isLogged = false;
	}
	
	public boolean isLogged()
	{
		return isLogged;
	}
	
	public boolean checkpassword(String password)
	{
		if (this.password.equals(password))
			return true;
		else
			return false;
		
	}
	
	public void addPoint(int point)
	{
		this.point += point;
	}
}

