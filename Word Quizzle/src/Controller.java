import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

public class Controller
{
	public void showRegisterClick(ActionEvent event) throws Exception
	{
		//load the Register.fxml
		Parent register = FXMLLoader.load(getClass().getResource("Register.fxml"));
		
		//get the Stage from the event
		Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		
		//Set the Stage
		primaryStage.setScene(new Scene(register, 800, 600));
		primaryStage.show();
		
		System.out.println("Register click");
	}
	
	
}
