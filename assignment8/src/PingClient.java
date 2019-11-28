import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;

public class PingClient
{
	private static InetAddress server;
	private static int port;
	private static int min = 0;
	private static int max = 0;
	private static float avg = 0;
	private static DecimalFormat df = new DecimalFormat("0.00");
	
	public static void main(String[] args)
	{
		
		if (args.length == 2)
		{
			try
			{
				server = InetAddress.getByName(args[0]);
			} catch (UnknownHostException ex)
			{
				System.out.println("ERR -arg 1 need to be a hostname of a server");
			}
			try
			{
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex)
			{
				System.out.println("ERR -arg 1 need to be a int value");
			}
		} else
			System.out.println("ERR -arg 1 is required and need to be a hostname of a server -arg 2  required and need to be a int");
		
		try
		{
			// Create a datagram socket for receiving and sending UDP packets with the port specified as input
			DatagramSocket socket = new DatagramSocket();
			int packetreceive = 0;
			int packetloss = 0;
			for (int i = 0; i < 10; i++)
			{
				// Timestamp in ms
				long timestamp = new Date().getTime();
				String ping = "PING " + i + " " + timestamp;
				
				//setting data to UDP packet
				byte[] buffer = new byte[1024];
				buffer = ping.getBytes(StandardCharsets.UTF_8);
				DatagramPacket request = new DatagramPacket(buffer, buffer.length, server, port);
				socket.send(request);
				try
				{
					//timeout of 2 secs
					socket.setSoTimeout(2000);
					// Set up an UPD packet for receiving
					DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
					// Try to receive the response from the ping
					socket.receive(response);
					
					/*ByteArrayInputStream bain = new ByteArrayInputStream(buffer, 0, request.getLength());
					DataInputStream dis = new DataInputStream(bain);
					String pings = dis.readUTF();
					System.out.println("Server: " + pings);*/
					// timestamp for when we received the packet
					int RTT = (int) ((int) new Date().getTime() - timestamp);
					if (RTT < min)
						min = RTT;
					if (RTT > max)
						max = RTT;
					avg += RTT;
					System.out.println(ping + " RTT: " + RTT + "ms");
					// Print the packet and the delay
					packetreceive++;
				} catch (IOException e)
				{
					// Print which packet has timed out
					packetloss++;
					System.out.println(ping + " RTT: *");
				}
			}
			avg /= packetreceive;
			packetloss *= 10;
			System.out.println("		---- PING Statistics ----		");
			System.out.println("10 packets transmitted, " + packetreceive + " packets received, " + packetloss + "% packet loss");
			System.out.printf("round-trip (ms) min/avg/max = " + min + "/" + df.format(avg) + "/" + max);
		} catch (SocketException ex)
		{
			ex.printStackTrace();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
	}
}
