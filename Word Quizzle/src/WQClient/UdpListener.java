package WQClient;

import Utils.JsonObj;
import com.google.gson.Gson;
import javafx.application.Platform;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpListener extends Thread
{
	private DatagramSocket socket;
	private static Userhome userhome;
	private volatile static AtomicBoolean canAccept = new AtomicBoolean(true);
	private static volatile boolean run = true;
	public static ScheduledExecutorService acceptTimer;
	
	public static void setRun(boolean run)
	{
		
		UdpListener.run = run;
	}
	
	
	public Userhome getUserhome()
	{
		return userhome;
	}
	
	public static void setUserhome(Userhome userhome)
	{
		UdpListener.userhome = userhome;
	}
	
	public static AtomicBoolean getCanAccept()
	{
		return canAccept;
	}
	
	public static void setCanAccept(AtomicBoolean canAccept)
	{
		UdpListener.canAccept = canAccept;
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
		run = true;
		socket = new DatagramSocket(port);
	}
	
	@Override
	public void run()
	{
		System.out.println("UDP thread started");
		
		while (run)
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
				
				if (canAccept.get())
				{
					canAccept.set(false);
					challengeShow(obj.getFriend());
					acceptTimer = Executors.newScheduledThreadPool(1);
					acceptTimer.schedule(UdpListener::resetAccept, 15, TimeUnit.SECONDS);
				}
				
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			
		}
	}
	
	public static void resetAccept()
	{
		Platform.runLater(() ->
		{
			try
			{
				if (!canAccept.get())
				{
					
					userhome.notificationReset();
					canAccept.set(true);
					acceptTimer.shutdownNow();
				}
				
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
		
	}
}
