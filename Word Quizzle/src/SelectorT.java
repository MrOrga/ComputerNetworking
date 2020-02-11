import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
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
	public static String username;
	private static UdpListener udpListener;
	private Controller controllerHome;
	private static volatile boolean run = true;
	
	public static void setRun(boolean run)
	{
		SelectorT.run = run;
	}
	
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
	
	public SelectorT(JsonObj obj, ControllerLogin controllerLogin, ActionEvent event, Controller controllerHome)
	{
		run = true;
		this.controllerLogin = controllerLogin;
		this.obj = obj;
		this.event = event;
		this.controllerHome = controllerHome;
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
	
	private void goToScore(ActionEvent event, int score, String error)
	{
		
		Platform.runLater(() ->
		{
			try
			{
				controllerLogin.goToScore(event, score, error);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}
	
	private void errorPopUp(ActionEvent event, String error)
	{
		
		Platform.runLater(() ->
		{
			try
			{
				Stage popup = new Stage();
				FXMLLoader load = new FXMLLoader(getClass().getResource("error.fxml"));
				Parent home = load.load();
				ErrorController c = load.getController();
				c.setError(error);
				popup.setScene(new Scene(home, 310, 220));
				popup.show();
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
	
	//show user points
	private void showScore(int points)
	{
		Platform.runLater(() ->
		{
			try
			{
				userhome.showPoints(points);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}
	
	//show leaderboard
	private void showLeaderboard(Vector<String> friendlist, Vector<Integer> scores)
	{
		Platform.runLater(() ->
		{
			try
			{
				userhome.showLeaderboard(friendlist, scores);
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
				userhome.showFriendListClick(event);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}
	
	private void showError(String s)
	{
		Platform.runLater(() ->
		{
			try
			{
				controllerHome.showError(s);
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
		//add friend
		if (op.startsWith("202"))
		{
			//this.setUsername(obj.getUsername());
			System.out.println("Friend added");
			showFriendListAfter();
		}
		//friendlist
		if (op.startsWith("203"))
		{
			
			showFriendList(obj.getFriendlist());
		}
		//logout
		if (op.startsWith("204"))
		{
			socket.close();
			this.setUsername("");
			goToHome();
			UdpListener.setRun(false);
			run = false;
		}
		//Leaderboard
		if (op.startsWith("205"))
		{
			
			showLeaderboard(obj.getFriendlist(), obj.getScores());
		}
		//show points
		if (op.startsWith("206"))
		{
			
			showScore(obj.getPoints());
		}
		//challenge
		if (op.startsWith("210"))
		{
			
			goToChallenge(obj.getWord());
		}
		//nextword challenge
		if (op.startsWith("211"))
		{
			
			newWord(obj.getWord());
		}
		
		if (op.startsWith("212"))
		{
			
			goToScore(event, 0, "Waiting opponent");
		}
		//challenge is over
		if (op.startsWith("213"))
		{
			goToScore(event, obj.getPoints(), null);
		}
		
		
		//error code 4XX
		//
		if (op.startsWith("400"))
		{
			showError("Invalid user or password");
			
		}
		if (op.startsWith("401"))
		{
			showError("User already logged");
			
		}
		//already friend
		if (op.startsWith("411"))
		{
			
			errorPopUp(event, op.substring(4));
		}
		//friend is busy
		if (op.startsWith("450"))
		{
			errorPopUp(event, op.substring(4));
			UdpListener.resetAccept();
		}
		//error on translation server
		if (op.startsWith("451"))
		{
			goToScore(event, 0, "translation error");
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
			while (run)
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
					
					
					if (key.isWritable())
					{
						
						System.out.println("SEND REQUEST TO SERVER");
						
						int len = toSend.array().length;
						System.out.println(len);
						
						ByteBuffer bufflen = ByteBuffer.allocate(4);
						bufflen.clear();
						bufflen.putInt(len);
						bufflen.flip();
						
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						outputStream.write(bufflen.array());
						outputStream.write(toSend.array());
						ByteBuffer newBuff = ByteBuffer.wrap(outputStream.toByteArray());
						socket.write(newBuff);
						
						socket.register(selector, SelectionKey.OP_READ, null);
						
						
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
