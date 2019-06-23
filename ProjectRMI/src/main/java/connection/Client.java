package connection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import controller.GUIController;

public class Client {
	
	private final int PORT = 9090;
	private final String HOSTNAME = "localhost";
	private static Registry registry;
	
	ClientInterface receiver;
    ServerInterface server;
	
	public Client(GUIController control) throws RemoteException {
		
		try {
        	registry = LocateRegistry.getRegistry(HOSTNAME, PORT);

            Remote remoteObject = registry.lookup("rmiserver");

			if (remoteObject instanceof ServerInterface) {
				server = (ServerInterface) remoteObject ;
				receiver = new ClientReceiver(control);
			} else {
				System.out.println("Server not a Chat Server.");
				System.exit(0);
			}
        }
        catch(Exception e){
            System.out.println("RMI Lookup Exception");
            System.exit(0);
        };
        
        server.add(receiver);
	}
	
	
	public void toDownload(ArrayList<String> names) {

		try {
			
			server.download(receiver, names);
			
		} catch (NullPointerException n) {
			
			System.out.println("Server Not Created");
		} catch (RemoteException e) {
			e.printStackTrace();
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
				
			server.upload(names, filesBytes);
			
		} catch (IOException e) {
			
			System.out.println("Upload Failed");
		}
	}
	
	
	public void toDelete(ArrayList<String> names) {
			
		try {
			server.delete(names);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	
	public void leave() {
		
		try {
			server.remove(receiver);
			
		} catch (RemoteException e) { e.printStackTrace(); }
	}
	
}
