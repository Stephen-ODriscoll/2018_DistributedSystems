package model;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

public class Shared implements Monitor {

	private File directory;
	private ArrayList<File> files;
	private FileChannel writeChannel;
	private FileChannel readChannel;
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
		
		try {
			
			File lock = new File(this.directory + "\\lock.txt");
			
			if(!lock.exists())
				lock.createNewFile();
			
		} catch (IOException e1) {
				System.out.println("Error, Couldn't Create Lock");
		}
		
		Path path = Paths.get(this.directory + "\\lock.txt");
		
		try {
			writeChannel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
			readChannel = FileChannel.open(path, StandardOpenOption.READ);
			
		} catch (IOException e) {
			
			System.out.println("Error Creating Lock");
		}

		files = refresh();
		System.out.println("Shared Monitor Active");
	}

	
	//Returns copy of files for creating local folder
	public ArrayList<File> copyShared() {
		
		ArrayList<File> copy = new ArrayList<>();

		for(int i = 0; i < files.size(); i++)
			copy.add(files.get(i));
		
		return copy;
	}

	
	//Returns file requested for download
	public ArrayList<File> download(ArrayList<String> names) {
		
		ArrayList<File> result = new ArrayList<>();
		
		//We transverse our files adding each one to result when found
		for(int a = 0; a < names.size(); a++) {

			//We transverse our files list looking for file with the given name
			for (int b = 0; b < files.size(); b++)
				if (files.get(b).getName().equals(names.get(a)))
					result.add(files.get(b));		//Add this file if found
		}

		return result;
	}
	
	
	//Upload file to directory
	public void upload(ArrayList<File> files) {
		
		try {
			FileLock lock = writeChannel.lock();		//lock up
		
			int i = 0;		//Count files uploaded and display to user
		
			//We transverse our files uploading each one
			for(; i < files.size(); i++) {
		
				try {

					FileUtils.copyFileToDirectory(files.get(i), directory);	//Try to upload
					
				} catch (IOException e1) {
			
					System.out.println("Error, Upload Failed. Retrying");
					upload(files);		//Shouldn't fail but if it does we should always be able to upload eventually
				}
			}
			
			lock.close();		//unlock
			System.out.println(i + " Files saved to Shared Directory");
			
		} catch (IOException e1) {
			
			System.out.println("Error Locking for Delete");
		}
	}
	
	
	//Delete file from directory
	public void delete(ArrayList<String> names) {
		
		try {
			FileLock lock = writeChannel.lock();
		
		int deleted = 0;		//Count files removed and display to user
		
		//We transverse our files list looking for files whose name we were given
		for(int a = 0; a < names.size(); a++) {
			
			boolean search = true;
			
			for (int b = 0; b < files.size() && search; b++) {
				
				if (files.get(b).getName().equals(names.get(a)))
					try {
						
						FileUtils.forceDelete(files.get(b));		//Delete this file
						
						deleted++;
						search = false;
					
					} catch (IOException e) {
						
						delete(b, 1);
						
					}
			}
		}
		
		lock.close();
		System.out.println(deleted + " Files Removed From Shared");
		
		} catch (IOException e1) {
			
			System.out.println("Error Locking for Delete");
		}
	}
	
	
	//Try to delete again
	public boolean delete(int fileNumber, int attempt) {
		
		System.out.println("Failed to Delete File #" + fileNumber + ". Attempt: " + attempt);
		
		//Yes, I've seen my program fail to delete a file 75 times then get it on the 76th time. No idea why
		//100 attempts should be enough, after that the file probably doesn't exist (deleted by something else)
		if(attempt == 100)
			return false;
		
		try {
			
			FileUtils.forceDelete(files.get(fileNumber));		//Delete this file
			return true;
		
		} catch (IOException e) {
			
			return delete(fileNumber, attempt + 1);				//If we failed to delete we try again
		}
		
	}
	
	
	// Checks for changes to shared folder and returns true if there is a change
	public synchronized boolean checkForChange(ArrayList<File> localFiles, ArrayList<String> differences) throws IOException {

		boolean isChange = false;					//No change yet, that we're aware of
		ArrayList<File> currentFiles = refresh();	//Get current files

		//If the old files list isn't the same size as the new one
		if (files.size() != currentFiles.size())
			isChange = true;		//There is a change
		
		//Otherwise there might still be a change, not sure
		else {

			//Check every file in both lists against every file in the other
			for (int a = 0; a < currentFiles.size(); a++)
				for(int b = 0; b < localFiles.size(); b++) {
					
					//Try to compare but we expect an error if we compare a directory to a file
					try {
						// If these two are now equal
						if (FileUtils.contentEquals(currentFiles.get(a), localFiles.get(b))) {

							// But they were different before
							if (differences.contains(currentFiles.get(a).getName()))
								isChange = true; // There is a change
						}

						// Otherwise if they have the same name and are different
						else if (currentFiles.get(a).getName().equals(localFiles.get(b).getName())) {

							// But they weren't different before
							if (!differences.contains(currentFiles.get(a).getName()))
								isChange = true; // There is a change
						}
					} catch (IOException e) {
						
						//If the directory is in current files move through it
						if(currentFiles.get(a).isDirectory()) {
							
							a++;
							b = 0;
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
	private ArrayList<File> refresh() {
		
		try {
			
			FileLock lock = readChannel.lock(0, Long.MAX_VALUE, true);	//Lock up
			
			/**		while(true) {} /*		*/				//Blue is for testing
			
			File[] filesArray = directory.listFiles();		//Get all files in our shared folder
			ArrayList<File> files = new ArrayList<>();		//Create empty array list

			//Iterate through files array converting to array list
			for (int i = 0; i < filesArray.length; i++)
				if(!filesArray[i].getName().equals("lock.txt"))
					files.add(filesArray[i]);

			lock.close();		//Open lock

			return files;		//Give back files

			/**		*/
		} catch (IOException e) {
			
			System.out.println("Error, Read Lock Failed");
			return new ArrayList<>();		//Return empty array list
		}
		
	
	}
}
