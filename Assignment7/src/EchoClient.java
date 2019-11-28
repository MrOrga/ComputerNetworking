import java.nio.channels.ServerSocketChannel;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EchoClient
{
	private static int port = 8001;
	private static int dimBuffer = 1024;
	
	public static void main(String[] args) throws IOException
	{
		SocketAddress address = new InetSocketAddress(port);
		SocketChannel client = SocketChannel.open(address);
		System.out.println("Client: active on port : " + port + "....");
		String send = "test";
		if (args.length > 0)
			send = args[0];
		client.write(ByteBuffer.wrap(send.getBytes()));
		System.out.println("Client: string sent : " + send);
		client.shutdownOutput();
		System.out.println("Client: reading mode ....");
		boolean cond = true;
		ByteBuffer input = ByteBuffer.allocate(dimBuffer);
		StringBuilder recv = new StringBuilder();
		while (cond)
		{
			input.clear();
			int byteRead = client.read(input);
			input.flip();
			recv.append(StandardCharsets.UTF_8.decode(input).toString());
			if (byteRead < dimBuffer)
				cond = false;
			
		}
		System.out.println("Client: " + recv);
		
	}
	
}
