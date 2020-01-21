import java.rmi.*;


public interface Congresso extends Remote
{
	public static final String SERVICE_NAME = "CongressoService";
	
	/**
	 * Registrazione ad una sessione del congresso come speaker.
	 *
	 * @return il numero di intervento assegnato dal server
	 * @throws FullSessionException se la sessione scelta è già piena
	 * @throws RemoteException      se si verifica un errore remoto
	 */
	public int register(int day, int session, String name) throws FullSessionException, RemoteException;
	
	/**
	 * Restituisce una vista testuale dell'intero programma del congresso.
	 *
	 * @return una visualizzazione testuale
	 * @throws RemoteException se si verifica un errore remoto
	 */
	public String[][][] getProgramma() throws RemoteException;
	
	//classe per gestire la sessione piena
	public class FullSessionException extends Exception
	{
		
		public FullSessionException()
		{
			super();
		}
		
		public FullSessionException(String message)
		{
			super(message);
		}
		
	}
}