import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class ControllerRegister
{
	@FXML
	private TextField username;
	@FXML
	private PasswordField passwd;
	
	public void registerClick(ActionEvent event)
	{
		//Register of user in the database using RMI
		int port = 60500;
		Database serverObject;
		Remote RemoteObject;
		try
		{
			Registry r = LocateRegistry.getRegistry(port);
			RemoteObject = r.lookup(Database.SERVICE_NAME);
			serverObject = (Database) RemoteObject;
			String usrname = username.getText();
			String password = passwd.getText();
			int result = serverObject.register(usrname, password);
			System.out.println("Registration user in progess...");
			System.out.println("Username: " + usrname + " Password: " + password);
		} catch (Exception e)
		{
			System.out.println("Error in invoking object method " + e.toString() + e.getMessage());
			e.printStackTrace();
		}
	}
}
