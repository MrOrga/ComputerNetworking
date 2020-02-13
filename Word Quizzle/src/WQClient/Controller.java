package WQClient;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import javafx.event.ActionEvent;


public class Controller
{
	@FXML
	public AnchorPane pane;
	
	@FXML
	public AnchorPane error;
	
	@FXML
	public Text errorText;
	
	public void showError(String s)
	{
		error.setVisible(true);
		errorText.setText(s);
	}
	
	public void clearError()
	{
		error.setVisible(false);
	}
	
	public void showRegisterClick(ActionEvent event) throws Exception
	{
		
		FXMLLoader load = new FXMLLoader(getClass().getResource("/WQClient/FXML/Register2.fxml"));
		AnchorPane newPane = load.load();
		ControllerRegister c = load.getController();
		c.setPane(pane);
		pane.getChildren().clear();
		pane.getChildren().setAll(newPane);
		c.setControllerHome(this);
		
		System.out.println("Register click");
	}
	
	public void showLoginClick(ActionEvent event) throws Exception
	{
		
		FXMLLoader load = new FXMLLoader(getClass().getResource("/WQClient/FXML/login.fxml"));
		AnchorPane newPane = load.load();
		ControllerLogin c = load.getController();
		c.setPane(pane);
		pane.getChildren().clear();
		pane.getChildren().setAll(newPane);
		c.setControllerHome(this);
		
		System.out.println("Login click");
	}
	
}
