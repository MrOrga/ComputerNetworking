import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.channels.ClosedChannelException;

public class challengeController
{
	@FXML
	private TextField wordEn;
	@FXML
	private Text friendUser;
	
	@FXML
	public AnchorPane challenge;
	
	@FXML
	private TextField word;
	
	
	public AnchorPane getChallenge()
	{
		return challenge;
	}
	
	public TextField getWord()
	{
		return word;
	}
	
	public void setWord(String word)
	{
		this.word.setText(word);
	}
	
	public void setChallenge(AnchorPane challenge)
	{
		this.challenge = challenge;
	}
	
	public void setUser(String s)
	{
		friendUser.setText(s);
	}
	
	public void acceptChallengeClick(ActionEvent event) throws ClosedChannelException
	{
		ControllerLogin.setEvent(event);
		ControllerLogin.sendRequest(new JsonObj("accept", friendUser.getText()));
	}
	
	public void sendClick(ActionEvent event) throws ClosedChannelException
	{
		
		ControllerLogin.setEvent(event);
		JsonObj obj = new JsonObj("word translation");
		obj.setWord(wordEn.getText());
		ControllerLogin.sendRequest(obj);
		wordEn.clear();
	}
	
	public void logoutClick(ActionEvent event) throws ClosedChannelException
	{
		JsonObj obj = new JsonObj("logout");
		ControllerLogin.setEvent(event);
		ControllerLogin.sendRequest(obj);
		
	}
	
	public void declineChallangeClick(ActionEvent event)
	{
		UdpListener.resetAccept();
		challenge.getChildren().clear();
	}
	
}
