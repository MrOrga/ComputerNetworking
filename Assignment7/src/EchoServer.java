import java.nio.channels.ServerSocketChannel;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class EchoServer
{
	private static int port = 8001;
	private static int dimBuffer = 1024;
	
	public static void main(String[] args) throws IOException
	{
		//creation server socket non blocking
		ServerSocketChannel server = ServerSocketChannel.open();
		server.configureBlocking(false);
		server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		server.socket().bind(new java.net.InetSocketAddress(port));
		System.out.println("Server: active on port : " + port + "....");
		
		//creation selector
		Selector selector = Selector.open();
		server.register(selector, SelectionKey.OP_ACCEPT, null);
		
		
		while (true)
		{
			selector.select();
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
			
			while (keyIterator.hasNext())
			{
				SelectionKey key = (SelectionKey) keyIterator.next();
				keyIterator.remove();
				if (key.isAcceptable())
				{
					//channel for client that has generated the event
					ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
					System.out.println("Server: accept is ready....");
					SocketChannel client = serverSocketChannel.accept();
					System.out.println("Accepted connection from " + client);
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ);
					// a connection was accepted by a ServerSocketChannel.
				} else if (key.isConnectable())
				{
					SocketChannel socketChannel = (SocketChannel) key.channel();
					System.out.println("Server: connect is ready....");
					// a connection was established with a remote server.
				} else if (key.isReadable())
				{
					String read = null;
					SocketChannel socketChannel = (SocketChannel) key.channel();
					if (key.attachment() != null)
						read = (String) key.attachment();
					if (read == null)
						read = "";
					ByteBuffer input = ByteBuffer.allocate(dimBuffer);
					input.clear();
					int byteRead = socketChannel.read(input);
					//check error on read
					if (byteRead == -1)
					{
						read += " echoed by server";
						key.interestOps(SelectionKey.OP_WRITE).attach(ByteBuffer.wrap(read.getBytes()));
					}
					//buffer full ,attach the string read for the next cycle
					else if (byteRead == dimBuffer)
					{
						System.out.println("Server: reading " + byteRead + "bytes");
						input.flip();
						read += StandardCharsets.UTF_8.decode(input).toString();
						key.attach(read);
					} else if (byteRead < dimBuffer)
					{
						System.out.println("Server: reading " + byteRead + "bytes");
						input.flip();
						read += StandardCharsets.UTF_8.decode(input).toString() + " echoed by server";
						System.out.println("Server: " + read);
						key.interestOps(SelectionKey.OP_WRITE).attach(ByteBuffer.wrap(read.getBytes()));
						
					}
					// a channel is ready for reading
				} else if (key.isWritable())
				{
					System.out.println("Server: writing data....");
					SocketChannel socketChannel = (SocketChannel) key.channel();
					ByteBuffer output = (ByteBuffer) key.attachment();
					socketChannel.write(output);
					output.clear();
					socketChannel.close();
					// a channel is ready for writing }
				}
			}
		}
	}
}
