package WQServer;

import java.io.*;
import java.net.*;

import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import java.util.concurrent.*;


import Utils.*;
import com.google.gson.Gson;


public class Server extends RemoteServer implements Database, Serializable
{
	//concurrent hash map to save user info
	private ConcurrentHashMap<String, User> database;
	//socket map to save connected user information
	private transient ConcurrentHashMap<String, UserHandler> socketmap;
	//need for retrieve the username from the socket
	private transient ConcurrentHashMap<SocketChannel, String> usersocketmap;
	private static ThreadPoolExecutor challengeExecutor;
	private static LinkedBlockingQueue<Runnable> myQueueChallenge;
	
	
	private static ServerSocketChannel socket;
	private static Selector selector;
	private transient SelectionKey currentKey;
	private transient JsonObj obj;
	private transient String currentUser;
	
	public Server()
	{
		database = new ConcurrentHashMap<String, User>();
		socketmap = new ConcurrentHashMap<String, UserHandler>();
		usersocketmap = new ConcurrentHashMap<SocketChannel, String>();
		myQueueChallenge = new LinkedBlockingQueue<Runnable>();
		//maxthread 50, max time in idle 2minutes
		challengeExecutor = new ThreadPoolExecutor(0, 100, 2, TimeUnit.MINUTES, myQueueChallenge);
	}
	
	
	public int register(String user, String password) throws IOException
	{
		if (password == null || user == null)
		{
			
			//obj = new Utils.JsonObj("400 IllegalArgumentException");
			return -400;
			
		}
		if (database.containsKey(user))
		{
			//obj = new Utils.JsonObj("401 UserAlreadyExist");
			
			return -401;
			
			
		}
		
		
		database.put(user, new User(user, password));
		System.out.println("Register user: " + user + "password: " + password);
		
		//saving db into file
		GsonHandler handler = new GsonHandler();
		handler.tofile(this, "db.json");
		
		
		return 0;
	}
	
	public void login(String username, String password, SocketChannel client) throws IOException
	{
		GsonHandler handler = new GsonHandler();
		
		System.out.println(username);
		if (!database.containsKey(username))
		{
			obj = new JsonObj("400 IllegalArgumentException");
			sendResponse(obj, client, currentUser);
		}
		
		User user = database.get(username);
		if (user.checkpassword(password))
		{
			if (user.isLogged())
			{
				obj = new JsonObj("401 UserAlreadyLogged");
				sendResponse(obj, client, currentUser);
			} else
			{
				user.login();
				//registration of socket in the socketmap
				socketmap.put(username, new UserHandler(client, username));
				usersocketmap.put(client, username);
				JsonObj obj1 = new JsonObj("200 OK LOGIN");
				obj1.setUsername(username);
				sendResponse(obj1, client, currentUser);
				handler.tofile(this, "db.json");
			}
			
		} else
		{
			obj = new JsonObj("400 IllegalArgumentException");
			sendResponse(obj, client, currentUser);
		}
		
	}
	
	public void logout(String username, SocketChannel client) throws IOException
	{
		currentUser = usersocketmap.get(client);
		User user = database.get(username);
		user.logout();
		sendResponse(new JsonObj("204 OK LOGOUT"), client, currentUser);
		
	}
	
	public void addFriend(String username, String friend, SocketChannel client) throws IOException
	{
		//check if the user and the user of a friend exist
		if ((!database.containsKey(username) || !(database.containsKey(friend))))
		{
			sendResponse(new JsonObj("410 Utils.User not exist"), client, currentUser);
			
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
				database.get(friend).setFriend(username);
				sendResponse(new JsonObj("202 Utils.User added to friend list"), client, currentUser);
				GsonHandler handler = new GsonHandler();
				handler.tofile(this, "db.json");
			}
		}
	}
	
	private void friendlist(String username, SocketChannel client) throws IOException
	{
		Gson gson = new Gson();
		Vector<String> friend = database.get(username).getFriend();
		JsonObj obj1 = new JsonObj("203 showfriendlist");
		obj1.setFriendlist(friend);
		sendResponse(obj1, client, currentUser);
		
	}
	
	private void friendlistRank(String username, SocketChannel client) throws IOException
	{
		Vector<String> friends = database.get(username).getFriend();
		Vector<String> users = new Vector<String>(friends);
		users.add(0, username);
		Vector<Integer> scores = new Vector<Integer>();
		scores.add(0, database.get(username).getPoint());
		for (int i = 1; i < users.size(); i++)
		{
			scores.add(database.get(users.get(i)).getPoint());
		}
		Sort sort = new Sort(scores, users);
		sort.sort();
		users = sort.getUsers();
		scores = sort.getScores();
		JsonObj obj1 = new JsonObj("205 showfriendlistScore");
		obj1.setFriendlist(users);
		obj1.setScores(scores);
		sendResponse(obj1, client, currentUser);
		
	}
	
	public void sendResponse(JsonObj obj, SocketChannel client, String user) throws IOException
	{
		this.obj = obj;
		Gson gson = new Gson();
		String json = gson.toJson(obj);
		System.out.println(json);
		if (socketmap.get(user) != null)
		{
			socketmap.get(user).setToSend(ByteBuffer.wrap(json.getBytes()));
			client.register(selector, SelectionKey.OP_WRITE);
		} else
		{
			client.register(selector, SelectionKey.OP_WRITE, new Attachment(json, json.length(), 0));
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
				case "leaderboard":
					this.friendlistRank(currentUser, client);
					break;
				case "challenge":
					this.challenge(friend, client);
					break;
				case "accept":
					this.acceptChallenge(currentUser, friend, client);
					break;
				case "score":
					this.score(currentUser, client);
					break;
				
				
			}
		} catch (Exception ex)
		{
			//ex.printStackTrace();
		}
		
	}
	
	private void score(String currentUser, SocketChannel client) throws IOException
	{
		JsonObj obj1 = new JsonObj("206 score");
		obj1.setPoints(database.get(currentUser).getPoint());
		sendResponse(obj1, client, currentUser);
	}
	
	
	private void write(SelectionKey key) throws IOException
	{
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer toSend;
		if (socketmap.get(currentUser) != null)
			toSend = socketmap.get(currentUser).getToSend();
			//need for error on login
		else
		{
			Attachment attachment = (Attachment) key.attachment();
			toSend = ByteBuffer.wrap(attachment.getRes().getBytes());
		}
		System.out.println("faccio write");
		
		int len = toSend.array().length;
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
			key.cancel();
			return;
		}
		
		
		if (attachment == null)
		{
			try
			{
				len = buf.getInt();
			} catch (Exception e)
			{
				key.cancel();
				return;
				/*key.cancel();
				client.close();*/
			}
		}
		
		//System.out.println(len);
		if (key.attachment() == null)
		{
			res = new String(buf.array(), buf.position(), buf.remaining(), StandardCharsets.UTF_8);
			attachment = new Attachment(res, len, len - (read + 4));
			if (attachment.getLeft() <= 0)
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
	
	public String getUsername(SocketChannel user)
	{
		return usersocketmap.get(user);
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
		UserHandler userFriend = socketmap.get(friend);
		UserHandler userCurrent = socketmap.get(friend);
		if (!userCurrent.isBusy() && !userFriend.isBusy())
		{
			System.out.println("WQServer.Challenge accepted");
			
			
			SocketChannel friendSocket = userFriend.getSocket();
			
			//geting the selection key
			SelectionKey keyClient = client.keyFor(selector);
			SelectionKey keyFriend = friendSocket.keyFor(selector);
			//setting interest of the keys to 0
			
			//keyClient.interestOps(0);
			//keyFriend.interestOps(0);
			
			userFriend.setBusy(true);
			userCurrent.setBusy(true);
			
			
			Challenge challenge = new Challenge(selector, socket, this, client, friendSocket, currentUser, friend);
			Future future = challengeExecutor.submit(challenge);
			if (challengeExecutor.getQueue().contains(future))
			{
				JsonObj obj1 = new JsonObj("460 challenge is full, wait");
				sendResponse(obj1, client, currentUser);
				sendResponse(obj1, friendSocket, friend);
			}
			
			
		} else
		{
			sendResponse(new JsonObj("450 Your friend is already in a challenge"), client, currentUser);
		}
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
		Server s;
		
		//restoring database from json file
		if (db.exists())
			s = (Server) handler.fromFile(db.getPath());
		else
			s = new Server();
		
		try
		{
			s.run();
		} catch (Exception ex)
		{
		}
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
			System.out.println("WQServer.Server ready");
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
			} catch (IOException ex)
			{
				currentUser = usersocketmap.get((SocketChannel) currentKey.channel());
				currentKey.cancel();
				socketmap.remove(currentUser);
				database.get(currentUser).logout();
				
				
			}
		}
	}
	
}
	

