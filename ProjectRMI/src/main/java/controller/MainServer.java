package controller;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

import connection.Server;
import javafx.application.Application;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainServer extends Application {

	private static final String defaultFolder = "C:\\Users\\steph\\Desktop\\Year 3 - Semester 1\\Distributive System Programming\\Eclipse Projects\\FinalProjectRMI";
	
	@Override
	public void start(Stage primaryStage) {
		
		DirectoryChooser chooserShared = new DirectoryChooser();
		chooserShared.setTitle("Select Shared Folder");
		chooserShared.setInitialDirectory(new File(defaultFolder));
		File shared = chooserShared.showDialog(new Stage());
		
		if(shared == null)
			System.exit(0);
		
		try {
			Server server = new Server(shared);
			server.start();
			
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		
		launch(args);
	}
}
