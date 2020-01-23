import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SelectorT implements Runnable
{
	private JsonObj obj;
	private int portTcp = 60501;
	private ControllerLogin controllerLogin;
	private ActionEvent event;
	
	public void setObj(JsonObj obj)
	{
		this.obj = obj;
	}
	
	public JsonObj getObj()
	{
		return obj;
	}
	
	public SelectorT(JsonObj obj, ControllerLogin controllerLogin, ActionEvent event)
	{
		this.controllerLogin = controllerLogin;
		this.obj = obj;
		this.event = event;
	}
	
	@Override
	public void run()
	{
		try
		{
			
			
			System.out.println("Thread Selector start");
			Gson gson = new Gson();
			SocketChannel socket = SocketChannel.open();
			socket.configureBlocking(false);
			Selector selector = Selector.open();
			boolean connected = socket.connect(new InetSocketAddress("localhost", portTcp));
			String json = gson.toJson(obj);
			System.out.println(json);
			//String jsend = new String(json, StandardCharsets.UTF_8);
			//ByteBuffer toSend = ByteBuffer.wrap(gson.toJson(obj).getBytes());
			ByteBuffer toSend = ByteBuffer.wrap(json.getBytes());
			if (!connected)
				socket.register(selector, SelectionKey.OP_CONNECT);
			else
				socket.register(selector, SelectionKey.OP_WRITE);
			while (true)
			{
				selector.select();
				for (SelectionKey key : selector.selectedKeys())
				{
					if (key.isConnectable())
					{
						connected = socket.finishConnect();
						if (!connected)
							socket.register(selector, SelectionKey.OP_CONNECT);
						else
							socket.register(selector, SelectionKey.OP_WRITE);
					}
					if (key.isReadable())
					{
						ByteBuffer buf = ByteBuffer.allocate(512);
						int read = socket.read(buf);
						if (read == -1)
							return;
						buf.flip();
						
						//running javafx code to changing scene
						Platform.runLater(() ->
						{
							try
							{
								controllerLogin.goToUserHome(event);
							} catch (Exception e)
							{
								e.printStackTrace();
							}
						});
						//System.out.print(new String(buf.array(), 0, buf.limit()));
						//gestione esito op
					}
					if (key.isWritable())
					{
						socket.write(toSend);
						if (toSend.hasRemaining())
							socket.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
						else
						{
							socket.register(selector, SelectionKey.OP_READ);
							socket.shutdownOutput();
							
							
						}
					}
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	
}
