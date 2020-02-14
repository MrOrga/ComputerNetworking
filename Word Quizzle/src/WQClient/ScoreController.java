package WQClient;

import Utils.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScoreController
{
	
	@FXML
	private Text score;
	@FXML
	private Text text;
	@FXML
	private Button gotohome;
	
	public void disableGotohome()
	{
		gotohome.setDisable(true);
		gotohome.setVisible(false);
	}
	
	public Text getText()
	{
		return text;
	}
	
	public void setText(String s)
	{
		this.text.setText(s);
	}
	
	
	public Text getScore()
	{
		return score;
	}
	
	public void setScore(String score)
	{
		this.score.setText(score);
	}
	
	@FXML
	void backToHome(ActionEvent event) throws IOException
	{
		//load the userhome.fxml
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/WQClient/FXML/userhome.fxml"));
		Parent home = loader.load();
		Userhome userhome = loader.getController();
		UdpListener.setUserhome(userhome);
		UdpListener.setCanAccept(new AtomicBoolean(true));
		userhome.setUser();
		//get the Stage from the event
		Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		
		//Set the Scene
		primaryStage.setScene(new Scene(home, 800, 600));
		primaryStage.show();
		
		System.out.println("Go to User Home");
		
	}
	
	@FXML
	void logoutClick(ActionEvent event) throws ClosedChannelException
	{
		JsonObj obj = new JsonObj("logout");
		ControllerLogin.setEvent(event);
		ControllerLogin.sendRequest(obj);
		
		
	}
	
}
