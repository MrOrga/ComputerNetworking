import javafx.css.Selector;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.nio.channels.ClosedChannelException;

public class Userhome
{
	@FXML
	private TextField username;
	
	public void addFriendClick(ActionEvent event) throws ClosedChannelException
	{
		String friend = username.getText();
		JsonObj obj = new JsonObj("addfriend", friend);
		ControllerLogin.sendRequest(obj);
		//SelectorT
		
	}
	
	public void showFriendListClick(ActionEvent event)
	{
	
	}
	
	public void logoutClick(ActionEvent event)
	{
	
	}
	
	
}
