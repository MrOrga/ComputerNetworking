import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

public class ServerRMI extends RemoteServer implements Congresso
{
	
	public static final int NO_SESSION = 12;
	public static final int NO_DAYS = 3;
	public static final int MAX_SPEAKER = 5;
	
	private static final String VOID_SPEAKER = null;
	
	//utilizzo una matrice di stringhe come struttura per memorizzare il programma
	
	private final String[][][] programma;
	
	public ServerRMI() throws RemoteException
	{
		programma = new String[NO_DAYS][NO_SESSION][MAX_SPEAKER];
		/* inizializzazione della matrice a null */
		for (int i = 0; i < NO_DAYS; i++)
			for (int j = 0; j < NO_SESSION; j++)
				for (int z = 0; z < MAX_SPEAKER; z++)
					programma[i][j][z] = VOID_SPEAKER;
	}
	
	@Override
	public int register(int day, int session, String name) throws FullSessionException, RemoteException
	{
		if (name == null)
			throw new IllegalArgumentException();
		if ((session < 1) || (session > NO_SESSION))
			throw new IllegalArgumentException();
		if ((day < 1) || (day > NO_DAYS))
			throw new IllegalArgumentException();
		//decremento day e session perchè gli array partono da 0
		day--;
		session--;
		for (int i = 0; i < MAX_SPEAKER; i++)
			//se ci sta un posto disponibile per il giorno e la sessione richiesti lo assegno
			if (programma[day][session][i] == VOID_SPEAKER)
			{
				programma[day][session][i] = name;
				return i + 1;
			}
		
		//se non c'è un posto disponibile lancio un eccezione
		throw new FullSessionException();
	}
	
	@Override
	public String[][][] getProgramma() throws RemoteException
	{
		return programma;
	}
	
	//ServerRMI port
	//port int numero porta
	public static void main(String[] args) throws RemoteException
	{
		
		if (args.length < 1)
		{
			System.out.println("usage: java ServerRMI port");
			return;
		}
		try
		{
			/* Creazione di un'istanza dell'oggetto ServerRMI */
			ServerRMI srv = new ServerRMI();
			int port = Integer.parseInt(args[0]);
			/* Esportazione dell'Oggetto */
			Congresso stub = (Congresso) UnicastRemoteObject.exportObject(srv, 0);
			
			/* esportazione oggetto che bisogna fare perchè ho scelto di estendere RemoteServer */
			LocateRegistry.createRegistry(port);
			Registry r = LocateRegistry.getRegistry(port);
			/* Pubblicazione dello stub nel registry */
			r.rebind(Congresso.SERVICE_NAME, stub);
			System.out.println("Server ready");
		}
		/* If any communication failures occur... */ catch (RemoteException e)
		{
			System.out.println("Communication error " + e.toString());
		}
	}
}

