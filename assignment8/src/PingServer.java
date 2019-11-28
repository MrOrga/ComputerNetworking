import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

public class PingServer
{
	private static long seed = 25;
	private static int port;
	private static final double loss = 0.25;
	
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("ERR -arg 1 is required");
			return;
		} else if (args.length == 2)
		{
			try
			{
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException ex)
			{
				System.out.println("ERR -arg 1 need to be a int value");
			}
			try
			{
				seed = Long.parseLong(args[1]);
			} catch (NumberFormatException ex)
			{
				System.out.println("ERR -arg 2 need to be a long value");
			}
		} else if (args.length != 1)
		{
			System.out.println("ERR -arg 1 is required and need to be a int -arg 2 is optional and need to be a long");
			return;
		}
		
		//rand generator for packet loss
		Random rand = new Random(seed);
		
		
		try
		{
			// Create a datagram socket for receiving and sending UDP packets with the port specified as input
			DatagramSocket socket = new DatagramSocket(port);
			
			while (true)
			{
				DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
				//waiting for request from client
				socket.receive(request);
				InetAddress clientHost = request.getAddress();
				int clientPort = request.getPort();
				byte[] buffer = request.getData();
				
				//print data and message
				System.out.println("Ip address client: " + clientHost.getHostAddress() + " port: " + port);
				
				ByteArrayInputStream bain = new ByteArrayInputStream(buffer, 0, request.getLength());
				DataInputStream dis = new DataInputStream(bain);
				String ping = dis.readUTF();
				System.out.println("Server: " + ping);
				
				//reply or simulate loss
				if (rand.nextDouble() < loss)
				{
					System.out.println("Packet loss.");
					continue;
				}
				
				// Simulate network delay
				Thread.sleep((int) (rand.nextDouble() * 100));
				
				//send reply
				
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length, clientHost, clientPort);
				socket.send(reply);
				
				System.out.println("Reply sent.");
				
			}
		} catch (SocketException ex)
		{
			ex.printStackTrace();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		} catch (InterruptedException ex)
		{
			ex.printStackTrace();
		}
		
		
	}
}
