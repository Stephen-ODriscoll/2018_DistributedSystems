package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import model.Shared;

public class Server extends Thread {
	
	private Shared shared;
	private ArrayList<String> toDownload = new ArrayList<String>();
	private ArrayList<File> toUpload = new ArrayList<File>();
	private ArrayList<String> toDelete = new ArrayList<String>();
	private ArrayList<File> localCopy;
	private ArrayList<String> differences;
	private Boolean localReceived = false;
	
	private Client client;
	private boolean stop = false;

	
	//Worker needs access to local
	public Server(File directory, Client client) {
		
		this.client = client;
		
		shared = Shared.getInstance(directory);
	}
	
	
	@Override
	public void run() {
		
		initializeLocal();
			
		while(!stop) {
			
			//Take a nap, no need to use all the CPU
			try {
				sleep(1500);
			} catch (InterruptedException e) {
				
				System.out.println("Error Sleeping Worker");
			}
			
			//If we have received a copy of the local files
			if(localReceived)
				try {
					//If there are new differences between the shared and local folders
					if (shared.checkForChange(localCopy, differences)) {

						ArrayList<File> newFiles = shared.copyShared();	//Send a copy of shared files to local folder
						client.checkChanges(newFiles);					//Check changes between local and shared
						
						localCopy = null;		//Reset local copy
						differences = null;		//Reset differences
						localReceived = false;
					}
				} catch (IOException e1) {

					System.out.println("Error Checking Shared for Changes");
				}
			
			//Tell client to send on files when ready, to check for changes
			client.getFiles();
			
			//If I have downloads to do
			if(!toDownload.isEmpty()) {
				
				client.addFiles( shared.download(toDownload) );		//Give local the files it requested
				toDownload.clear();									//Job done
			}
			
			//If I have uploads to do
			if(!toUpload.isEmpty()) {
				
				shared.upload(toUpload);			//Give shared the files to upload
				toUpload.clear();					//Job done
			}
			
			//If I have deleting to do
			if(!toDelete.isEmpty()) {
				
				shared.delete(toDelete);			//Tell shared to delete these files
				toDelete.clear();					//Job done
			}
			
		}
		
	}
	
	
	//Initialize changes to shared in local
	private void initializeLocal() {
		
		//Give GUI time to initialize then initialize files
			try {
				sleep(200);
				ArrayList<File> newFiles = shared.copyShared();
				client.checkChanges(newFiles);
				System.out.println("Initial Files Set");
				
			} catch (Exception e) {
						
				System.out.println("Error Setting Initial Files");
				initializeLocal();		//If we fail call recursively. We have to get local initialized
			}
	}
	
	
	//Called by client, this thread sets its local and the recorded differences between the two then returns
	public void checkforChange(ArrayList<File> files, ArrayList<String> differences) {
		
		localCopy = files;
		this.differences = differences;
		
		localReceived = true;
	}
	
	
	//Add job to workers list - get local a file from shared
	public void addDownload(String name) {
		
		toDownload.add(name);
	}
	
	//Add job to workers list - add file to shared
	public void addUpload(File file) {
		
		toUpload.add(file);
	}
	
	//Add job to workers list - delete file from shared
	public void toDelete(String name) {
		
		toDelete.add(name);
	}
	
	//Stops this thread just before it sleeps next
	public void stopWhenReady() {
		
		stop = true;
	}
}
