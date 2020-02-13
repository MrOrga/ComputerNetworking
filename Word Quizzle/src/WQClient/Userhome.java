package WQClient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import Utils.*;

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
	@FXML
	public AnchorPane showPoints;
	@FXML
	private Text score;
	@FXML
	private Text user;
	
	public void setScore(String s)
	{
		this.score.setText(s);
	}
	
	public void setUser()
	{
		this.user.setText(SelectorT.username);
	}
	
	public void notificationReset()
	{
		challenge.getChildren().clear();
	}
	
	public void challengeShow(String friend) throws Exception
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/WQClient/FXML/notification.fxml"));
		AnchorPane newPane = loader.load();
		
		challengeController c = loader.getController();
		c.setUser(friend);
		c.setChallenge(challenge);
		challenge.getChildren().setAll(newPane);
		
		
	}
	
	public void addFriendClick(ActionEvent event) throws ClosedChannelException
	{
		String friend = username.getText();
		JsonObj obj = new JsonObj("addfriend", friend);
		ControllerLogin.sendRequest(obj);
		username.clear();
		ControllerLogin.setUserHome(this);
		
	}
	
	public void challengeClick(ActionEvent event) throws ClosedChannelException
	{
		ControllerLogin.setEvent(event);
		String friend = friendlist.getSelectionModel().getSelectedItem().split(" ")[0];
		JsonObj obj = new JsonObj("challenge", friend);
		ControllerLogin.sendRequest(obj);
	}
	
	public void showLeaderboard(Vector<String> users, Vector<Integer> scores)
	{
		if (users.size() == 0)
			return;
		friendlist.getItems().clear();
		for (int i = users.size() - 1; i >= 0; i--)
		{
			friendlist.getItems().add(users.get(i) + " score: " + scores.get(i));
		}
		friendlist.setVisible(true);
		System.out.println("Leaderboard show");
	}
	
	public void showFriend(Vector<String> friend)
	{
		//no friend
		if (friend.size() == 0)
			return;
		
		friendlist.getItems().clear();
		friendlist.getItems().addAll(friend);
		friendlist.getStylesheets().add("WQClient/FXML/css.css");
		
		friendlist.setVisible(true);
		System.out.println("friendlist show");
	}
	
	public void showFriendListClick(ActionEvent event) throws ClosedChannelException
	{
		JsonObj obj = new JsonObj("friendlist");
		ControllerLogin.setEvent(event);
		ControllerLogin.sendRequest(obj);
		ControllerLogin.setUserHome(this);
	}
	
	public void showScoreClick(ActionEvent event) throws ClosedChannelException
	{
		JsonObj obj = new JsonObj("score");
		ControllerLogin.setEvent(event);
		ControllerLogin.sendRequest(obj);
		ControllerLogin.setUserHome(this);
	}
	
	public void logoutClick(ActionEvent event) throws ClosedChannelException
	{
		JsonObj obj = new JsonObj("logout");
		ControllerLogin.setEvent(event);
		ControllerLogin.sendRequest(obj);
		
	}
	
	
	public void showLeaderboardClick(ActionEvent event) throws ClosedChannelException
	{
		JsonObj obj = new JsonObj("leaderboard");
		ControllerLogin.setEvent(event);
		ControllerLogin.sendRequest(obj);
		ControllerLogin.setUserHome(this);
	}
	
	
	public void showPoints(int points)
	{
		score.setText(String.valueOf(points));
		showPoints.setVisible(true);
		
	}
}
