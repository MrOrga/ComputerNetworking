import javafx.css.Selector;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.nio.channels.ClosedChannelException;
import java.util.Vector;

public class Userhome
{
	@FXML
	private TextField username;
	@FXML
	private ListView<String> friendlist;
	
	
	public void addFriendClick(ActionEvent event) throws ClosedChannelException
	{
		String friend = username.getText();
		JsonObj obj = new JsonObj("addfriend", friend);
		ControllerLogin.sendRequest(obj);
		username.clear();
		//SelectorT
		
	}
	
	
	public void showFriend(Vector<String> friend) throws ClosedChannelException
	{
		//no friend
		if (friend.size() == 0)
			return;
		
		friendlist.getItems().clear();
		friendlist.getItems().addAll(friend);
		System.out.println("friendlist show");
	}
	
	public void showFriendListClick(ActionEvent event) throws ClosedChannelException
	{
		JsonObj obj = new JsonObj("friendlist");
		ControllerLogin.sendRequest(obj);
		ControllerLogin.setUserHome(this);
	}
	
	public void logoutClick(ActionEvent event)
	{
	
	}
	
	
}
