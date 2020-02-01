import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
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
	@FXML
	public AnchorPane pane;
	//public static Thread sel;
	private static SelectorT selector;
	
	public static void setEvent(ActionEvent event)
	{
		selector.setEvent(event);
	}
	
	public void setPane(AnchorPane pane)
	{
		this.pane = pane;
	}
	
	public void backToRegister() throws IOException
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Register2.fxml"));
		AnchorPane newPane = loader.load();
		ControllerRegister c = loader.getController();
		c.setPane(pane);
		
		pane.getChildren().setAll(newPane);
	}
	
	public static void sendRequest(JsonObj obj) throws ClosedChannelException
	{
		selector.sendRequest(obj);
	}
	
	public static void setUserHome(Userhome userhome) throws ClosedChannelException
	{
		selector.setUserhome(userhome);
	}
	
	public void goToUserHome(ActionEvent event) throws Exception
	{
		//load the Register.fxml
		Parent home = FXMLLoader.load(getClass().getResource("userhome.fxml"));
		
		//get the Stage from the event
		Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		
		//Set the Scene
		primaryStage.setScene(new Scene(home, 800, 600));
		primaryStage.show();
		
		System.out.println("Go to User Home");
	}
	
	public void goToHome(ActionEvent event) throws Exception
	{
		//load the Register.fxml
		Parent home = FXMLLoader.load(getClass().getResource("sample.fxml"));
		
		//get the Stage from the event
		Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		
		//Set the Scene
		primaryStage.setScene(new Scene(home, 800, 600));
		primaryStage.show();
		
		System.out.println("Go to Home");
	}
	
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
			selector = new SelectorT(obj, this, event);
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