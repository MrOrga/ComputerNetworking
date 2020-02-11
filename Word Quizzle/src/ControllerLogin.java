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
import java.util.concurrent.atomic.AtomicBoolean;

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
	private challengeController c;
	private Controller controllerHome;
	private static Stage stage;
	
	public static Stage getStage()
	{
		return stage;
	}
	
	public static void setStage(Stage stage)
	{
		ControllerLogin.stage = stage;
	}
	
	public void setControllerHome(Controller controllerHome)
	{
		this.controllerHome = controllerHome;
	}
	
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
		c.setControllerHome(controllerHome);
		controllerHome.clearError();
		pane.getChildren().setAll(newPane);
	}
	
	public static void sendRequest(JsonObj obj) throws ClosedChannelException
	{
		selector.sendRequest(obj);
	}
	
	public static void setUserHome(Userhome userhome)
	{
		selector.setUserhome(userhome);
	}
	
	public void goToChallenge(ActionEvent event, String word) throws Exception
	{
		//load the Register.fxml
		FXMLLoader loader = new FXMLLoader(getClass().getResource("challenge.fxml"));
		Parent home = loader.load();
		c = loader.getController();
		c.setWord(word);
		//get the Stage from the event
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		
		UdpListener.setCanAccept(new AtomicBoolean(false));
		//Set the Scene
		stage.setScene(new Scene(home, 800, 600));
		stage.show();
		
		System.out.println("Go to Challenge");
	}
	
	public void setWord(String word)
	{
		c.setWord(word);
	}
	
	public void goToUserHome(ActionEvent event) throws Exception
	{
		//load the userhome.fxml
		FXMLLoader loader = new FXMLLoader(getClass().getResource("userhome.fxml"));
		Parent home = loader.load();
		Userhome userhome = loader.getController();
		userhome.setUser();
		UdpListener.setUserhome(userhome);
		//get the Stage from the event
		Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		
		//Set the Scene
		primaryStage.setScene(new Scene(home, 800, 600));
		primaryStage.show();
		
		System.out.println("Go to User Home");
	}
	
	public void goToScore(ActionEvent event, int score, String eventChallenge) throws Exception
	{
		//load the score.fxml
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("score.fxml"));
		Parent home = loader.load();
		ScoreController c = loader.getController();
		if (eventChallenge != null)
		{
			c.setText(eventChallenge);
			
			c.setScore("");
		} else
		{
			
			c.setScore(String.valueOf(score));
		}
		//get the Stage from the event
		Stage primaryStage = stage;
		
		//Set the Scene
		primaryStage.setScene(new Scene(home, 800, 600));
		primaryStage.show();
		
		System.out.println("Go to Score");
	}
	
	public void goToHome(ActionEvent event) throws Exception
	{
		//load the home.fxml
		Parent home = FXMLLoader.load(getClass().getResource("home.fxml"));
		
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
			selector = new SelectorT(obj, this, event, controllerHome);
			Thread sel = new Thread(selector);
			sel.setDaemon(true);
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