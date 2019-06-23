package view;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

public class PopUp {

	public static String deleteWhere() {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Are You Sure?");
		alert.setHeaderText("Where would you like to delete from?");
		alert.setContentText("Choose a directory: ");

		ButtonType buttonTypeOne = new ButtonType("Local");
		ButtonType buttonTypeTwo = new ButtonType("Shared");
		ButtonType buttonTypeThree = new ButtonType("Both");
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree, buttonTypeCancel);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == buttonTypeOne)
			return "local";
		
		else if (result.get() == buttonTypeTwo)
			return "shared";
		
		else if (result.get() == buttonTypeThree)
			return "both";
		
		else
			return "cancel";
		
	}
}