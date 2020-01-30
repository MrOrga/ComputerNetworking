import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class UserHandler
{
	private SocketChannel socket;
	private ByteBuffer toSend;
	private String username;
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public ByteBuffer getToSend()
	{
		return toSend;
	}
	
	public void setToSend(ByteBuffer toSend)
	{
		this.toSend = toSend;
	}
	
	public UserHandler(SocketChannel socket, String username)
	{
		this.socket = socket;
		this.username = username;
	}
	
	public UserHandler(SocketChannel socket)
	{
		this.socket = socket;
	}
	
	public SocketChannel getSocket()
	{
		return socket;
	}
}
