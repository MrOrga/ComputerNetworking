import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class challengeController
{
	@FXML
	private Text friendUser;
	
	public void setUser(String s)
	{
		friendUser.setText(s);
	}
	
	public void acceptChallangeClick(ActionEvent event)
	{
		//challenge.getChildren().clear();
	}
	
	public void declineChallangeClick(ActionEvent event)
	{
		//challenge.getChildren().clear();
	}
	
}
