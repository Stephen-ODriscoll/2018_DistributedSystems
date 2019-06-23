package connection;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;

import model.Shared;

public class Server extends UnicastRemoteObject implements ServerInterface {

	private static final long serialVersionUID = 1L;
	
	private static final int PORT = 9090;
	private static Registry registry;
	private static HashSet<ClientInterface> clients = new HashSet<>();
	private static ArrayList<byte[]> filesBytes = new ArrayList<>();
	private static File directory;
	private static Shared shared;
	
	private static FileChannel writeChannel;
	private static FileChannel readChannel;

	
	public Server(File directory) throws RemoteException {
		super();

		Server.directory = directory;
		Server.shared = Shared.getInstance(directory);
		Server.filesBytes = shared.getFiles();
		createLock();
	}
	
	
	private static void startRegistry() throws RemoteException {
		registry =  LocateRegistry.createRegistry(PORT);
	}

	private static void registerObject(String name, Remote remoteObj) throws RemoteException, AlreadyBoundException {
  		registry.bind(name, remoteObj);
	}

	
	public void start() throws AlreadyBoundException {
		
		try {
			Server server = new Server(directory);
			startRegistry();
	     	registerObject("rmiserver", server);
	     	
	     	check();

			System.out.println("Server is Running Correctly");
		}
		
		catch (RemoteException e) {
			System.out.println("Communication error " + e.toString());
		}
	}
	
	
	private static void check() {
		
		(new Thread() {

			@Override
			public void run() {

				while (true) {

					try {
						sleep(3000);
						FileLock lock = readChannel.lock(0, Long.MAX_VALUE, true);	//Lock up
						
						// If there are new differences between the shared and local folders
						if (clients.size() > 0 && shared.checkForChange(filesBytes)) {

							filesBytes = shared.getFiles();

							for (ClientInterface client : clients)
								client.check(shared.getFileNames(), filesBytes);
							
						}
						lock.close();
						
					} catch (OverlappingFileLockException e) {
						
					} catch (IOException e) {

						System.out.println("Error Checking Shared for Changes" + e);
					} catch (InterruptedException e) {

						System.out.println("Failed to Sleep Checking Thread");
					} 
				}
			}
		}).start();
	}
	
	
	private void createLock() {

		try {

			File lock = new File(Server.directory + "\\lock.txt");

			if (!lock.exists())
				lock.createNewFile();

		} catch (IOException e1) {
			System.out.println("Error, Couldn't Create Lock");
		}

		Path path = Paths.get(Server.directory + "\\lock.txt");

		try {
			writeChannel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
			readChannel = FileChannel.open(path, StandardOpenOption.READ);

		} catch (IOException e) {

			System.out.println("Error Creating Lock");
		}
	}

	
	@Override
	public synchronized void add(ClientInterface c) throws RemoteException {
		
		clients.add(c);
		c.check(shared.getFileNames(), filesBytes);
		
		System.out.println("Client connection successful");
	}

	
	@Override
	public synchronized void download(ClientInterface c, ArrayList<String> names) throws RemoteException {
		
		while(true)
			try {
				FileLock lock = readChannel.lock(0, Long.MAX_VALUE, true);	//Lock up

				ArrayList<byte[]> result = shared.download(names);
				c.add(shared.getDownloadNames(), result);

				lock.close();
				break;
			} catch (Exception e) {
				try {
					wait(1000);
				} catch (InterruptedException e1) { }
			}
	}

	
	@Override
	public synchronized void upload(ArrayList<String> names, ArrayList<byte[]> files) throws RemoteException {

		while(true)
			try {
				FileLock lock1 = writeChannel.lock(); // lock up

				shared.upload(names, files);

				lock1.close();
				break;
			} catch (Exception e) {
				try {
					wait(1000);
				} catch (InterruptedException e1) { }
			}
	}

	
	@Override
	public synchronized void delete(ArrayList<String> names) throws RemoteException {
		
		while (true)
			try {
				FileLock lock2 = writeChannel.lock(); // lock up

				shared.delete(names);

				lock2.close();
				break;
			} catch (Exception e) {
				try {
					wait(1000);
				} catch (InterruptedException e1) { }
			}
	}

	
	@Override
	public synchronized void remove(ClientInterface c) throws RemoteException {
		
		clients.remove(c);
	}
}
