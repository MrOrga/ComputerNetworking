package WQServer;

import java.io.IOException;
import java.rmi.*;
import java.rmi.Remote;


public interface Database extends Remote
{
	public static final String SERVICE_NAME = "DatabaseService";
	
	/**
	 * Registrazione di un utente nel database
	 *
	 * @return
	 * @throws RemoteException se si verifica un errore remoto
	 */
	public int register(String user, String password) throws IOException;
	
}

