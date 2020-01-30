import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SelectorT implements Runnable
{
	private JsonObj obj;
	private int portTcp = 60501;
	private ControllerLogin controllerLogin;
	private ActionEvent event;
	private Selector selector;
	private SocketChannel socket;
	private ByteBuffer toSend;
	private Gson gson;
	private String username;
	
	public void setObj(JsonObj obj)
	{
		this.obj = obj;
	}
	
	public JsonObj getObj()
	{
		return obj;
	}
	
	public SelectorT(JsonObj obj, ControllerLogin controllerLogin, ActionEvent event)
	{
		this.controllerLogin = controllerLogin;
		this.obj = obj;
		this.event = event;
	}
	
	public void sendRequest(JsonObj obj) throws ClosedChannelException
	{
		System.out.println("CHECK");
		this.obj = obj;
		this.obj.setUsername(this.getUsername());
		String json = gson.toJson(this.obj);
		toSend = ByteBuffer.wrap(json.getBytes());
		
		socket.register(selector, SelectionKey.OP_WRITE);
		selector.wakeup();
		
	}
	
	//running javafx code to changing scene
	private void goToUserHome()
	{
		
		Platform.runLater(() ->
		{
			try
			{
				controllerLogin.goToUserHome(event);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	//handle response from server
	private void hadleResponse(String json)
	{
		System.out.println(json);
		Gson gson = new Gson();
		JsonObj obj = gson.fromJson(json.trim(), JsonObj.class);
		String op = obj.getOp();
		
		//operation login successful
		if (op.startsWith("200"))
		{
			this.setUsername(obj.getUsername());
			goToUserHome();
		}
		if (op.startsWith("202"))
		{
			//this.setUsername(obj.getUsername());
			System.out.println("Friend added");
		}
		//operation error
		else
		{
		
		}
		
		
	}
	
	
	@Override
	public void run()
	{
		try
		{
			
			
			System.out.println("Thread Selector start");
			
			socket = SocketChannel.open();
			socket.configureBlocking(false);
			selector = Selector.open();
			
			boolean connected = socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), portTcp));
			gson = new Gson();
			String json = gson.toJson(obj);
			System.out.println(json);
			//String jsend = new String(json, StandardCharsets.UTF_8);
			//ByteBuffer toSend = ByteBuffer.wrap(gson.toJson(obj).getBytes());
			toSend = ByteBuffer.wrap(json.getBytes());
			if (!connected)
				socket.register(selector, SelectionKey.OP_CONNECT);
			else
				socket.register(selector, SelectionKey.OP_WRITE);
			while (true)
			{
				selector.select();
				for (SelectionKey key : selector.selectedKeys())
				{
					if (key.isConnectable())
					{
						connected = socket.finishConnect();
						if (!connected)
							socket.register(selector, SelectionKey.OP_CONNECT);
						else
							socket.register(selector, SelectionKey.OP_WRITE);
					}
					if (key.isReadable())
					{
						/*String res = "";
						ByteBuffer buf = ByteBuffer.allocate(512);
						if (key.attachment() != null)
							res += (String) key.attachment();
						
						int len = buf.getInt();
						
						int read = socket.read(buf);
						//some error
						if (read == -1)
							return;
						int bufRemaining = buf.limit() - buf.position();
						if (len > bufRemaining)
						{
							res += new String(buf.array(), buf.position(), bufRemaining, StandardCharsets.UTF_8);
							socket.register(selector, SelectionKey.OP_READ, res);
						} else
						{
							buf.flip();
							hadleResponse(buf);
						}*/
						
						SocketChannel client = (SocketChannel) key.channel();
						
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
								hadleResponse(res);
							else
								socket.register(selector, SelectionKey.OP_READ, attachment);
							
						} else
						{
							if (key.attachment() != null)
							{
								res = attachment.getRes() + new String(buf.array(), StandardCharsets.UTF_8);
								
								attachment = new Attachment(res, len, attachment.getLeft() - read);
								if (attachment.getLeft() > 0)
									
									socket.register(selector, SelectionKey.OP_READ, attachment);
								else
								{
									hadleResponse(res);
								}
								
							}
							
							
						}
					}
					
					//System.out.print(new String(buf.array(), 0, buf.limit()));
					//gestione esito op
					//}
					if (key.isWritable())
					{
						
						System.out.println("SEND REQUEST TO SERVER");
						if (toSend.position() == 0)
						{
							
							int len = toSend.array().length;
							System.out.println(len);
							//len of int value
							String slen = String.valueOf(len) + " ";
							int intlen = slen.length();
							//int totlen = len + intlen;
							ByteBuffer bufflen = ByteBuffer.allocate(4);
							bufflen.clear();
							
							bufflen.putInt(len);
							bufflen.flip();
							//bufflen = ByteBuffer.wrap(slen.getBytes());
							int byteWrite = socket.write(bufflen);
							System.out.println(byteWrite);
							//System.out.println(bufflen.getInt(0));
						}
						socket.write(toSend);
						//System.out.println(toSend.getInt(0));
						//System.out.println(new String(toSend.array()));
						if (toSend.hasRemaining())
							socket.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
						else
						{
							socket.register(selector, SelectionKey.OP_READ);
							//socket.shutdownOutput();
							
						}
					}
				}
				selector.selectedKeys().clear();
			}
		} catch (
			Exception ex)
		
		{
			ex.printStackTrace();
		}
	}
	
	
}
