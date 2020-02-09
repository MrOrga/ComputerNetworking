import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class SelectorT implements Runnable
{
	private JsonObj obj;
	private int portTcp = 60501;
	private ControllerLogin controllerLogin;
	private Userhome userhome;
	private ActionEvent event;
	private Selector selector;
	private SocketChannel socket;
	private ByteBuffer toSend;
	private String username;
	private static UdpListener udpListener;
	
	
	public void setEvent(ActionEvent event)
	{
		this.event = event;
	}
	
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
	
	public void setUserhome(Userhome userhome)
	{
		this.userhome = userhome;
	}
	
	public void sendRequest(JsonObj obj) throws ClosedChannelException
	{
		Gson gson = new Gson();
		this.obj = obj;
		this.obj.setUsername(this.getUsername());
		String json = gson.toJson(this.obj);
		toSend = ByteBuffer.wrap(json.getBytes());
		
		socket.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
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
	
	private void goToHome()
	{
		
		Platform.runLater(() ->
		{
			try
			{
				controllerLogin.goToHome(event);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}
	
	private void goToChallenge(String word)
	{
		
		Platform.runLater(() ->
		{
			try
			{
				controllerLogin.goToChallenge(event, word);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}
	
	private void newWord(String word)
	{
		
		Platform.runLater(() ->
		{
			try
			{
				controllerLogin.setWord(word);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}
	
	//show friend list
	private void showFriendList(Vector<String> friendlist)
	{
		Platform.runLater(() ->
		{
			try
			{
				userhome.showFriend(friendlist);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}
	
	private void showFriendListAfter()
	{
		Platform.runLater(() ->
		{
			try
			{
				userhome.showFriendListClick();
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
	private void hadleResponse(String json) throws IOException
	{
		
		System.out.println(json.trim());
		Gson gson = new Gson();
		JsonObj obj = gson.fromJson(json.trim(), JsonObj.class);
		//some problem with gson function
		
		String op = obj.getOp();
		
		//operation login successful
		if (op.startsWith("200"))
		{
			this.setUsername(obj.getUsername());
			goToUserHome();
			//show friend list
			//userhome.showFriend();
			
			//start UDP thread listener
			InetSocketAddress myAddress = (InetSocketAddress) socket.getLocalAddress();
			
			udpListener = new UdpListener(myAddress.getPort());
			udpListener.setDaemon(true);
			udpListener.start();
			
			
			//udpListener.setUserhome(userhome);
			
		}
		if (op.startsWith("202"))
		{
			//this.setUsername(obj.getUsername());
			System.out.println("Friend added");
			showFriendListAfter();
		}
		if (op.startsWith("203"))
		{
			
			showFriendList(obj.getFriendlist());
		}
		if (op.startsWith("204"))
		{
			socket.close();
			this.setUsername("");
			goToHome();
			
		}
		if (op.startsWith("210"))
		{
			
			goToChallenge(obj.getWord());
		}
		if (op.startsWith("211"))
		{
			
			newWord(obj.getWord());
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
			Gson gson = new Gson();
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
						System.out.println("Reading some message from Server");
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
							System.out.println("Attachment not null");
							attachment = (Attachment) key.attachment();
							len = attachment.getLen();
						}
						int read = client.read(buf);
						buf.flip();
						//some error
						System.out.println(read);
						if (read == -1)
						{
							System.out.println("help");
							return;
						}
						
						
						if (attachment == null)
						{
							
							len = buf.getInt();
						}
						/*if (read < len && len < 512)
							read += client.read(buf);*/
						
						//System.out.println(len);
						if (key.attachment() == null)
						{
							res = new String(buf.array(), buf.position(), buf.remaining(), StandardCharsets.UTF_8);
							attachment = new Attachment(res, len, len - (read - 4));
							if (attachment.getLeft() <= 0)
							{
								key.attach(null);
								hadleResponse(res);
							} else
							{
								System.out.println("attachment read");
								socket.register(selector, SelectionKey.OP_READ, attachment);
							}
							
						} else
						{
							if (key.attachment() != null)
							{
								res = attachment.getRes() + new String(buf.array(), buf.position(), buf.remaining(), StandardCharsets.UTF_8);
								
								attachment = new Attachment(res, len, attachment.getLeft() - read);
								if (attachment.getLeft() > 0)
								{
									System.out.println("attachment read");
									socket.register(selector, SelectionKey.OP_READ, attachment);
								} else
								{
									key.attach(null);
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
							socket.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, null);
						else
						{
							socket.register(selector, SelectionKey.OP_READ, null);
							//socket.shutdownOutput();
							
						}
					}
				}
				selector.selectedKeys().clear();
			}
		} catch (CancelledKeyException ex)
		{
			selector.selectedKeys().clear();
		} catch (Exception ex)
		
		{
			ex.printStackTrace();
		}
	}
	
	
}
