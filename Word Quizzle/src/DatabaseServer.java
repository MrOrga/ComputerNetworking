import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import Exception.*;


public class DatabaseServer extends RemoteServer implements Database
{
	//concurrent hash map per memorizzare user
	private ConcurrentHashMap<String, User> database;
	
	public DatabaseServer()
	{
		this.database = new ConcurrentHashMap<String, User>();
	}
	
	@Override
	public int register(String user, String password) throws UserAlreadyExist, RemoteException
	{
		if (database.containsKey(user))
			throw new UserAlreadyExist("User già esistente");
		if (password == null)
			throw new IllegalArgumentException("La password non può essere nulla");
		
		database.put(user, new User(user, password));
		System.out.println("Register user: " + user + "password: " + password);
		return 0;
	}
	
	public void login(String username, String password) throws UserAlreadyLogged, IllegalArgumentException
	{
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
	
	public static void main(String[] args) throws RemoteException
	{
		
		int port = 60500;
		try
		{
			/* Creazione di un'istanza dell'oggetto ServerRMI */
			DatabaseServer srv = new DatabaseServer();
			//int port = Integer.parseInt(args[0]);
			/* Esportazione dell'Oggetto */
			Database stub = (Database) UnicastRemoteObject.exportObject(srv, 0);
			
			/* esportazione oggetto che bisogna fare perchè ho scelto di estendere RemoteServer */
			LocateRegistry.createRegistry(port);
			Registry r = LocateRegistry.getRegistry(port);
			/* Pubblicazione dello stub nel registry */
			r.rebind("DatabaseService", stub);
			System.out.println("Server ready");
		}
		/* If any communication failures occur... */ catch (RemoteException e)
		{
			System.out.println("Communication error " + e.toString());
		}
	}
	
}
	

