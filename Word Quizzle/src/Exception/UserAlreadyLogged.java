package Exception;

//classe per gestire utente già esistente

public class UserAlreadyLogged extends Exception
{
	
	public UserAlreadyLogged()
	{
		super();
	}
	
	public UserAlreadyLogged(String message)
	{
		super(message);
	}
	
}