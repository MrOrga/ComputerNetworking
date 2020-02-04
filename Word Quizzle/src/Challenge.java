import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Challenge extends Thread
{
	private Selector selector;
	private boolean isFinishied = false;
	private Selector challengeSelector;
	private SocketChannel player1;
	private SocketChannel player2;
	private ServerSocketChannel socket;
	private DatabaseServer server;
	private String currentUser;
	
	
	public Challenge(Selector selector, ServerSocketChannel socket, DatabaseServer server, SocketChannel player1, SocketChannel player2) throws IOException
	{
		challengeSelector = Selector.open();
		this.player1 = player1;
		this.player2 = player2;
		this.selector = selector;
		this.socket = socket;
		this.server = server;
	}
	
	@Override
	public void run()
	{
		try
		{
			
			challengeSelector = Selector.open();
			
			
			socket.register(challengeSelector, SelectionKey.OP_ACCEPT);
			
			SocketChannel client = (SocketChannel) player1.keyFor(selector).channel();
			client.register(challengeSelector, SelectionKey.OP_READ);
			client = (SocketChannel) player2.keyFor(selector).channel();
			client.register(challengeSelector, SelectionKey.OP_READ);
			//player2.register(challengeSelector, SelectionKey.OP_ACCEPT);
			while (!isFinishied)
			{
				System.out.println("selector new");
				challengeSelector.select();
				for (SelectionKey key : challengeSelector.selectedKeys())
				{
					if (key.isAcceptable())
					{
						accept(key);
					}
					if (key.isReadable())
					{
						read(key);
					}
					if (key.isWritable())
					{
						write(key);
					}
				}
				challengeSelector.selectedKeys().clear();
				
				
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void handleOperation(String json, SocketChannel client)
	{
		
		Gson gson = new Gson();
		
		System.out.println(json);
		JsonObj obj = gson.fromJson(json.trim(), JsonObj.class);
		
		currentUser = obj.getUsername();
		String passwd = obj.getPasswd();
		String friend = obj.getFriend();
		try
		{
			switch (obj.getOp())
			{
				case "login":
					//this.login(currentUser, passwd, client);
					break;
				case "logout":
					//this.logout(currentUser, client);
					break;
				case "addfriend":
					//this.addFriend(currentUser, friend, client);
					break;
				case "friendlist":
					//this.friendlist(currentUser, client);
					break;
				case "challenge":
					//this.challenge(friend, client);
					break;
				case "accept":
					//this.acceptChallenge(currentUser, friend, client);
				
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	private void accept(SelectionKey key) throws IOException
	{
		SocketChannel client = socket.accept();
		System.out.println("New Client connected");
		client.configureBlocking(false);
		client.register(challengeSelector, SelectionKey.OP_READ);
	}
	
	private void write(SelectionKey key) throws IOException
	{
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer toSend = server.getToSend(currentUser);
		System.out.println("Sending Response");
		
		//if is the first write sending the lenght of obj
		if (toSend.position() == 0)
		{
			int len = toSend.array().length;
			//len of int value
			int intlen = String.valueOf(len).length();
			//int totlen = len + intlen;
			ByteBuffer bufflen = ByteBuffer.allocate(len);
			bufflen.clear();
			bufflen.putInt(len);
			bufflen.flip();
			client.write(bufflen);
		}
		client.write(toSend);
		if (toSend.hasRemaining())
			client.register(challengeSelector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
		
		else
		{
			client.register(challengeSelector, SelectionKey.OP_READ);
		}
	}
	
	private void read(SelectionKey key) throws IOException
	{
		SocketChannel client = (SocketChannel) key.channel();
		//ByteBuffer toSend = socketmap.get(client).getToSend();
		ByteBuffer buf = ByteBuffer.allocate(512);
		buf.clear();
		Attachment attachment = null;
		String res = "";
		int len = 0;
		if (key.attachment() != null)
		{
			attachment = (Attachment) key.attachment();
			len = attachment.getLen();
		}
		int read = client.read(buf);
		buf.flip();
		//some error
		if (read == -1)
			return;
		
		
		if (attachment == null)
		{
			len = buf.getInt();
		}
		
		
		//System.out.println(len);
		if (key.attachment() == null)
		{
			res = new String(buf.array(), buf.position(), buf.remaining(), StandardCharsets.UTF_8);
			attachment = new Attachment(res, len, len - read - (String.valueOf(len).length()));
			if (len <= buf.remaining())
				handleOperation(res, client);
			else
				client.register(challengeSelector, SelectionKey.OP_READ, attachment);
			
		} else
		{
			if (key.attachment() != null)
			{
				res = attachment.getRes() + new String(buf.array(), StandardCharsets.UTF_8);
				
				attachment = new Attachment(res, len, attachment.getLeft() - read);
				if (attachment.getLeft() > 0)
					
					client.register(challengeSelector, SelectionKey.OP_READ, attachment);
				else
				{
					handleOperation(res, client);
				}
				
			}
			
			
		}
	}
}
