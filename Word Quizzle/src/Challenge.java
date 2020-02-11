import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Challenge extends Thread
{
	private Selector selector;
	private volatile boolean isFinishied = false;
	private volatile AtomicBoolean error = new AtomicBoolean(false);
	
	public void setError(AtomicBoolean error)
	{
		this.error = error;
	}
	
	private volatile int lastWrite = 0;
	private Selector challengeSelector;
	private SocketChannel player1;
	private SocketChannel player2;
	private SelectionKey keyPlayer1;
	private SelectionKey keyPlayer2;
	private String userPlayer1;
	private String userPlayer2;
	private ServerSocketChannel socket;
	private DatabaseServer server;
	private String currentUser;
	private ScheduledExecutorService challengeTimer;
	private List<String> wordIt;
	private ArrayList<String> challengeWordIt;
	private Translate translate;
	private ArrayList<String> challengeWordEnPlayer1;
	private ArrayList<String> challengeWordEnPlayer2;
	private int pointPlayer1;
	private int pointPlayer2;
	private SelectionKey currentKey;
	
	public ArrayList<String> getChallengeWordIt()
	{
		return challengeWordIt;
	}
	
	public void setChallengeWordIt(ArrayList<String> challengeWordIt)
	{
		this.challengeWordIt = challengeWordIt;
	}
	
	public ArrayList<String> getChallengeWordEn()
	{
		return challengeWordEn;
	}
	
	public void setChallengeWordEn(ArrayList<String> challengeWordEn)
	{
		this.challengeWordEn = challengeWordEn;
	}
	
	private static ArrayList<String> challengeWordEn;
	private URL url = new URL("https://api.mymemory.translated.net/get?q=");
	
	
	public Challenge(Selector selector, ServerSocketChannel socket, DatabaseServer server, SocketChannel player1, SocketChannel player2, String user1, String user2) throws IOException
	{
		challengeSelector = Selector.open();
		this.player1 = player1;
		this.player2 = player2;
		this.selector = selector;
		keyPlayer1 = player1.keyFor(selector);
		keyPlayer2 = player2.keyFor(selector);
		this.socket = socket;
		this.server = server;
		userPlayer1 = user1;
		userPlayer2 = user2;
		GsonHandler gson = new GsonHandler();
		wordIt = gson.fromDiz("dizionario.json");
		
		challengeWordIt = challengeWordGenerator(wordIt);
		System.out.println(challengeWordIt);
		
		challengeWordEn = new ArrayList<String>();
		challengeWordEnPlayer1 = new ArrayList<String>();
		challengeWordEnPlayer2 = new ArrayList<String>();
		translate = new Translate(this);
		
		translate.start();
		
		challengeTimer = Executors.newScheduledThreadPool(1);
		
	}
	
	@Override
	public void run()
	{
		try
		{
			
			challengeSelector = Selector.open();
			
			//regisration of the channel in the new selector
			SocketChannel client = (SocketChannel) keyPlayer1.channel();
			client.register(challengeSelector, SelectionKey.OP_WRITE);
			SocketChannel client2 = (SocketChannel) keyPlayer2.channel();
			client2.register(challengeSelector, SelectionKey.OP_WRITE);
			
			//sending first word to the client
			JsonObj obj = new JsonObj("210 Challenge started");
			obj.setWord(challengeWordIt.get(0));
			sendResponse(obj, client, userPlayer1);
			sendResponse(obj, client2, userPlayer2);
			
			challengeTimer.schedule(this::restoreMainSelectorKey, 10, TimeUnit.SECONDS);
			
			while (lastWrite != 2)
			{
				try
				{
					System.out.println("selector new");
					challengeSelector.select();
					for (SelectionKey key : challengeSelector.selectedKeys())
					{
						currentKey = key;
						if (key.isValid() && key.isAcceptable())
						{
							accept(key);
						}
						if (key.isValid() && key.isReadable())
						{
							read(key);
						}
						if (key.isValid() && key.isWritable())
						{
							write(key);
						}
					}
					challengeSelector.selectedKeys().clear();
				} catch (SocketException e)
				{
					SocketChannel clientEx = (SocketChannel) currentKey.channel();
					clientEx.close();
					if (clientEx == player1)
					{
						server.getSocketmap().get(userPlayer1).setBusy(false);
						server.getDatabase().get(userPlayer1).logout();
						server.getSocketmap().remove(userPlayer1);
					} else
					{
						server.getSocketmap().get(userPlayer2).setBusy(false);
						server.getDatabase().get(userPlayer2).logout();
						server.getSocketmap().remove(userPlayer2);
					}
					currentKey.cancel();
				}
				
				
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendResponse(JsonObj obj, SocketChannel client, String user) throws IOException
	{
		//this.obj = obj;
		Gson gson = new Gson();
		String json = gson.toJson(obj);
		System.out.println(json);
		server.setToSend(ByteBuffer.wrap(json.getBytes()), user);
		
		client.register(challengeSelector, SelectionKey.OP_WRITE);
		
		
	}
	
	private ArrayList<String> challengeWordGenerator(List<String> list)
	{
		ArrayList<String> challengeList = new ArrayList<String>();
		
		for (int i = 0; i < 10; i++)
		{
			Random random = new Random();
			int rand = random.nextInt(list.size());
			challengeList.add(list.remove(rand));
		}
		
		
		return challengeList;
		
	}
	
	
	public void restoreMainSelectorKey()
	{
		if (!isFinishied)
		{
			System.out.println("Challenge is over");
			//setting interest to 0
			isFinishied = true;
			if (player1.isConnected())
			{
				player1.keyFor(challengeSelector).interestOps(0);
			} else
				lastWrite++;
			if (player2.isConnected())
				player2.keyFor(challengeSelector).interestOps(0);
			else
				lastWrite++;
			try
			{
				if (!error.get())
				{
					if (translate.isAlive())
						translate.join();
					
					System.out.println(challengeWordEn);
					
					//setting the result
					challengeResult();
					
					
					JsonObj obj1 = new JsonObj("213 challenge is over");
					obj1.setChallengePoints(pointPlayer1);
					if (player1.isConnected())
					{
						player1.register(challengeSelector, SelectionKey.OP_WRITE);
						sendResponse(obj1, player1, userPlayer1);
						
						server.getSocketmap().get(userPlayer1).setBusy(false);
					}
					
					obj1.setChallengePoints(pointPlayer2);
					if (player2.isConnected())
					{
						player2.register(challengeSelector, SelectionKey.OP_WRITE);
						sendResponse(obj1, player2, userPlayer2);
						
						server.getSocketmap().get(userPlayer2).setBusy(false);
					}
					
					
					GsonHandler handler = new GsonHandler();
					handler.tofile(server, "db.json");
				} else
				{
					JsonObj obj1 = new JsonObj("451 challenge is over error on server");
					if (player1.isConnected())
					{
						
						player1.register(challengeSelector, SelectionKey.OP_WRITE);
						sendResponse(obj1, player1, userPlayer1);
						
						server.getSocketmap().get(userPlayer1).setBusy(false);
					}
					if (player2.isConnected())
					{
						player2.register(challengeSelector, SelectionKey.OP_WRITE);
						sendResponse(obj1, player2, userPlayer2);
						
						server.getSocketmap().get(userPlayer2).setBusy(false);
					}
					
					System.out.println("Error server traslation");
				}
				challengeSelector.wakeup();
				player1.keyFor(selector).interestOps(SelectionKey.OP_READ);
				player2.keyFor(selector).interestOps(SelectionKey.OP_READ);
				selector.wakeup();
				
				
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
	}
	
	private void challengeResult()
	{
		//counting the point
		pointPlayer1 = 0;
		pointPlayer2 = 0;
		for (int i = 0; i < challengeWordEn.size(); i++)
		{
			if (challengeWordEnPlayer1.size() > i)
			{
				if (challengeWordEnPlayer1.get(i).equals(challengeWordEn.get(i)))
					pointPlayer1++;
			}
			if (challengeWordEnPlayer2.size() > i)
			{
				if (challengeWordEnPlayer2.get(i).equals(challengeWordEn.get(i)))
					pointPlayer2++;
			}
		}
		//add 5 points for the win
		if (pointPlayer2 < pointPlayer1)
			pointPlayer1 += 5;
		if (pointPlayer1 < pointPlayer2)
			pointPlayer2 += 5;
		server.getUser(userPlayer1).addPoint(pointPlayer1);
		server.getUser(userPlayer2).addPoint(pointPlayer2);
	}
	
	private void handleOperation(String json, SocketChannel client)
	{
		
		Gson gson = new Gson();
		
		System.out.println(json.trim());
		JsonObj obj = gson.fromJson(json.trim(), JsonObj.class);
		
		currentUser = obj.getUsername();
		
		try
		{
			switch (obj.getOp())
			{
				
				case "logout":
					server.logout(currentUser, client);
					//closing connection for the player that logout and set interestOps to the other to OP_READ
					if (player1.keyFor(selector) != null && client == player1.keyFor(selector).channel())
					{
						player1.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
						//player2.keyFor(selector).interestOps(SelectionKey.OP_READ);
						selector.wakeup();
					} else if (player2.keyFor(selector) != null && client == player2.keyFor(selector).channel())
					{
						player2.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
						//player1.keyFor(selector).interestOps(SelectionKey.OP_READ);
						selector.wakeup();
					}
					
					//isFinishied = true;
					break;
				case "word translation":
					if (client == player1)
					{
						if (challengeWordEnPlayer1.size() != challengeWordIt.size())
							challengeWordEnPlayer1.add(obj.getWord());
						if (challengeWordEnPlayer1.size() >= challengeWordIt.size())
						{
							
							if ((challengeWordEnPlayer1.size() == challengeWordEnPlayer2.size()) && (challengeWordEnPlayer1.size() == challengeWordIt.size()))
							{
								restoreMainSelectorKey();
								
							} else
							{
								sendResponse(new JsonObj("212 wait opponent"), client, currentUser);
							}
						} else
						{
							String word = challengeWordIt.get(challengeWordEnPlayer1.size());
							sendNewWord(currentUser, client, word);
							
						}
					} else
					{
						if (challengeWordEnPlayer2.size() != challengeWordIt.size())
							challengeWordEnPlayer2.add(obj.getWord());
						if (challengeWordEnPlayer2.size() >= challengeWordIt.size())
						{
							
							if ((challengeWordEnPlayer1.size() == challengeWordEnPlayer2.size()) && (challengeWordEnPlayer1.size() == challengeWordIt.size()))
							{
								restoreMainSelectorKey();
								
							} else
							{
								sendResponse(new JsonObj("212 waiting opponent"), client, currentUser);
							}
						} else
						{
							String word = challengeWordIt.get(challengeWordEnPlayer2.size());
							sendNewWord(currentUser, client, word);
						}
					}
					
					
					break;
				
				
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	private void sendNewWord(String currentUser, SocketChannel client, String word) throws IOException
	{
		JsonObj obj = new JsonObj("211 next word");
		obj.setWord(word);
		sendResponse(obj, client, currentUser);
	}
	
	private void accept(SelectionKey key) throws IOException
	{
		SocketChannel client = socket.accept();
		System.out.println("New Client connected");
		client.configureBlocking(false);
		client.register(challengeSelector, SelectionKey.OP_READ);
	}
	
	private void write(SelectionKey key) throws IOException
	{
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer toSend;
		if (client == player1)
			toSend = server.getToSend(userPlayer1);
		else
			toSend = server.getToSend(userPlayer2);
		System.out.println("Sending Response");
		
		int len = toSend.array().length;
		
		ByteBuffer bufflen = ByteBuffer.allocate(4);
		bufflen.clear();
		bufflen.putInt(len);
		bufflen.flip();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(bufflen.array());
		outputStream.write(toSend.array());
		ByteBuffer newBuff = ByteBuffer.wrap(outputStream.toByteArray());
		
		
		client.write(newBuff);
		if (isFinishied)
		{
			
			lastWrite++;
			if (lastWrite == 2)
				key.cancel();
			
		} else
		{
			client.register(challengeSelector, SelectionKey.OP_READ);
		}
	}
	
	private void read(SelectionKey key) throws IOException
	{
		SocketChannel client = (SocketChannel) key.channel();
		
		ByteBuffer buf = ByteBuffer.allocate(512);
		buf.clear();
		Attachment attachment = null;
		String res = "";
		int len = 0;
		if (key.attachment() != null)
		{
			attachment = (Attachment) key.attachment();
			len = attachment.getLen();
		}
		int read = client.read(buf);
		buf.flip();
		//some error
		if (read == -1)
		{
			client.close();
			return;
		}
		
		
		if (attachment == null)
		{
			len = buf.getInt();
		}
		
		
		//System.out.println(len);
		if (key.attachment() == null)
		{
			res = new String(buf.array(), buf.position(), buf.remaining(), StandardCharsets.UTF_8);
			attachment = new Attachment(res, len, len - read);
			if (len <= buf.remaining())
				handleOperation(res, client);
			else
				client.register(challengeSelector, SelectionKey.OP_READ, attachment);
			
		} else
		{
			if (key.attachment() != null)
			{
				res = attachment.getRes() + new String(buf.array(), StandardCharsets.UTF_8);
				
				attachment = new Attachment(res, len, attachment.getLeft() - read);
				if (attachment.getLeft() > 0)
					
					client.register(challengeSelector, SelectionKey.OP_READ, attachment);
				else
				{
					handleOperation(res, client);
				}
				
			}
			
			
		}
	}
}
