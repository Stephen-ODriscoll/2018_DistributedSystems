package connection;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

import controller.GUIController;

public class Client extends Thread {
	
	private final int PORT = 9090;
	private Socket socket;
	private Streams streams;
	
	private GUIController control;
	private boolean stop = false;
	
	public Client(GUIController control) {
		
		this.control = control;
		
        try {
        	
        	InetAddress host = InetAddress.getLocalHost();
        	socket = new Socket(host.getHostName(), PORT);
        	streams = new Streams(socket);
			
		} catch (IOException e) {

			System.out.println("No Server to Connect To");
			System.exit(0);
		}
	}
	
	
	@Override
	public void run() {
		
		while(!stop) {
			
			try {
				
				String message = streams.readString();
				System.out.println("Message: " + message);
				
				switch (message) {

					case ("check"):

						ArrayList<String> newfileNames = streams.readStrings();
						ArrayList<byte[]> newFiles = streams.readData();
						control.checkChanges(newFiles, newfileNames);

					break;
					case ("add"):
					
						ArrayList<String> names = streams.readStrings();
						ArrayList<byte[]> toAdd = streams.readData();
						control.addFiles(names, toAdd);
						
					break;
				}
				
				sleep(1500);
				
			} catch (InterruptedException e) {

	        	System.out.println("Error Sleeping Thread");
	        }
		}
	}
	
	
	public void toDownload(ArrayList<String> names) {

		try {
			
			streams.writeString("download");
			streams.writeStrings(names);
			
		} catch (NullPointerException n) {
			
			System.out.println("Server Not Created");
		}
		
	}
	
	
	public void toUpload(ArrayList<File> files) {

		try {
			
			ArrayList<String> names = new ArrayList<>();
			ArrayList<byte[]> filesBytes = new ArrayList<>();
			
			for(int i = 0; i < files.size(); i++) {
				
				names.add(files.get(i).getName());
				filesBytes.add(Files.readAllBytes(files.get(i).toPath()));
			}
				
			streams.writeString("upload");
			streams.writeStrings(names);
			streams.writeData(filesBytes);
			
		} catch (IOException e) { 
			
			System.out.println("Upload Failed");
		}
	}
	
	
	public void toDelete(ArrayList<String> names) {
			
			streams.writeString("delete");
			streams.writeStrings(names);
	}
	
	
	public void stopWhenReady() {
		
		System.out.println("Stopping Client");
		
		try {
			stop = true;
			
			streams.close();
			socket.close();
			
		} catch (IOException e) {
			
			System.out.println("Stopping Server Thread Failed");
		}
	}
	
}
