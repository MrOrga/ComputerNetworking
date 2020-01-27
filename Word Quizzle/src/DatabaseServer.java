import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import Exception.*;
import com.google.gson.Gson;


public class DatabaseServer extends RemoteServer implements Database, Serializable
{
	//concurrent hash map per memorizzare user
	private ConcurrentHashMap<String, User> database;
	
	private transient ConcurrentHashMap<String, UserHandler> socketmap;
	
	private static ServerSocketChannel socket;
	private static Selector selector;
	private transient JsonObj obj;
	private transient ByteBuffer toSend;
	
	public DatabaseServer()
	{
		database = new ConcurrentHashMap<String, User>();
		socketmap = new ConcurrentHashMap<String, UserHandler>();
	}
	
	
	@Override
	public int register(String user, String password) throws UserAlreadyExist, RemoteException, IOException
	{
		if (database.containsKey(user))
		{
			obj = new JsonObj("401 UserAlreadyExist");
			return -401;
			//sendResponse(obj);
			//throw new UserAlreadyExist("User già esistente");
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
		handler.tofile(this, "db.json");
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
				sendResponse(new JsonObj("200 OK LOGIN"), client);
			}
			
		} else
			throw new IllegalArgumentException("Utente o password errati");
		
	}
	
	public void logout(String username, SocketChannel client) throws IOException
	{
		User user = database.get(username);
		user.logout();
		sendResponse(new JsonObj("201 OK LOGOUT"), client);
		//closing conn with client
		client.shutdownOutput();
	}
	
	public void addFriend(String username, String friend, SocketChannel client) throws ClosedChannelException
	{
		//check if the user and the user of a friend exist
		if (!(database.containsKey(username) && database.containsKey(friend)))
		{
			sendResponse(new JsonObj("410 User not exist"), client);
		}
		
		//check if the they are already friends
		User user = database.get(username);
		if (user.checkFriend(friend))
		{
			sendResponse(new JsonObj("411 Already friend"), client);
		}
		
		//setting friendship between user
		user.setFriend(friend);
		database.get(friend).setFriend(username);
	}
	
	private void sendResponse(JsonObj obj, SocketChannel client) throws ClosedChannelException
	{
		Gson gson = new Gson();
		String json = gson.toJson(obj);
		
		toSend = ByteBuffer.wrap(json.getBytes());
		
		client.register(selector, SelectionKey.OP_WRITE);
		
	}
	
	private void handleOperation(ByteBuffer buffer, SocketChannel client)
	{
		
		//GsonHandler handler = new GsonHandler();
		Gson gson = new Gson();
		//String json = buffer.toString();
		String json = new String(buffer.array(), StandardCharsets.UTF_8);
		System.out.println(json);
		JsonObj obj = gson.fromJson(json.trim(), JsonObj.class);
		//JsonObj obj = handler.readFromGson(buffer);
		
		try
		{
			switch (obj.op)
			{
				case "login":
					this.login(obj.username, obj.passwd, client);
					break;
				case "logout":
					this.logout(obj.username, client);
					break;
				case "addfriend":
					this.addFriend(obj.username, obj.friend, client);
					break;
				case "friendlist":
					this.friendlist(obj.username, client);
					break;
				
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	private void friendlist(String username, SocketChannel client)
	{
	}
	
	private void write(SelectionKey key) throws IOException
	{
		SocketChannel client = (SocketChannel) key.channel();
		System.out.println("Sending Response");
		/*if (toSend == null)
			sendResponse(new JsonObj("500 OK"), client);*/
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
		
		ByteBuffer buffer = ByteBuffer.allocate(512);
		int read = client.read(buffer);
		if (read == -1)
		{
		
		} else
		{
			buffer.flip();
			handleOperation(buffer, client);
			
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
		
		int port = 60500;
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
	

