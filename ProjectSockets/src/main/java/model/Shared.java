package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class Shared implements Monitor {

	private File directory;
	private ArrayList<File> files;
	private ArrayList<String> downloadNames;
	private static Shared sharedFolder;
	
	public static Shared getInstance(File directory) {
		
		if(sharedFolder == null)
			sharedFolder = new Shared(directory);
		
		return sharedFolder;
	}
	
	
	private Shared(File directory) {
		
		this.directory = directory;
		
		if(!this.directory.exists())
			this.directory.mkdir();

		files = refresh();
		System.out.println("Shared Monitor Active");
	}

	
	//Returns copy of files for creating local folder
	public synchronized ArrayList<byte[]> getFiles() {
		
		ArrayList<byte[]> copy = new ArrayList<>();
		
		for (int i = 0; i < files.size(); i++)
			try {
				
				copy.add(Files.readAllBytes(files.get(i).toPath()));

			} catch (IOException e) {

				System.out.println("Error Reading File from Shared Folder");
			}
		
		return copy;
	}

	
	//Returns file requested for download
	public synchronized ArrayList<byte[]> download(ArrayList<String> names) {
		
		ArrayList<byte[]> resultBytes = new ArrayList<>();
		downloadNames = new ArrayList<>();
		
		//We transverse our files adding each one to result when found
		for(int a = 0; a < names.size(); a++) {

			//We transverse our files list looking for file with the given name
			for (int b = 0; b < files.size(); b++)
				if (files.get(b).getName().equals(names.get(a))) {
					
					//Add this file if found
					try {
						resultBytes.add(Files.readAllBytes(files.get(b).toPath()));
						downloadNames.add(files.get(b).getName());
					} catch (IOException e) {

						System.out.println("Server Error, Reading Bytes for Download Failed");
					}
				}
		}

		return resultBytes;
	}
	
	
	public synchronized ArrayList<String> getDownloadNames() {
		
		return downloadNames;
	}
	
	
	//Upload file to directory
	public synchronized void upload(ArrayList<String> names, ArrayList<byte[]> filesBytes) {
		
		int i = 0;		//Count files uploaded and display to user

		//We transverse our files uploading each one
		for(; i < filesBytes.size(); i++) {

			try {

				File file = new File(directory + "\\" + names.get(i));
				Files.write(file.toPath(), filesBytes.get(i));	//Try to upload
				
			} catch (IOException e1) {
		
				System.out.println("Error, Upload Failed. Retrying");
				upload(names, filesBytes);		//Shouldn't fail but if it does we should always be able to upload eventually
			}
		}
		
		System.out.println(i + " Files saved to Shared Directory");
	}
	
	
	//Delete file from directory
	public synchronized void delete(ArrayList<String> names) {
		
		int deleted = 0;		//Count files removed and display to user
		
		//We transverse our files list looking for files whose name we were given
		for(int a = 0; a < names.size(); a++) {
			
			boolean search = true;
			
			for (int b = 0; b < files.size() && search; b++) {
				
				if (files.get(b).getName().equals(names.get(a)))
					try {
						
						Files.delete(files.get(b).toPath());		//Delete this file
						
						deleted++;
						search = false;
					
					} catch (IOException e) {
						
						delete(b, 1);
						
					}
			}
		}
		
		System.out.println(deleted + " Files Removed From Shared");
	}
	
	
	//Try to delete again
	private boolean delete(int fileNumber, int attempt) {
		
		System.out.println("Failed to Delete File #" + fileNumber + ". Attempt: " + attempt);
		
		//Yes, I've seen my program fail to delete a file 75 times then get it on the 76th time. No idea why
		//100 attempts should be enough, after that the file probably doesn't exist (deleted by something else)
		if(attempt == 100)
			return false;
		
		try {
			
			Files.delete(files.get(fileNumber).toPath());		//Delete this file
			return true;
		
		} catch (IOException e) {
			
			return delete(fileNumber, attempt + 1);				//If we failed to delete we try again
		}
		
	}
	
	
	// Checks for changes to shared folder and returns true if there is a change
	public synchronized boolean checkForChange(ArrayList<byte[]> filesBytes) throws IOException {

		boolean isChange = false;					//No change yet, that we're aware of
		ArrayList<File> currentFiles = refresh();	//Get current files

		//If the old files list isn't the same size as the new one
		if (filesBytes.size() != currentFiles.size())
			isChange = true;		//There is a change
		
		//Otherwise there might still be a change, not sure
		else {

			//Check every file in both lists against every file in the other
			for (int a = 0; a < currentFiles.size() && !isChange; a++) {
					
					//Try to compare but we expect an error if we compare a directory to a file
					try {
						// If these two are not equal
						if (!Arrays.equals(Files.readAllBytes(currentFiles.get(a).toPath()), filesBytes.get(a)))
							isChange = true; // There is a change

					} catch (IOException e) {
						
						//If a directory is in current files move through it
						if(currentFiles.get(a).isDirectory()) {
							
							a++;
						}

						System.out.println("Can't compare a directory");
					}
				}
		}

		//If there is a change
		if (isChange) {
			
			files = currentFiles;		//Set the old files equal to current files
			System.out.println("There is a change to Shared Folder");
		}

		return isChange;				//Return that there is a change
	}

	
	// Returns files currently in shared folder
	private synchronized ArrayList<File> refresh() {
		
		File[] filesArray = directory.listFiles();		//Get all files in our shared folder
		ArrayList<File> files = new ArrayList<>();		//Create empty array list

		//Iterate through files array converting to array list
		for (int i = 0; i < filesArray.length; i++)
			if(!filesArray[i].getName().equals("lock.txt"))
				files.add(filesArray[i]);

		return files;		//Give back files
	}
	
	
	public synchronized ArrayList<String> getFileNames() {
		
		ArrayList<String> names = new ArrayList<>();
		
		for(int i = 0; i < files.size(); i++)
			names.add(files.get(i).getName());
		
		return names;
	}
}
