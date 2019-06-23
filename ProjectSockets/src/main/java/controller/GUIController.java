package controller;

import java.io.File;
import java.util.ArrayList;

import connection.Client;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mediaPlayer.MyMediaPlayer;
import model.Local;
import view.GUI;

public class GUIController {

	private Client client = new Client(this);
	private Local local;
	private GUI gui;
	private ArrayList<String> selected = new ArrayList<String>();
	private String selectedColor = "";

	public GUIController(File localFolder, Stage stage, GUI gui) {

		this.local = new Local(localFolder);
		this.gui = gui;
		local.setGUI(gui);
		
		client.start();
		
		stage.setOnCloseRequest(e -> {
			client.stopWhenReady();
			local.removeClaim();
		});
	}
	
	
	//Called by server, creates a client thread to add files to local
	public void addFiles(ArrayList<String> names, ArrayList<byte[]> add) {
		
		(new Thread() {
			
			@Override
			public void run() {
				
				local.addFiles(names, add);
			}
		}).start();		//Start this thread
	}
	
	
	//Called by server, check the differences between local and shared and display to GUI
	public void checkChanges(ArrayList<byte[]> newFiles, ArrayList<String> newFileNames) {
				
		local.checkChanges(newFiles,newFileNames);
	}
	
	
	//Called by client connection, gets a copy of local files and known differences and sends back to server
	public ArrayList<File> getFiles() {
				
		return local.getFiles();
	}
	
	
	public ArrayList<String> getDifferences() {
		
		return local.getDifferences();
	}
	

	public boolean select(TextField fileName) {

		if (selectedColor.equals("")) {

			if (fileName.getStyle().contains("red"))
				selectedColor = "red";

			else if (fileName.getStyle().contains("orange"))
				selectedColor = "orange";

			else if (fileName.getStyle().contains("yellow"))
				selectedColor = "yellow";

			else
				selectedColor = "white";

			changeButtons(true);
		}

		if (fileName.getStyle().contains(selectedColor)) {

			selected.add(fileName.getText());
			return true;
		}

		return false;
	}

	public boolean deselect(String fileName) {

		if (selected.contains(fileName)) {
			selected.remove(fileName);

			if (selected.size() == 0) {
				
				changeButtons(false);
				selected.remove(fileName);
				selectedColor = "";
				
			}
			
			return true;
		}
		return false;
	}
	
	
	public void selectAll(TextField fileName) {
		
		selected.add(fileName.getText());
	}
	

	public ArrayList<String> getNames() {

		ArrayList<File> files = local.getFiles();
		ArrayList<String> fileNames = new ArrayList<>();

		for (int i = 0; i < files.size(); i++)
			fileNames.add(files.get(i).getName());

		return fileNames;
	}
	

	public void download() {
		
		client.toDownload(selected);
		gui.deselectAll();
	}
	

	public void upload() {

		ArrayList<File> files = new ArrayList<>();
		
		for (int i = 0; i < selected.size(); i++)
			files.add(local.getFile(selected.get(i)));
		
		client.toUpload(files);
		gui.deselectAll();
	}
	

	public void playFiles() {

		ArrayList<File> files = local.getFiles();

		for (int a = 0; a < files.size(); a++)
			for (int b = 0; b < selected.size(); b++)
				if (files.get(a).getName().equals(selected.get(b)))
					new MyMediaPlayer(files.get(a));
	}
	
	
	//Delete red/orange
		public void delete() {	
				
			if(selectedColor.equals("red")) {
				
				ArrayList<String> names = new ArrayList<>();
				
				while(selected.size() > 0) {
					
					names.add(selected.get(0));
					deselect(selected.get(0));
				}
				
				client.toDelete(names);
				
			}
				
			else if(selectedColor.equals("orange")) {
					
				local.delete(selected);
				gui.deleteEntries(selected);
				
				selected.clear();
				changeButtons(false);
				selectedColor = "";
			}
			
		}
		
		
		// Delete white/yellow
		public void delete(String result) {

			if (result.equals("local")) {

				local.delete(selected);

				while (selected.size() > 0) {

					gui.changeEntry(selected.get(0), "red");
					deselect(selected.get(0));
				}
			}

			else if (result.equals("shared")) {
				
				client.toDelete(selected);
				gui.deselectAll();
			}

			else if (result.equals("both")) {

				local.delete(selected);
				gui.deleteEntries(selected);
				ArrayList<String> names = new ArrayList<>();

				while (selected.size() > 0) {

					names.add(selected.get(0));
					deselect(selected.get(0));
				}
				
				client.toDelete(names);
			}

		}
	
	
	private void changeButtons(boolean selecting) {

		if (selectedColor.equals("red"))
			gui.changeDownload();

		else if (selectedColor.equals("orange")) {

			gui.changePlay();
			gui.changeUpload();
		}

		else if (selectedColor.equals("yellow")) {

			gui.changeDownload();
			gui.changePlay();
			gui.changeUpload();
		}

		else if(selectedColor.equals("white"))
			gui.changePlay();
		
		else
			return;
		
		gui.changeDelete();
		gui.changeOthers(selecting);
		
	}
	
	public String getSelectedColor() {
		
		return selectedColor;
	}
}
