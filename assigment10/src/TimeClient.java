

import java.io.*;
import java.net.*;

public class TimeClient
{
	
	private static final int PORT = 8081;
	/*
	 TimeCLient groupIP
	
	 groupIP Indirizzo IP del gruppo multicast deve essere nel range [224.0.0.0 – 239.255.255.255]
	*/
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("usage: java TimeClient groupIP");
			return;
		}
		//setto la versione ipV4 per protocollo IP
		System.setProperty("java.net.preferIPv4Stack","true");
		try
		{
			//multicast socket per il gruppo alla porta 8081
			MulticastSocket Socket = new MulticastSocket(PORT);
			
			//L'indirizzo IP del gruppo è preso da riga di comando che deve essere nel range [224.0.0.0 – 239.255.255.255]
			InetAddress groupIp = InetAddress.getByName(args[0]);
			
			//Il client si unisce al gruppo
			Socket.joinGroup(groupIp);
			
			byte[] Buffer = new byte[1024];
			
			int i=0;
			
			while(i < 10)
			{
				DatagramPacket Packet = new DatagramPacket(Buffer,Buffer.length);
				
				//Ricezione del paccketto
				Socket.receive(Packet);
				
				String RCV = new String(Packet.getData());
				
				System.out.println(RCV);
				
				i++;
			}
			
			//il client lascia il gruppo e chiude la sua connessione
			Socket.leaveGroup(groupIp);
			Socket.close();
			
		}
		catch (UnknownHostException ex)
		{
			System.out.println("ERRORE: Indirizzo sconosciuto - "+args[0]);
			ex.printStackTrace();
		}
		catch (SocketException ex)
		{
			System.out.println("ERRORE: Errore di apertura socket");
			ex.printStackTrace();
		}
		catch (IOException ex)
		{
			System.out.println("ERRORE: Errore di I/O");
			ex.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		
	}
}