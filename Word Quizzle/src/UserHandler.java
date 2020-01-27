import java.nio.channels.SocketChannel;

public class UserHandler
{
	private SocketChannel socket;
	
	public UserHandler(SocketChannel socket)
	{
		this.socket = socket;
	}
	
	public SocketChannel getSocket()
	{
		return socket;
	}
}
