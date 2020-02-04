import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.nio.channels.ClosedChannelException;

public class challengeController
{
	@FXML
	private Text friendUser;
	
	@FXML
	public AnchorPane challenge;
	
	public AnchorPane getChallenge()
	{
		return challenge;
	}
	
	public void setChallenge(AnchorPane challenge)
	{
		this.challenge = challenge;
	}
	
	public void setUser(String s)
	{
		friendUser.setText(s);
	}
	
	public void acceptChallangeClick(ActionEvent event) throws ClosedChannelException
	{
		
		ControllerLogin.sendRequest(new JsonObj("accept", friendUser.getText()));
	}
	
	public void declineChallangeClick(ActionEvent event)
	{
		challenge.getChildren().clear();
	}
	
}
