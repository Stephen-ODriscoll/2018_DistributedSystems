package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

import view.GUI;

public class Local implements Monitor {

	private File directory;
	private String myDirectory;
	private File claim;
	private ArrayList<File> files = new ArrayList<>();
	private static ArrayList<String> differences = new ArrayList<>();
	private GUI gui;
	
	
	public Local(File directory) {
		
		this.directory = directory;
		
		if(!this.directory.exists())
			this.directory.mkdir();
		
		createLocal();
	}
	
	
	//Find a local folder that hasn't been claimed by another thread
	public synchronized void createLocal() {
		
		File myFolder = new File("");		//Place holder for each folder I check
		
		//While I don't have a folder keep counting
		for(int i = 0; myDirectory == null; i++) {
			
			myFolder = new File(directory + "\\Local" + i);					//Set my folder
			claim = new File(directory + "\\Local" + i + "\\claim.txt");	//Set my claim file
			
			//If this folder doesn't already have a claim file
			if(!claim.exists()) {
				
				myFolder.mkdir();		//Create the folder for my files (might exist already)
				myDirectory = directory + "\\Local" + i;		//Set this as my directory
				System.out.println("Local Repository Created: Local" + i);
			}
		}
		
		File[] filesArray = myFolder.listFiles();		//Take in these files as an array

		//Add files in this array to my ArrayList
		for (int i = 0; i < filesArray.length; i++)
			files.add(filesArray[i]);
		
		//Claim this folder as my own
		try {
			claim.createNewFile();
		} catch (IOException e) {
			
			System.out.println("Error, Could Not Claim");
		}
	}
	
	
	//Shared has told me there are changes, I must check what these are
	public synchronized void checkChanges(ArrayList<File> newFiles) throws IOException {
		
		ArrayList<File> localCopy = new ArrayList<File>();		//I make a copy of my files
		
		//I add my files to this copy
		for(int i = 0; i < files.size(); i++)
			localCopy.add(files.get(i));
		
		System.out.println("Shared: " + newFiles.size() + " Local: " + localCopy.size());
		
		//I check all files in both lists against each other
		for(int a = 0; a < newFiles.size(); a++)
			for(int b = 0; b < localCopy.size(); b++) {
				
				//If two files are the exact same content
				if(FileUtils.contentEquals(newFiles.get(a), localCopy.get(b))) {
					
					gui.changeEntry(localCopy.get(b).getName(), "white");	//Set this file as being the same now
					differences.remove(localCopy.get(b).getName());			//If this file was different it isn't now
					
					newFiles.remove(a);		//Remove the file from shared copy
					localCopy.remove(b);	//Remove the file from local copy
					
					b = -1;					//Reset b to 0 to start again on next two files(it will be 0 next loop)
				}
				
				//Otherwise if two files aren't the same but have the same name
				else if(newFiles.get(a).getName().equals( localCopy.get(b).getName() )) {
					
					//And weren't different before
					if(!differences.contains(localCopy.get(b).getName())) {
						
						differences.add(localCopy.get(b).getName());			//Mark these files as being different
						gui.changeEntry(localCopy.get(b).getName(), "yellow");	//Set this file as being different
					}
					
					newFiles.remove(a);		//Remove the file from shared copy
					localCopy.remove(b);	//Remove the file from local copy
					
					b = -1;					//Reset b to 0 to start again on next two files(it will be 0 next loop)
				}
				
				//If removing a file from a means there are no more
				if(a >= newFiles.size())
					break;					//Stop looping
			}
			
			gui.resetShared();			//Remove all files that were only in shared
		
			//Set all files that are only in shared
			for(int i = 0; i < newFiles.size(); i++) {
				
				gui.changeEntry(newFiles.get(i).getName(), "red");		//Red means file is in shared but not local
				differences.remove(newFiles.get(i).getName());			//If this file was different it isn't now
			}
		
			//Set all files that are only in local
			for(int i = 0; i < localCopy.size(); i++) {
				
				gui.changeEntry(localCopy.get(i).getName(), "orange");	//Orange means file is in local but not shared
				differences.remove(localCopy.get(i).getName());			//If this file was different it isn't now
			}
		
	}
	
	
	//Remove my claim file as app is closing
	public synchronized void removeClaim() {
		
		claim.delete();
	}
	
	
	//Add a file to local folder
	public synchronized void addFiles(ArrayList<File> add) {
		
		int a = 0;		//Count files added and display at the end
		
		//Transverse list of files to add until all have been added
		for(; a < add.size(); a++) {
			
			//If the file exists already remove it
			for(int i = 0; i < files.size(); i++)
				if(add.get(a).getName().equals(files.get(i).getName()))
					files.remove(i);
		
			String name = add.get(a).getName();		//Get the name of this file
		
			try {
			
				FileUtils.copyFileToDirectory(add.get(a), new File(myDirectory));		//Try to copy this file to local folder
				File myFolder = new File(myDirectory);									//Hold new files in local folder
				File[] newFiles = myFolder.listFiles();									//Read in all files in local folder
			
				//Transverse files until we find the file we added
				for(int b = 0; b < newFiles.length; b++)
					if(newFiles[b].getName().equals(name))
						files.add(newFiles[b]);		//We need to get this specific file because this will be a reference to the
													//One in our local instead of the one in the shared folder
			
				gui.changeEntry(files.get(files.size()-1).getName(), "white");		//Change this file to white as it's in both folders
				differences.remove(files.get(files.size()-1).getName());			//If this file was different it isn't now
			
			} catch (IOException e1) {
			
				System.out.println("Error, Download Failed");
			}
		}
		
		System.out.println(a + " Files Saved to Local Folder");
	}
	
	
	//Return a copy of one of my files
	public synchronized File getFile(String name) {
		
		for(int i = 0; i < files.size(); i++)
			if(files.get(i).getName().equals(name))
				return files.get(i);
		
		System.out.println("Error, Failed to Find File: " + name);
		
		for(int i = 0; i < files.size(); i++)
			System.out.println(files.get(i));
		
		return null;
	}
	
	
	//Remove a certain file from my folder
	public synchronized void delete(ArrayList<String> names) {
		
		int deleted = 0;
		
		//Transverse my files until I find one that matches the given names
		for(int a = 0; a < names.size(); a++)
			for(int b = 0; b < files.size(); b++)
				if(files.get(b).getName().equals(names.get(a)))
					try {
						FileUtils.forceDelete(files.get(b));			//Try to delete this file
						differences.remove(files.get(b).getName());		//Remove this file from my current differences if it's there
						files.remove(b);								//Remove this file from my list
						deleted++;
					
					} catch (IOException e) {
					
						System.out.println("Error, Local Delete Failed on File #" + b + ". Retrying:");
						boolean result = delete(names, 1);
						
						if(result)
							deleted++;
					}
		
		if(deleted > 0)
			System.out.println(deleted + " Files Removed from Local");
	}
	
	//Try to remove again
	public synchronized boolean delete(ArrayList<String> names, int attempt) {

		//If we have tried 100 times give up
		if (attempt == 100)
			return false;

		//Transverse my files until I find one that matches the given names
		for (int a = 0; a < names.size(); a++)
			for (int b = 0; b < files.size(); b++)
				if (files.get(b).getName().equals(names.get(a)))
					try {
						FileUtils.forceDelete(files.get(b)); //Try to delete this file
						differences.remove(files.get(b).getName()); //Remove this file from my current differences if
																	//it's there
						files.remove(b); //Remove this file from my list
						
						return true;

					} catch (IOException e) {

						System.out.println("Error, Local Delete Failed on File #" + b + ". Attempy: " + attempt);
						return delete(names, attempt + 1);
					}
		
		return false;
	}
	
	
	//Set reference to the GUI so I can update changes to user
	public synchronized void setGUI(GUI gui) {
		
		this.gui = gui;
	}
	
	
	//Get a copy of all my files
	public synchronized ArrayList<File> getFiles() {
		
		ArrayList<File> copy = new ArrayList<>();

		for(int i = 0; i < files.size(); i++)
			copy.add(files.get(i));
			
		return copy;
	}
	
	
	//Get my list of differences between local and shared folders
	public synchronized ArrayList<String> getDifferences() {
		
		return differences;
	}
	
}
