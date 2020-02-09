import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class GsonHandler
{
	private Gson gson;
	
	public GsonHandler()
	{
		gson = new Gson();
	}
	
	//saving db into json file
	public synchronized void tofile(DatabaseServer db, String path) throws IOException
	{
		//gson.toJson(db, new FileWriter("C:\\db.json"));...
		String json = gson.toJson(db);
		System.out.println(json);
		FileWriter fileWriter = new FileWriter(path);
		gson.toJson(db, fileWriter);
		fileWriter.flush();
		fileWriter.close();
		
	}
	
	//restore db from json file
	public DatabaseServer fromFile(String path) throws IOException
	{
		FileReader fileReader = new FileReader(path);
		DatabaseServer db = gson.fromJson(fileReader, DatabaseServer.class);
		fileReader.close();
		return db;
	}
	
	public JsonObj readFromGson(ByteBuffer buffer)
	{
		System.out.println("readfromGson");
		buffer.flip();
		String json = new String(buffer.array(), StandardCharsets.UTF_8);
		System.out.println(json);
		
		return gson.fromJson(json.trim(), JsonObj.class);
	}
	
	public String readWordTranslate(String json) throws IOException
	{
		String word = "";
		JsonReader jsonReader = new JsonReader(new StringReader(json));
		
		try
		{
			while (jsonReader.hasNext())
			{
				JsonToken nextToken = jsonReader.peek();
				//System.out.println(nextToken);
				
				if (JsonToken.BEGIN_OBJECT.equals(nextToken))
				{
					
					jsonReader.beginObject();
					
				} else if (JsonToken.NAME.equals(nextToken))
				{
					
					String name = jsonReader.nextName();
					//System.out.println(name);
					
				} else if (JsonToken.STRING.equals(nextToken))
				{
					
					String value = jsonReader.nextString();
					//System.out.println(value);
					return value;
					
				} else if (JsonToken.NUMBER.equals(nextToken))
				{
					
					long value = jsonReader.nextLong();
					//System.out.println(value);
					
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return word;
	}
	
	public List<String> fromDiz(String path) throws IOException
	{
		FileReader fileReader = new FileReader(path);
		List<String> word = gson.fromJson(fileReader, List.class);
		fileReader.close();
		return word;
	}
}

