package view;

import java.io.File;
import java.util.ArrayList;

import controller.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GUI extends VBox {

	private ScrollPane scrollPane = new ScrollPane();
	private ArrayList<TextField> fileNames = new ArrayList<>();
	private Button download = new Button("Download");
	private Button downloadAll = new Button("Download All");
	private Button play = new Button("Play");
	private Button upload = new Button("Upload");
	private Button uploadAll = new Button("Upload All");
	private Button delete = new Button("Delete");
	private HBox top = new HBox();
	private HBox bottom = new HBox();
	private VBox list = new VBox();
	
	private int red = 0;
	private int orange = 0;

	private Client control;

	//Create display to user
	public GUI(File localFolder, File sharedFolder, Stage stage) {

		control = new Client(localFolder, sharedFolder, stage, this);
		list = new VBox();

		setFiles();

		download.setOnAction(e -> {
			control.download();
		});
		upload.setOnAction(e -> {
			control.upload();
		});
		play.setOnAction(e -> control.playFiles());

		delete.setOnAction(e -> {

			if (control.getSelectedColor().equals("white") || control.getSelectedColor().equals("yellow"))
				control.delete(PopUp.deleteWhere());

			else
				control.delete();
		});
		
		downloadAll.setOnAction(e -> {
			downloadAll();
		});
		
		uploadAll.setOnAction(e -> {
			uploadAll();
		});
		
		String[] colors = {new String("red"), new String("orange"), new String("white"), new String("yellow")};
		String[] meanings = {new String("Shared Only"), new String("Local Only"), new String("Both Folders"), new String("Both But Different")};
		
		//Set explanations about what colors mean at top of screen
		for(int i = 0; i < 4; i++) {
			
			TextField option = new TextField(meanings[i]);
			
			option.setEditable(false);
			option.setPrefWidth(120);
			option.setStyle("-fx-background-color: " + colors[i] + "; -fx-border-color: grey; -fx-border-width: 2px;");
			top.getChildren().add(option);
		}
		
		scrollPane.setPrefSize(557, 258);
		scrollPane.setMaxSize(557, 258);
		download.setPrefWidth(110);
		upload.setPrefWidth(110);
		delete.setPrefWidth(110);
		play.setPrefWidth(110);
		
		downloadAll.setPrefWidth(110);
		uploadAll.setPrefWidth(110);

		top.setSpacing(20);
		top.setAlignment(Pos.CENTER);
		bottom.setSpacing(40);
		bottom.setAlignment(Pos.CENTER);
		setSpacing(20);
		setAlignment(Pos.TOP_CENTER);
		scrollPane.setContent(list);
		bottom.getChildren().addAll(downloadAll, uploadAll);
		getChildren().addAll(top, scrollPane, bottom);
		setPadding(new Insets(15, 15, 15, 15));
	}

	private void select(MouseEvent e) {

		TextField fileName = (TextField) e.getSource();

		if (deselect(fileName)) { }

		else {

			if (!control.select(fileName))
				return;

			if (fileName.getStyle().contains("red"))
				fileName.setStyle("-fx-background-color: red; -fx-border-color: blue ; -fx-border-width: 2px ;");

			else if (fileName.getStyle().contains("orange"))
				fileName.setStyle("-fx-background-color: orange; -fx-border-color: blue ; -fx-border-width: 2px ;");

			else if (fileName.getStyle().contains("yellow"))
				fileName.setStyle("-fx-background-color: yellow; -fx-border-color: blue ; -fx-border-width: 2px ;");

			else
				fileName.setStyle("-fx-background-color: white; -fx-border-color: blue ; -fx-border-width: 2px ;");
		}
	}
	
	
	private boolean deselect(TextField fileName) {
		
		if (control.deselect(fileName.getText())) {

			if (fileName.getStyle().contains("red"))
				fileName.setStyle("-fx-background-color: red; -fx-border-color: grey ; -fx-border-width: 2px ;");

			else if (fileName.getStyle().contains("orange"))
				fileName.setStyle("-fx-background-color: orange; -fx-border-color: grey ; -fx-border-width: 2px ;");

			else if (fileName.getStyle().contains("yellow"))
				fileName.setStyle("-fx-background-color: yellow; -fx-border-color: grey ; -fx-border-width: 2px ;");

			else
				fileName.setStyle("-fx-background-color: white; -fx-border-color: grey ; -fx-border-width: 2px ;");
			
			return true;
		}
		
		return false;
	}
	
	
	//Deselect all files, we either downloaded, uploaded or deleted selected files
	public void deselectAll() {
		
		for (int i = 0; i < fileNames.size(); i++)
			deselect(fileNames.get(i));
	}
	

	//Set files in local directory initially
	private void setFiles() {

		ArrayList<String> files = control.getNames();
		list.getChildren().clear();

		for (int i = 0; i < files.size(); i++) {

			fileNames.add(new TextField());

			fileNames.get(i).setOnMouseClicked(e -> select(e));

			fileNames.get(i).setStyle("-fx-background-color: white ; -fx-border-color: grey ; -fx-border-width: 2px ;");
			fileNames.get(i).setFont(Font.font("Monospace", 15));
			fileNames.get(i).setEditable(false);
			fileNames.get(i).setMinSize(555, 32);
			fileNames.get(i).setPrefSize(files.get(i).length() * 10, 32);

			fileNames.get(i).setText(files.get(i));
			list.getChildren().add(fileNames.get(i));
		}
	}
	
	
	//Select all files that can be downloaded and download them
	private void downloadAll() {
		
		for(int i = 0; i < list.getChildren().size(); i++)
			if(((TextField) list.getChildren().get(i)).getStyle().contains("red"))
				control.selectAll(((TextField) list.getChildren().get(i)));
		
		control.download();
	}
	
	
	//Select all files that can be uploaded and upload them
	private void uploadAll() {
		
		for(int i = 0; i < list.getChildren().size(); i++)
			if(((TextField) list.getChildren().get(i)).getStyle().contains("orange"))
				control.selectAll(((TextField) list.getChildren().get(i)));
		
		control.upload();
	}
	
	
	public void resetShared() {
		
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				
				//Remove all red files as we have no record of what was in shared folder
				//Every time it changes we must clear what we have and get new files
				//Otherwise files could be left displayed when removed from shared folder
				for (int i = 0; i < fileNames.size(); i++)
					if (fileNames.get(i).getStyle().contains("red")) {
						
						deselect(fileNames.get(i));
						list.getChildren().remove(fileNames.get(i));
						fileNames.remove(i);
						i--;
					}
				
				//Reset red variable as all reds are deleted
				red = 0;
				if(bottom.getChildren().contains(downloadAll))
					bottom.getChildren().remove(downloadAll);
			}
		});
	}

	
	public void changeEntry(String file, String color) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				TextField fileName = new TextField(file);
				boolean exists = false;
				int i = 0;
				
				//Check if the file exists and if it does make sure it's not selected
				for (; i < fileNames.size(); i++)
					if (fileNames.get(i).getText().equals(file)) {

						exists = true;
						deselect(fileNames.get(i));
						break;
					}

				//If it doesn't exist create it
				if (!exists) {

					i = fileNames.size();
					fileNames.add(fileName);
					list.getChildren().add(fileName);
				}
				
				checkButtons(i, color, exists);		//Make sure download all and upload all are correct
				
				//Adjusts width of files to make room for scrollbar so scaling looks nice
				//If we have more than 8 textfields we make the boxes smaller
				if(fileNames.size() > 8 + red + orange)
					fileNames.get(i).setMinSize(540, 32);
				
				else
					fileNames.get(i).setMinSize(555, 32);

				fileNames.get(i).setOnMouseClicked(e -> select(e));

				fileNames.get(i).setStyle(
						"-fx-background-color: " + color + "; -fx-border-color: grey ; -fx-border-width: 2px ;");
				fileNames.get(i).setFont(Font.font("Monospace", 15));
				fileNames.get(i).setEditable(false);
				fileNames.get(i).setPrefSize(file.length() * 10, 32);
			}
		});
	}
	
	
	private void checkButtons(int i, String color, boolean exist) {
		
		//If the field didn't exist or wasn't red before but will be now
		if((!exist || (!fileNames.get(i).getStyle().contains("red")) && color.equals("red")))
			red++;
		
		//Otherwise if the field did exist and was red but won't be after
		else if(exist && fileNames.get(i).getStyle().contains("red") && !color.equals("red"))
			red--;
		
		//Otherwise if the field didn't exist or wasn't orange before but will be now
		else if(!exist || (!fileNames.get(i).getStyle().contains("orange") && color.equals("orange")))
			orange++;
				
		//Otherwise if the field did exits and was orange but won't be after
		else if(exist && fileNames.get(i).getStyle().contains("orange") && !color.equals("orange"))
			orange--;
		
		//If we have at least one red file and download all is hidden add it
		if(red > 0 && !bottom.getChildren().contains(downloadAll))
			bottom.getChildren().add(downloadAll);
		
		//Else if we have no red file and download all is showing hide it
		else if(red == 0 && bottom.getChildren().contains(downloadAll))
			bottom.getChildren().remove(downloadAll);
			
		//If we have at least one orange file and upload all is hidden add it
		if(orange > 0 && !bottom.getChildren().contains(uploadAll))
			bottom.getChildren().add(uploadAll);
				
		//Else if we have no orange file and upload all is showing hide it
		else if(orange == 0 && bottom.getChildren().contains(uploadAll))
			bottom.getChildren().remove(uploadAll);
	}
	

	public void deleteEntries(ArrayList<String> files) {

		for(int a = 0; a < files.size(); a++)
			for (int b = 0; b < fileNames.size(); b++)
				if (fileNames.get(b).getText().equals(files.get(a))) {
					
					if(fileNames.get(b).getStyle().contains("orange"))
						orange--;
					
					//Don't need to check red as red will be reset anyway in reset shared method

					list.getChildren().remove(fileNames.get(b));
					fileNames.remove(b);
					b = fileNames.size();
				}
	}

	public void changeDownload() {

		if (bottom.getChildren().contains(download))
			bottom.getChildren().remove(download);

		else
			bottom.getChildren().add(download);
	}

	public void changePlay() {

		if (bottom.getChildren().contains(play))
			bottom.getChildren().remove(play);

		else
			bottom.getChildren().add(play);
	}

	public void changeUpload() {

		if (bottom.getChildren().contains(upload))
			bottom.getChildren().remove(upload);

		else
			bottom.getChildren().add(upload);
	}

	public void changeDelete() {

		if (bottom.getChildren().contains(delete))
			bottom.getChildren().remove(delete);

		else
			bottom.getChildren().add(delete);
	}
	
	//Changing upload all and download all
	public void changeOthers(boolean selecting) {
		
		//If there is nothing to uplaod or download do nothing either way
		if(red == 0 && orange == 0)
			return;
		
		//Otherwise if we have just selected something we remove these buttons if they exist
		else if(selecting) {
		
			if (bottom.getChildren().contains(downloadAll))
				bottom.getChildren().remove(downloadAll);
			
			if (bottom.getChildren().contains(uploadAll))
				bottom.getChildren().remove(uploadAll);
		}

		//Otherwise we are deselecting, if we can upload or download anything show these buttons
		else {

			//This sorts upload and download if they're both showing, upload all right of download all
			if (red > 0 && !bottom.getChildren().contains(downloadAll)) {
				
				//If upload all is there we remove it and add it back on the right
				if (bottom.getChildren().contains(uploadAll)) {
					
					bottom.getChildren().remove(uploadAll);
					bottom.getChildren().addAll(downloadAll, uploadAll);
				}
				
				else
					bottom.getChildren().add(downloadAll);
			}
			
			if (orange > 0 && !bottom.getChildren().contains(uploadAll))
				bottom.getChildren().add(uploadAll);
		}
		
	}

}
