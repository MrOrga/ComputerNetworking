import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import Exception.*;
import com.google.gson.Gson;


public class DatabaseServer extends RemoteServer implements Database, Serializable
{
	//concurrent hash map to save user info
	private ConcurrentHashMap<String, User> database;
	
	//socket map to save user info
	private transient ConcurrentHashMap<String, UserHandler> socketmap;
	//private transient ConcurrentHashMap<SocketChannel, UserHandler> socketmap;
	
	private static ServerSocketChannel socket;
	private static Selector selector;
	private transient JsonObj obj;
	private transient String currentUser;
	//private ByteBuffer toSend;
	
	public DatabaseServer()
	{
		database = new ConcurrentHashMap<String, User>();
		//socketmap = new ConcurrentHashMap<SocketChannel, UserHandler>();
		socketmap = new ConcurrentHashMap<String, UserHandler>();
	}
	
	
	@Override
	public int register(String user, String password) throws UserAlreadyExist, RemoteException, IOException
	{
		if (database.containsKey(user))
		{
			obj = new JsonObj("401 UserAlreadyExist");
			//throw new UserAlreadyExist("User già esistente");
			return -401;
			//sendResponse(obj);
			
		}
		if (password == null)
		{
			
			obj = new JsonObj("400 IllegalArgumentException");
			return -400;
			//sendResponse(obj);
			//throw new IllegalArgumentException("La password non può essere nulla");
		}
		
		database.put(user, new User(user, password));
		System.out.println("Register user: " + user + "password: " + password);
		
		//saving db into file
		GsonHandler handler = new GsonHandler();
		handler.tofile(this, "db.json");
		
		
		return 0;
	}
	
	public void login(String username, String password, SocketChannel client) throws UserAlreadyLogged, IllegalArgumentException, IOException
	{
		GsonHandler handler = new GsonHandler();
		
		System.out.println(username);
		if (!database.containsKey(username))
		{
			obj = new JsonObj("400 IllegalArgumentException");
			sendResponse(obj, client);
			//throw new IllegalArgumentException("Utente o password errati");
		}
		
		User user = database.get(username);
		if (user.checkpassword(password))
		{
			if (user.isLogged())
			{
				obj = new JsonObj("401 UserAlreadyExist");
				sendResponse(obj, client);
				//throw new UserAlreadyLogged("Utente già loggato");
			} else
			{
				user.login();
				//registration of socket in the socketmap
				socketmap.put(username, new UserHandler(client, username));
				JsonObj obj1 = new JsonObj("200 OK LOGIN");
				obj1.setUsername(username);
				sendResponse(obj1, client);
				handler.tofile(this, "db.json");
			}
			
		} else
			throw new IllegalArgumentException("Utente o password errati");
		
	}
	
	public void logout(String username, SocketChannel client) throws IOException
	{
		User user = database.get(username);
		user.logout();
		sendResponse(new JsonObj("204 OK LOGOUT"), client);
		//closing conn with client
		//client.shutdownOutput();
	}
	
	public void addFriend(String username, String friend, SocketChannel client) throws IOException
	{
		//check if the user and the user of a friend exist
		if ((!database.containsKey(username) || !(database.containsKey(friend))))
		{
			sendResponse(new JsonObj("410 User not exist"), client);
			
		} else
		{
			if (database.get(username).checkFriend(friend) || database.get(friend).checkFriend(username))
			{
				sendResponse(new JsonObj("411 Already friend"), client);
				
			}
			
			//check if the they are already friends
			else
			{
				//setting friendship between user
				database.get(username).setFriend(friend);
				//user.setFriend(friend);
				database.get(friend).setFriend(username);
				sendResponse(new JsonObj("202 User added to friend list"), client);
				GsonHandler handler = new GsonHandler();
				handler.tofile(this, "db.json");
			}
		}
	}
	
	private void sendResponse(JsonObj obj, SocketChannel client) throws ClosedChannelException
	{
		Gson gson = new Gson();
		String json = gson.toJson(obj);
		System.out.println(json);
		socketmap.get(currentUser).setToSend(ByteBuffer.wrap(json.getBytes()));
		
		client.register(selector, SelectionKey.OP_WRITE);
		
	}
	
	/*private void handleOperation(ByteBuffer buffer, SocketChannel client)
	{
		
		//GsonHandler handler = new GsonHandler();
		Gson gson = new Gson();
		//String json = buffer.toString();
		String json = new String(buffer.array(), StandardCharsets.UTF_8);
		System.out.println(json);
		JsonObj obj = gson.fromJson(json.trim(), JsonObj.class);
		//JsonObj obj = handler.readFromGson(buffer);
		currentUser=obj.getUsername();
		String passwd=obj.getPasswd();
		String friend =obj.getFriend();
		try
		{
			switch (obj.getOp())
			{
				case "login":
					this.login(currentUser, passwd, client);
					break;
				case "logout":
					this.logout(currentUser, client);
					break;
				case "addfriend":
					this.addFriend(currentUser, friend, client);
					break;
				case "friendlist":
					this.friendlist(currentUser, client);
					break;
				case "challange":
					this.challange(friend,client);
					break;
				
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}*/
	
	private void handleOperation(String json, SocketChannel client)
	{
		
		//GsonHandler handler = new GsonHandler();
		Gson gson = new Gson();
		//String json = buffer.toString();
		//*String json = new String(buffer.array(), StandardCharsets.UTF_8);*//
		System.out.println(json);
		JsonObj obj = gson.fromJson(json.trim(), JsonObj.class);
		//JsonObj obj = handler.readFromGson(buffer);
		currentUser = obj.getUsername();
		String passwd = obj.getPasswd();
		String friend = obj.getFriend();
		try
		{
			switch (obj.getOp())
			{
				case "login":
					this.login(currentUser, passwd, client);
					break;
				case "logout":
					this.logout(currentUser, client);
					break;
				case "addfriend":
					this.addFriend(currentUser, friend, client);
					break;
				case "friendlist":
					this.friendlist(currentUser, client);
					break;
				case "challenge":
					this.challenge(friend, client);
					break;
				
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	private void friendlist(String username, SocketChannel client) throws ClosedChannelException
	{
		Gson gson = new Gson();
		Vector<String> friend = database.get(username).getFriend();
		JsonObj obj1 = new JsonObj("203 showfriendlist");
		obj1.setFriendlist(friend);
		String json = gson.toJson(obj);
		/*ByteBuffer toSend = ByteBuffer.wrap(json.getBytes());
		socketmap.get(username).setToSend(toSend);*/
		sendResponse(obj1, client);
		
	}
	
	private void write(SelectionKey key) throws IOException
	{
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer toSend = socketmap.get(currentUser).getToSend();
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
			client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
		
		else
		{
			client.register(selector, SelectionKey.OP_READ);
			//client.shutdownOutput();
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
				client.register(selector, SelectionKey.OP_READ, attachment);
			
		} else
		{
			if (key.attachment() != null)
			{
				res = attachment.getRes() + new String(buf.array(), StandardCharsets.UTF_8);
				
				attachment = new Attachment(res, len, attachment.getLeft() - read);
				if (attachment.getLeft() > 0)
					
					client.register(selector, SelectionKey.OP_READ, attachment);
				else
				{
					handleOperation(res, client);
				}
				
			}
			
			
		}
	}
	
	private static void accept(SelectionKey key) throws IOException
	{
		SocketChannel client = socket.accept();
		System.out.println("New Client connected");
		client.configureBlocking(false);
		//ClientState state = new ClientState();
		client.register(selector, SelectionKey.OP_READ);
	}
	
	private void saveDatabase(DatabaseServer db)
	{
		File file = new File("db.json");
		
	}
	
	//sending challenge with udp to the user
	private void challenge(String friend, SocketChannel client) throws IOException
	{
		System.out.println("sending challenge");
		UserHandler handler = socketmap.get(friend);
		Gson gson = new Gson();
		SocketChannel friendSocket = handler.getSocket();
		InetSocketAddress addressFriend = (InetSocketAddress) (friendSocket.getRemoteAddress());
		InetAddress address = addressFriend.getAddress();
		int port = addressFriend.getPort();
		
		System.out.println("UDP port client: " + port);
		//UDP request
		
		DatagramSocket socketUDP = new DatagramSocket();
		
		//setting challange request
		obj = new JsonObj("challenge", currentUser);
		String json = gson.toJson(obj);
		
		DatagramPacket request = new DatagramPacket(json.getBytes(), json.getBytes().length, address, port);
		
		socketUDP.send(request);
		System.out.println("challenge sent");
		
	}
	
	public static void main(String[] args) throws IOException
	{
		File db = new File("db.json");
		GsonHandler handler = new GsonHandler();
		DatabaseServer s;
		
		//restoring database from json file
		if (db.exists())
			s = (DatabaseServer) handler.fromFile(db.getPath());
		else
			s = new DatabaseServer();
		
		s.run();
	}
	
	private void run() throws RemoteException, IOException
	{
		
		int port = 60550;
		int portTcp = 60501;
		try
		{
			
			/* Esportazione dell'Oggetto */
			Database stub = (Database) UnicastRemoteObject.exportObject(this, 0);
			
			/* esportazione oggetto che bisogna fare perchè ho scelto di estendere RemoteServer */
			LocateRegistry.createRegistry(port);
			Registry r = LocateRegistry.getRegistry(port);
			/* Pubblicazione dello stub nel registry */
			r.rebind(Database.SERVICE_NAME, stub);
			System.out.println("Server ready");
		}
		/* If any communication failures occur... */ catch (RemoteException e)
		{
			System.out.println("Communication error " + e.toString());
		}
		socket = ServerSocketChannel.open();
		socket.bind(new InetSocketAddress(portTcp));
		socket.configureBlocking(false);
		selector = Selector.open();
		socket.register(selector, SelectionKey.OP_ACCEPT);
		while (true)
		{
			try
			{
				selector.select();
				for (SelectionKey key : selector.selectedKeys())
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
				selector.selectedKeys().clear();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
	

