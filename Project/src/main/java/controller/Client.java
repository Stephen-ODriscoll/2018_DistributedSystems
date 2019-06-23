package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mediaPlayer.MyMediaPlayer;
import model.Local;
import view.GUI;

public class Client {

	private Server worker;
	private Local local;
	private GUI gui;
	private ArrayList<String> selected = new ArrayList<String>();
	private String selectedColor = "";

	public Client(File localFolder, File sharedFolder, Stage stage, GUI gui) {

		this.local = new Local(localFolder);
		this.worker = new Server(sharedFolder, this);
		this.gui = gui;
		local.setGUI(gui);
		
		worker.start();
		
		stage.setOnCloseRequest(e -> {
			worker.stopWhenReady();
			local.removeClaim();
		});
	}
	
	
	//Called by server, creates a client thread to add files to local
	public void addFiles(ArrayList<File> add) {
		
		(new Thread() {
			
			@Override
			public void run() {
				
				local.addFiles(add);
			}
		}).start();		//Start this thread
	}
	
	
	//Called by server, creates a client thread to check the differences between local and shared and display to GUI
	public void checkChanges(ArrayList<File> newFiles) {
		
		(new Thread() {
			
			@Override
			public void run() {
				
				try {
					
					local.checkChanges(newFiles);
				} catch (IOException e) {
			
					System.out.println("Error Checking Shared Against Local");
				}
			}
		}).start();
	}
	
	
	//Called by server, gets a copy of local files and known differences and sends back to server
	public void getFiles() {
		
		(new Thread() {
			
			@Override
			public void run() {
				
				ArrayList<File> files = local.getFiles();
				ArrayList<String> differences = local.getDifferences();
				worker.checkforChange(files, differences);
			}
		}).start();
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

	public void stopWorker() {

		worker.stopWhenReady();
	}

	public void download() {

		for (int i = 0; i < selected.size(); i++)
			worker.addDownload(selected.get(i));
		
		gui.deselectAll();
	}
	

	public void upload() {

		for (int i = 0; i < selected.size(); i++)
			worker.addUpload(local.getFile(selected.get(i)));
		
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
				
			if(selectedColor.equals("red"))
				while(selected.size() > 0) {
					
					worker.toDelete(selected.get(0));
					deselect(selected.get(0));
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
				
				for(int i = 0; i < selected.size(); i++)
					worker.toDelete(selected.get(i));
				
				gui.deselectAll();
			}

			else if (result.equals("both")) {

				local.delete(selected);
				gui.deleteEntries(selected);

				while (selected.size() > 0) {

					worker.toDelete(selected.get(0));
					deselect(selected.get(0));
				}
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
