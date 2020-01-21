import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeServer
{
	private static final int PORT = 8081;
	/*
	 TimeServer groupIP
	
	 groupIP Indirizzo IP del gruppo multicast deve essere nel range [224.0.0.0 – 239.255.255.255]
	*/
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("usage: java TimeServer groupIP");
			return;
		}
		try
		{
			DatagramSocket Socket = new DatagramSocket();
			
			InetAddress groupIp = InetAddress.getByName(args[0]);
			int i=0;
			while(true)
			{
				// FORMATTAZIONE DI DATA ED ORA D'INVIO
				SimpleDateFormat Format = new SimpleDateFormat("'DATA: 'dd-MM-yyyy 'ORA' HH:mm:ss ");
				
				Date date = new Date(System.currentTimeMillis());
				
				byte[] CurrentDate = Format.format(date).getBytes();
				
				//INVIO DEL PACCHETTO
				DatagramPacket Packet = new DatagramPacket(CurrentDate,CurrentDate.length,groupIp,PORT);
				
				Socket.send(Packet);
				System.out.println("invio : "+(i++));
				//INTERVALLO DI TRE SECONDI TRA UN INVIO E L'ALTRO
				Thread.sleep(3000);
			}
		}
		catch (IOException ex)
		{
			System.out.println("ERRORE: Errore di I/O relativo al socket");
			ex.printStackTrace();
		}
		catch (InterruptedException ex)
		{
			System.out.println("ERRORE: Interruzione avvenuta nell'intervallo di tempo tra un invio ed il successivo");
			ex.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
}

