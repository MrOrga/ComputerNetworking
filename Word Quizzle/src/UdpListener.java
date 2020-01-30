import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UdpListener extends Thread
{
	private DatagramSocket socket;
	
	
	public UdpListener(int port) throws SocketException
	{
		socket = new DatagramSocket();
	}
	
	@Override
	public void run()
	{
		System.out.println("UDP thread started");
		
		while (true)
		{
			DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
			//waiting for request from client
			try
			{
				socket.receive(request);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			InetAddress clientHost = request.getAddress();
			int clientPort = request.getPort();
			byte[] buffer = request.getData();
			
			System.out.println("UDP REQUEST");
			
			
			String json = new String(request.getData(), 0, request.getLength(), StandardCharsets.UTF_8);
			
			System.out.println(json);
			
			
			Gson gson = new Gson();
			JsonObj obj = gson.fromJson(json, JsonObj.class);
			
			
			//send reply via TCP
			//DatagramPacket reply = new DatagramPacket(buffer, buffer.length, clientHost, clientPort);
			//socket.send(reply);
			
			
		}
	}
}
