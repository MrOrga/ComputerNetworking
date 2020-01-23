import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
	
	private static ServerSocketChannel socket;
	private static Selector selector;
	
	public DatabaseServer()
	{
		database = new ConcurrentHashMap<String, User>();
	}
	
	@Override
	public int register(String user, String password) throws UserAlreadyExist, RemoteException, IOException
	{
		if (database.containsKey(user))
			throw new UserAlreadyExist("User già esistente");
		if (password == null)
			throw new IllegalArgumentException("La password non può essere nulla");
		
		database.put(user, new User(user, password));
		System.out.println("Register user: " + user + "password: " + password);
		
		//saving db into file
		GsonHandler handler = new GsonHandler();
		handler.tofile(this, "db.json");
		
		
		return 0;
	}
	
	public void login(String username, String password) throws UserAlreadyLogged, IllegalArgumentException, IOException
	{
		GsonHandler handler = new GsonHandler();
		handler.tofile(this, "db.json");
		System.out.println(username);
		if (!database.containsKey(username))
			throw new IllegalArgumentException("Utente o password errati");
		
		User user = database.get(username);
		if (user.checkpassword(password))
		{
			if (user.isLogged())
				throw new UserAlreadyLogged("Utente già loggato");
			else
				user.login();
		} else
			throw new IllegalArgumentException("Utente o password errati");
		
	}
	
	public void logout(String username)
	{
		User user = database.get(username);
		user.logout();
	}
	
	private void sendResponse(int err)
	{
	
	}
	
	private void handleOperation(ByteBuffer buffer)
	{
		
		Gson gson = new Gson();
		//String json = buffer.toString();
		String json = new String(buffer.array(), StandardCharsets.UTF_8);
		System.out.println(json);
		JsonObj obj = gson.fromJson(json.trim(), JsonObj.class);
		//System.out.println(obj.op);
		//System.out.println(obj.username);
		//System.out.println(obj.passwd);
		try
		{
			switch (obj.op)
			{
				case "login":
					this.login(obj.username, obj.passwd);
					break;
				case "logout":
					this.logout(obj.username);
					break;
				case "addfriend":
					
					break;
				case "friendlist":
					
					break;
				
			}
		} catch (UserAlreadyLogged ex)
		{
			System.out.println("User already logged");
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	private static void write(SelectionKey key) throws IOException
	{
		SocketChannel client = (SocketChannel) key.channel();
		/*ClientState state = (ClientState) key.attachment();
		ByteBuffer [] toSend = state.toSend.toArray(new ByteBuffer [1]);
		client.write(toSend);
		int i = 0;
		while(i < state.toSend.size())
		{
			if(state.toSend.get(i).hasRemaining())
				break;
			else
				state.toSend.remove(i);
			i++;
		}
		if(state.stillToRead)
			client.register(selector,
				state.toSend.size() > 0 ?
					SelectionKey.OP_READ | SelectionKey.OP_WRITE :
					SelectionKey.OP_READ,
				state);
		else if(state.toSend.size() > 0)
			client.register(selector, SelectionKey.OP_WRITE, state);
		else
			client.close();*/
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
			handleOperation(buffer);
			
		}
		//buffer.flip();
		
	}
	
	private static void accept(SelectionKey key) throws IOException
	{
		SocketChannel client = socket.accept();
		System.out.println("New Client connected");
		client.configureBlocking(false);
		//ClientState state = new ClientState();
		client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, null);
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
			s = handler.fromFile(db.getPath());
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
	

