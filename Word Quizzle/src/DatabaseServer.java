import java.io.*;
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
	private transient SelectionKey currentKey;
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
			sendResponse(obj, client, currentUser);
			//throw new IllegalArgumentException("Utente o password errati");
		}
		
		User user = database.get(username);
		if (user.checkpassword(password))
		{
			if (user.isLogged())
			{
				obj = new JsonObj("401 UserAlreadyLogged");
				sendResponse(obj, client, currentUser);
				//throw new UserAlreadyLogged("Utente già loggato");
			} else
			{
				user.login();
				//registration of socket in the socketmap
				socketmap.put(username, new UserHandler(client, username));
				JsonObj obj1 = new JsonObj("200 OK LOGIN");
				obj1.setUsername(username);
				sendResponse(obj1, client, currentUser);
				handler.tofile(this, "db.json");
			}
			
		} else
			throw new IllegalArgumentException("Utente o password errati");
		
	}
	
	public void logout(String username, SocketChannel client) throws IOException
	{
		User user = database.get(username);
		user.logout();
		sendResponse(new JsonObj("204 OK LOGOUT"), client, currentUser);
		
		//closing conn with client
		//client.shutdownOutput();
	}
	
	public void addFriend(String username, String friend, SocketChannel client) throws IOException
	{
		//check if the user and the user of a friend exist
		if ((!database.containsKey(username) || !(database.containsKey(friend))))
		{
			sendResponse(new JsonObj("410 User not exist"), client, currentUser);
			
		} else
		{
			if (database.get(username).checkFriend(friend) || database.get(friend).checkFriend(username))
			{
				sendResponse(new JsonObj("411 Already friend"), client, currentUser);
				
			}
			
			//check if the they are already friends
			else
			{
				//setting friendship between user
				database.get(username).setFriend(friend);
				//user.setFriend(friend);
				database.get(friend).setFriend(username);
				sendResponse(new JsonObj("202 User added to friend list"), client, currentUser);
				GsonHandler handler = new GsonHandler();
				handler.tofile(this, "db.json");
			}
		}
	}
	
	public void sendResponse(JsonObj obj, SocketChannel client, String user) throws IOException
	{
		this.obj = obj;
		Gson gson = new Gson();
		String json = gson.toJson(obj);
		System.out.println(json);
		socketmap.get(user).setToSend(ByteBuffer.wrap(json.getBytes()));
		
		client.register(selector, SelectionKey.OP_WRITE);
		
		
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
				case "accept":
					this.acceptChallenge(currentUser, friend, client);
				
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	
	private void friendlist(String username, SocketChannel client) throws IOException
	{
		Gson gson = new Gson();
		Vector<String> friend = database.get(username).getFriend();
		JsonObj obj1 = new JsonObj("203 showfriendlist");
		obj1.setFriendlist(friend);
		String json = gson.toJson(obj);
		/*ByteBuffer toSend = ByteBuffer.wrap(json.getBytes());
		socketmap.get(username).setToSend(toSend);*/
		sendResponse(obj1, client, currentUser);
		
	}
	
	private void write(SelectionKey key) throws IOException
	{
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer toSend = socketmap.get(currentUser).getToSend();
		System.out.println("faccio write");
		
		//if is the first write sending the lenght of obj
		
		int len = toSend.array().length;
		//len of int value
		//int intlen = String.valueOf(len).length();
		//int totlen = len + intlen;
		ByteBuffer bufflen = ByteBuffer.allocate(4);
		bufflen.clear();
		bufflen.putInt(len);
		bufflen.flip();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(bufflen.array());
		outputStream.write(toSend.array());
		ByteBuffer newBuff = ByteBuffer.wrap(outputStream.toByteArray());
		client.write(newBuff);
		
		
		if (obj.getOp().startsWith("204"))
		{
			client.close();
		} else
		{
			
			client.register(selector, SelectionKey.OP_READ);
		}
	}
	
	private void read(SelectionKey key) throws IOException
	{
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
		{
			client.close();
			return;
		}
		
		
		if (attachment == null)
		{
			len = buf.getInt();
		}
		
		//System.out.println(len);
		if (key.attachment() == null)
		{
			res = new String(buf.array(), buf.position(), buf.remaining(), StandardCharsets.UTF_8);
			attachment = new Attachment(res, len, len - read);
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
		client.register(selector, SelectionKey.OP_READ);
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
	
	public UserHandler getUserHandler(String user)
	{
		return socketmap.get(user);
	}
	
	public User getUser(String user)
	{
		return database.get(user);
	}
	
	private void acceptChallenge(String currentUser, String friend, SocketChannel client) throws IOException
	{
		System.out.println("Challenge accepted");
		UserHandler handler = socketmap.get(friend);
		SocketChannel friendSocket = handler.getSocket();
		
		//geting the selection key
		SelectionKey keyClient = client.keyFor(selector);
		SelectionKey keyFriend = friendSocket.keyFor(selector);
		//setting interest of the keys to 0
		keyClient.interestOps(0);
		keyFriend.interestOps(0);
		/*keyClient.interestOps(SelectionKey.OP_WRITE);
		keyFriend.interestOps(SelectionKey.OP_WRITE);*/
		
		Challenge challenge = new Challenge(selector, socket, this, client, friendSocket, currentUser, friend);
		challenge.start();
	}
	
	public ByteBuffer getToSend(String user)
	{
		return socketmap.get(user).getToSend();
	}
	
	public void setToSend(ByteBuffer toSend, String user)
	{
		socketmap.get(user).getToSend().clear();
		socketmap.get(user).setToSend(toSend);
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
	
	public ConcurrentHashMap<String, User> getDatabase()
	{
		return database;
	}
	
	public void setDatabase(ConcurrentHashMap<String, User> database)
	{
		this.database = database;
	}
	
	public ConcurrentHashMap<String, UserHandler> getSocketmap()
	{
		return socketmap;
	}
	
	public void setSocketmap(ConcurrentHashMap<String, UserHandler> socketmap)
	{
		this.socketmap = socketmap;
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
				System.out.println("selector main");
				for (SelectionKey key : selector.selectedKeys())
				{
					currentKey = key;
					if (key.isValid() && key.isAcceptable())
					{
						accept(key);
					}
					if (key.isValid() && key.isReadable())
					{
						read(key);
					}
					if (key.isValid() && key.isWritable())
					{
						write(key);
					}
				}
				selector.selectedKeys().clear();
			} catch (SocketException ex)
			{
				
				currentKey.cancel();
				
				socketmap.remove(currentUser);
				database.get(currentUser).logout();
				
				
			} catch (IOException e)
			{
				
				e.printStackTrace();
			}
		}
	}
	
}
	

