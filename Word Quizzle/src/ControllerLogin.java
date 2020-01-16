import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class ControllerLogin
{
	@FXML
	private TextField username;
	@FXML
	private PasswordField passwd;
	
	
	public void loginClick(ActionEvent event)
	{
		//Register of user in the database using RMI
		int port = 60500;
		try
		{
			
			String usrname = username.getText();
			String password = passwd.getText();
			
			//Gson gson = new Gson();
			JsonObj obj = new JsonObj("login", usrname, password);
			SelectorT selector = new SelectorT(obj);
			Thread sel = new Thread(selector);
			sel.start();
			System.out.println("Login user in progress...");
			System.out.println("Username: " + usrname + " Password: " + password);
			
		} catch (Exception e)
		{
			System.out.println("Error in invoking object method " + e.toString() + e.getMessage());
			e.printStackTrace();
		}
		
	}
}