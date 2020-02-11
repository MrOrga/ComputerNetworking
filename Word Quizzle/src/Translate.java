import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Translate extends Thread
{
	private ArrayList<String> challengeWordEn;
	private ArrayList<String> challengeWordIt;
	private Challenge challenge;
	//private Thread tChallenge;
	
	Translate(Challenge challenge)
	{
		
		this.challenge = challenge;
		this.challengeWordEn = challenge.getChallengeWordEn();
		this.challengeWordIt = challenge.getChallengeWordIt();
	}
	
	@Override
	public void run()
	{
		
		try
		{
			String url = "https://aapi.mymemory.translated.net/get?q=";
			URLConnection urlConnection;
			for (int i = 0; i < challengeWordIt.size(); i++)
			{
				URL urlRequest = new URL(url + challengeWordIt.get(i) + "&langpair=it|en");
				InputStream uin = urlRequest.openStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(uin));
				String line;
				String json = "";
				while ((line = in.readLine()) != null)
				{
					json += line;
				}
				if (!json.equals(""))
				{
					//System.out.println(json);
					GsonHandler gsonHandler = new GsonHandler();
					String word = gsonHandler.readWordTranslate(json);
					challengeWordEn.add(word);
				}
				
			}
			challenge.setChallengeWordEn(challengeWordEn);
			System.out.println(challengeWordEn);
		} catch (IOException e)
		{
			challenge.setError(new AtomicBoolean(true));
			challenge.restoreMainSelectorKey();
			e.printStackTrace();
		}
	}
}
