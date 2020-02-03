import com.google.gson.Gson;
import javafx.application.Platform;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UdpListener extends Thread
{
	private DatagramSocket socket;
	private static Userhome userhome;
	
	public Userhome getUserhome()
	{
		return userhome;
	}
	
	public static void setUserhome(Userhome userhome)
	{
		UdpListener.userhome = userhome;
	}
	
	//private port
	private void challengeShow(String friend)
	{
		Platform.runLater(() ->
		{
			try
			{
				userhome.challengeShow(friend);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}
	
	public UdpListener(int port) throws SocketException
	{
		socket = new DatagramSocket(port);
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
			Gson gson = new Gson();
			JsonObj obj = gson.fromJson(json.trim(), JsonObj.class);
			
			System.out.println(json);
			try
			{
				challengeShow(obj.getFriend());
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			
			//send reply via TCP
			//DatagramPacket reply = new DatagramPacket(buffer, buffer.length, clientHost, clientPort);
			//socket.send(reply);
			
			
		}
	}
}
