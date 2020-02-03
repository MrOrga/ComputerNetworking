import javafx.css.Selector;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Vector;

public class Userhome
{
	@FXML
	private TextField username;
	@FXML
	private ListView<String> friendlist;
	@FXML
	public AnchorPane challenge;
	
	
	public void challengeShow(String friend) throws Exception
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("challenge.fxml"));
		AnchorPane newPane = loader.load();
		
		challengeController c = loader.getController();
		c.setUser(friend);
		challenge.getChildren().setAll(newPane);
		
		
	}
	
	public void addFriendClick() throws ClosedChannelException
	{
		String friend = username.getText();
		JsonObj obj = new JsonObj("addfriend", friend);
		ControllerLogin.sendRequest(obj);
		username.clear();
		ControllerLogin.setUserHome(this);
		
	}
	
	public void challengeClick(ActionEvent event) throws ClosedChannelException
	{
		String friend = friendlist.getSelectionModel().getSelectedItem();
		JsonObj obj = new JsonObj("challenge", friend);
		ControllerLogin.sendRequest(obj);
	}
	
	
	public void showFriend(Vector<String> friend) throws ClosedChannelException
	{
		//no friend
		if (friend.size() == 0)
			return;
		
		friendlist.getItems().clear();
		friendlist.getItems().addAll(friend);
		System.out.println(friendlist.getItems().size());
		for (int i = 0; i < friendlist.getItems().size(); i++)
			if (friendlist.getItems().get(i) == null)
			{
				System.out.println("empty cell");
				friendlist.getItems().remove(friendlist.getItems().get(i));
			}
		{
		
		}
		friendlist.setVisible(true);
		System.out.println("friendlist show");
	}
	
	public void showFriendListClick() throws ClosedChannelException
	{
		JsonObj obj = new JsonObj("friendlist");
		ControllerLogin.sendRequest(obj);
		ControllerLogin.setUserHome(this);
	}
	
	public void logoutClick(ActionEvent event) throws ClosedChannelException
	{
		JsonObj obj = new JsonObj("logout");
		ControllerLogin.sendRequest(obj);
		ControllerLogin.setEvent(event);
	}
	
	
}
