import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class GsonHandler
{
	private Gson gson;
	
	public GsonHandler()
	{
		gson = new Gson();
	}
	
	//saving db into json file
	public void tofile(DatabaseServer db, String path) throws IOException
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
}

