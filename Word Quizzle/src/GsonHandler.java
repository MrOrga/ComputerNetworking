import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


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
		gson.toJson(db, new FileWriter(path));
	}
	
	//restore db from json file
	public DatabaseServer fromFile(String path) throws IOException
	{
		return gson.fromJson(new FileReader(path), DatabaseServer.class);
	}
}

