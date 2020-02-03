import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application
{
	Button button;
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		Parent root = FXMLLoader.load(getClass().getResource("home.fxml"));
		primaryStage.setTitle("Word Quizzle");
		primaryStage.getIcons().add(new Image(("/image/wico.png")));
		/*
		button = new Button();
		button.setText("Register");
		
		//creazione layout
		StackPane layout = new StackPane();
		layout.getChildren().add(button);
		
		//creazione scena
		Scene scene = new Scene(layout, 300, 275);
		//primaryStage.setScene(scene);*/
		
		primaryStage.setScene(new Scene(root, 800, 600));
		primaryStage.show();
	}
	
	
	public static void main(String[] args)
	{
		launch(args);
	}
}
