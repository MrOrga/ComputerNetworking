import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class UserHandler
{
	private SocketChannel socket;
	private ByteBuffer toSend;
	private String username;
	private boolean isBusy = false;
	
	public boolean isBusy()
	{
		return isBusy;
	}
	
	public void setBusy(boolean busy)
	{
		isBusy = busy;
	}
	
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
