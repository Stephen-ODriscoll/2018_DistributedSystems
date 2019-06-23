package connection;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;

import model.Shared;

public class Server extends Thread {

	private static final int PORT = 9090;
	private static HashSet<Streams> dataStreams = new HashSet<>();
	private static ArrayList<byte[]> filesBytes = new ArrayList<>();
	private static File directory;
	private static Shared shared;
	
	private static FileChannel writeChannel;
	private static FileChannel readChannel;

	public Server(File directory) {

		Server.directory = directory;
		Server.shared = Shared.getInstance(directory);
		Server.filesBytes = shared.getFiles();
		createLock();
	}

	@Override
	public void run() {

		(new Thread() {

			@Override
			public void run() {

				check();
			}
		}).start(); // Start this thread

		try {

			System.out.println("Server is Running");
			ServerSocket listener = new ServerSocket(PORT);

			try {
				while (true) {
					new Handler(listener.accept()).start();
				}
			} finally {
				listener.close();
			}

		} catch (IOException e) {

			System.out.println("Server has Failed: " + e);
		}
	}

	private static void check() {

		while (true) {

			try {
				sleep(3000);
				FileLock lock = readChannel.lock(0, Long.MAX_VALUE, true);	//Lock up
				
				// If there are new differences between the shared and local folders
				if (dataStreams.size() > 0 && shared.checkForChange(filesBytes)) {

					filesBytes = shared.getFiles();

					for (Streams stream : dataStreams) {

						stream.writeString("check");
						stream.writeStrings(shared.getFileNames());
						stream.writeData(filesBytes);
					}
				}
				lock.close();
				
			} catch (OverlappingFileLockException e) {
				
			} catch (IOException e) {

				System.out.println("Error Checking Shared for Changes");
			} catch (InterruptedException e) {

				System.out.println("Failed to Sleep Checking Thread");
			} 
		}
	}
	
	
	private static void createLock() {

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
	

	private static class Handler extends Thread {

		private Socket socket;
		private boolean stop = false;
		private Streams streams;

		/**
		 * Constructs a handler thread, squirreling away the socket. All the interesting
		 * work is done in the run method.
		 */
		public Handler(Socket socket) {

			this.socket = socket;
		}

		@Override
		public void run() {

			try {
				loop();
			} catch (IOException e) {

				System.out.println("Server Thread Failed. Connection to One Client Lost");
			}
		}

		public void loop() throws IOException {
			
			streams = new Streams(socket);
			
			initialize();
			dataStreams.add(streams);

			System.out.println("Server Thread Started Successfully");

			while (!stop) {

				try {

					String message = streams.readString();
					System.out.println("Message: " + message);

					switch (message) {

					case "download":

						while(true)
							try {
								FileLock lock = readChannel.lock(0, Long.MAX_VALUE, true);	//Lock up

								ArrayList<String> toDownload = streams.readStrings();
								ArrayList<byte[]> result = shared.download(toDownload);
								streams.writeString("add");
								streams.writeStrings(shared.getDownloadNames());
								streams.writeData(result);

								lock.close();
								break;
							} catch (OverlappingFileLockException e) { sleep(1000); }

						break;
					case "upload":

						while(true)
							try {
								FileLock lock1 = writeChannel.lock(); // lock up

								ArrayList<String> names = streams.readStrings();
								ArrayList<byte[]> toUpload = streams.readData();
								shared.upload(names, toUpload);

								lock1.close();
								break;
							} catch (OverlappingFileLockException e) { sleep(1000); }

						break;
					case "delete":

						while (true)
							try {
								FileLock lock2 = writeChannel.lock(); // lock up

								ArrayList<String> toDelete = streams.readStrings();
								shared.delete(toDelete);

								lock2.close();
								break;
							} catch (OverlappingFileLockException e) { sleep(1000); }

						break;
					case "":

						stop = true;

						break;
					}

					sleep(1500);

				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			
			streams.close();
			dataStreams.remove(streams);
			socket.close();
		}
		
		
		private void initialize() throws IOException {

			// Initialize local
			streams.writeString("check");
			streams.writeStrings(shared.getFileNames());
			streams.writeData(filesBytes); // Check changes between local and shared
		}
	}
}
