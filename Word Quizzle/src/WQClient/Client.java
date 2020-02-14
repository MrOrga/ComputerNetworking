package WQClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Client extends Application
{
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		Parent root = FXMLLoader.load(getClass().getResource("/WQClient/FXML/home.fxml"));
		primaryStage.setTitle("Word Quizzle");
		primaryStage.getIcons().add(new Image(("/image/wico.png")));
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(root, 800, 600));
		primaryStage.show();
	}
	
	
	public static void main(String[] args)
	{
		launch(args);
	}
}
