package controller;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import view.GUI;

public class Main extends Application {
	
	private final String defaultFolder = "C:\\Users\\steph\\Desktop\\Year 3 - Semester 1\\Distributive System Programming\\Eclipse Projects\\Project";
	
	@Override
	public void start(Stage primaryStage) {
		
		Group root = new Group();
		Scene scene = new Scene(root, 600, 400);
		BorderPane borderPane = new BorderPane();
		
		DirectoryChooser chooserLocal = new DirectoryChooser();
		chooserLocal.setTitle("Select Local Folders");
		chooserLocal.setInitialDirectory(new File(defaultFolder));
		File local = chooserLocal.showDialog(new Stage());
		
		if(local == null)
			System.exit(0);
		
		DirectoryChooser chooserShared = new DirectoryChooser();
		chooserShared.setTitle("Select Shared Folder");
		chooserShared.setInitialDirectory(new File(defaultFolder));
		File shared = chooserShared.showDialog(new Stage());
		
		
		if(shared == null || local.equals(shared))
			System.exit(0);
		
		borderPane.prefHeightProperty().bind(scene.heightProperty());
		borderPane.prefWidthProperty().bind(scene.widthProperty());
		
		borderPane.setCenter(new GUI(local, shared, primaryStage));
		root.getChildren().add(borderPane);
		primaryStage.setScene(scene);
		primaryStage.setTitle("File Sharing Program");
		primaryStage.show();
	}
	
	
	public static void main(String[] args) {
		
		launch(args);
	}

}
