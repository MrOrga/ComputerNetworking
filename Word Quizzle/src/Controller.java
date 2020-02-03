import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.io.IOException;


public class Controller
{
	@FXML
	public AnchorPane pane;
	
	
	public void showRegisterClick(ActionEvent event) throws Exception
	{
		
		FXMLLoader load = new FXMLLoader(getClass().getResource("Register2.fxml"));
		AnchorPane newPane = load.load();
		ControllerRegister c = load.getController();
		c.setPane(pane);
		pane.getChildren().clear();
		pane.getChildren().setAll(newPane);
		
		
		System.out.println("Register click");
	}
	
	public void showLoginClick(ActionEvent event) throws Exception
	{
		
		FXMLLoader load = new FXMLLoader(getClass().getResource("login.fxml"));
		AnchorPane newPane = load.load();
		ControllerLogin c = load.getController();
		c.setPane(pane);
		pane.getChildren().clear();
		pane.getChildren().setAll(newPane);
		
		
		System.out.println("Login click");
	}
	
}
