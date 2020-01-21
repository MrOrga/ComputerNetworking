import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


public class ClientRMI
{
	private synchronized static void stampaProgramma(Congresso serverObject)
	{
		try
		{
			String[][][] programma = serverObject.getProgramma();
			String stampa = "";
			for (int i = 0; i < ServerRMI.NO_DAYS; i++)
			{
				System.out.println("---------------------------------------- DAY " + (i + 1) + " ----------------------------------------");
				System.out.println("-----Sessione | Intervento 1 | Intervento 2| Intervento 3 | Intervento 4 | Intervento 5");
				stampa = "";
				for (int j = 0; j < ServerRMI.NO_SESSION; j++)
				{
					stampa += ("-----S" + (j + 1) + "       | ");
					for (int z = 0; z < ServerRMI.MAX_SPEAKER; z++)
					{
						if (programma[i][j][z] != null)
						{
							stampa += programma[i][j][z] + "    | ";
						} else
							stampa += "             | ";
						if (z == 4)
						{
							stampa += "\n";
						}
					}
					
				}
				stampa += "--------------------------------------------------------------------------------------------\n";
				System.out.println(stampa);
			}
		} catch (Exception e)
		{
			System.out.println("Error in invoking object method " + e.toString() + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static synchronized void addSpeaker(Congresso serverObject, int regEffettuate)
	{
		int day = (int) (Math.random() * 3) + 1;
		int session = (int) (Math.random() * 12) + 1;
		try
		{
			serverObject.register(day, session, "speaker" + regEffettuate);
		} catch (Congresso.FullSessionException ex)
		{
			System.out.println("La sessione " + session + " per il giorno richiesto : " + day + " è piena");
		} catch (Exception e)
		{
			System.out.println("Error in invoking object method " + e.toString() + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static synchronized void addSpeaker(Congresso serverObject, int day, int session, String name)
	{
		try
		{
			serverObject.register(day, session, name);
		} catch (Congresso.FullSessionException ex)
		{
			System.out.println("La sessione " + session + " per il giorno richiesto : " + day + " è piena");
		} catch (RemoteException e)
		{
			System.out.println("Error in invoking object method " + e.toString() + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	//ServerRMI port nRegistrazioni
	//port int numero porta
	//nRegistrazioni int numero di registrazioni che effettua il client
	public static void main(String args[])
	{
		Congresso serverObject;
		Remote RemoteObject;
		Scanner scanner = new Scanner(System.in);
		
		/* Check number of arguments */
		/* If not enough, print usage string and exit */
		if (args.length != 1)
		{
			System.out.println("usage: java ClientRMI port");
			return;
		}
		boolean cond = true;
		
		try
		{
			Registry r = LocateRegistry.getRegistry(Integer.parseInt(args[0]));
			RemoteObject = r.lookup(Congresso.SERVICE_NAME);
			serverObject = (Congresso) RemoteObject;
			
			//generazione speaker per sessioni sessioni
			while (cond)
			{
				//stampa menu
				System.out.println("-------------------------------- MENU: --------------------------------");
				System.out.println("selezionare una delle seguenti opzioni: ");
				System.out.println("digitare 1 per aggiungere uno speaker");
				System.out.println("digitare 2 per visualizzare il programma");
				System.out.println("digitare 3 per terminare");
				System.out.println("-----------------------------------------------------------------------");
				int option = Integer.parseInt(scanner.nextLine());
				try
				{
					if (option == 1)
					{
						System.out.println("Inserire nome speaker");
						String name = scanner.nextLine();
						System.out.println("Inserire giorno da 1 a 3 (estremi inclusi)");
						int day = Integer.parseInt(scanner.nextLine());
						System.out.println("Inserire sessione da 1 a 12 (estremi inclusi)");
						int session = Integer.parseInt(scanner.nextLine());
						addSpeaker(serverObject, day, session, name);
					}
				} catch (IllegalArgumentException ex)
				{
					System.out.println("Formato dei dati inserito errato, riprova");
					continue;
				}
				if (option == 2)
					stampaProgramma(serverObject);
				if (option == 3)
				{
					cond = false;
					continue;
				}
				
				
			}
			
			
		} catch (Exception e)
		{
			System.out.println("Error in invoking object method " + e.toString() + e.getMessage());
			e.printStackTrace();
		}
		
	}
}
