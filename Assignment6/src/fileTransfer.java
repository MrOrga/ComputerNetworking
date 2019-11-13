import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.StringTokenizer;
import java.net.URLDecoder;

public class fileTransfer
{
	private ServerSocket servSock;
	private boolean serverIsRunning;
	private Path root;
	private static int port = 6789;
	
	public fileTransfer() throws IOException
	{
		servSock = new ServerSocket(port);
		System.out.println("Server started.\nListening for connections on port : " + port + " ...\n");
		serverIsRunning = true;
		root = Paths.get("");
	}
	
	
	public static void main(String[] args)
	{
		try
		{
			fileTransfer ft = new fileTransfer();
			while (ft.serverIsRunning)
			{
				Socket socket = ft.servSock.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				// get first line of the request from the client
				String input = in.readLine();
				
				//parsing request
				StringTokenizer parse = new StringTokenizer(input);
				//StringTokenizer parseName = new StringTokenizer(input, "/");
				//http method
				String method = parse.nextToken().toUpperCase();
				//file requested
				String fileRequested = parse.nextToken().toLowerCase();
				fileRequested = fileRequested.substring(1, fileRequested.length());
				fileRequested = URLDecoder.decode(fileRequested, StandardCharsets.UTF_8);
				
				
				//handle request
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				
				if (!method.equals("GET"))
				{
					//  send HTTP Headers with no data to client
					out.writeBytes("HTTP/1.1 501 Not Implemented\r\n");
					out.writeBytes("Date: " + new Date() + "\r\n");
					out.flush();
				} else
				{
					File file = new File(ft.root.toAbsolutePath().toString(), fileRequested);
					int fileLength = (int) file.length();
					String contentType = "multipart/form-data";
					byte[] fileData = readFileData(file, fileLength);
					out.writeBytes("HTTP/1.1 200 OK\r\n");
					out.writeBytes("Connection: keep-alive\r\n");
					out.writeBytes("Date: " + new Date() + "\r\n");
					out.writeBytes("Content-type: " + contentType + "\r\n");
					out.writeBytes("Content-length: " + fileLength + "\r\n\r\n");
					out.write(fileData);
					out.flush();
				}
				
				
				socket.close();
				
			}
			
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	private static byte[] readFileData(File file, int fileLength) throws IOException
	{
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try
		{
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally
		{
			if (fileIn != null)
				fileIn.close();
		}
		
		return fileData;
	}
	
}


