package WQClient;

import WQServer.Database;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class ControllerRegister
{
	@FXML
	private TextField username;
	@FXML
	private PasswordField passwd;
	@FXML
	private AnchorPane pane;
	
	private Controller controllerHome;
	
	public void setControllerHome(Controller controllerHome)
	{
		this.controllerHome = controllerHome;
	}
	
	public void setPane(AnchorPane pane)
	{
		this.pane = pane;
	}
	
	public void goToLogin() throws IOException
	{
		
		//pane = WQClient.Controller.getPane();
		FXMLLoader load = new FXMLLoader(getClass().getResource("/WQClient/FXML/login.fxml"));
		AnchorPane newPane = load.load();
		ControllerLogin c = load.getController();
		c.setPane(pane);
		c.setControllerHome(controllerHome);
		controllerHome.clearError();
		pane.getChildren().setAll(newPane);
		
	}
	
	public void backToHome(ActionEvent event) throws Exception
	{
		//load the Register.fxml
		Parent home = FXMLLoader.load(getClass().getResource("/WQClient/FXML/home.fxml"));
		
		//get the Stage from the event
		Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		
		//Set the Stage
		primaryStage.setScene(new Scene(home, 800, 600));
		primaryStage.show();
		
		System.out.println("Back to home");
	}
	
	public void registerClick(ActionEvent event)
	{
		//Register of user in the database using RMI
		int port = 60550;
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
			
			System.out.println("Registration user in progress...");
			System.out.println("Username: " + usrname + " Password: " + password);
			System.out.println(result);
			if (result == 0)
			{
				System.out.println("Registration success");
				backToHome(event);
			}
			if (result == -400)
			{
				controllerHome.showError("Invalid user or password");
			}
			if (result == -401)
			{
				controllerHome.showError("User already exist");
			}
		} catch (Exception e)
		{
			System.out.println("Error in invoking object method " + e.toString() + e.getMessage());
			e.printStackTrace();
		}
	}
}
