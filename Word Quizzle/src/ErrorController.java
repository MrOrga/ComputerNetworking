import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ErrorController
{
	
	@FXML
	private Text error;
	
	public Text getError()
	{
		return error;
	}
	
	public void setError(String s)
	{
		this.error.setText(s);
	}
	
	@FXML
	void CloseClick(ActionEvent event)
	{
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		stage.close();
	}
	
}
