package connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Streams {
	
	
	private ObjectOutputStream dos;
	private ObjectInputStream dis;
	
	//Create Connection
	public Streams(Socket socket) {
		
		try {
			
			dos = new ObjectOutputStream(socket.getOutputStream());		//Create output stream
			System.out.println("Output Stream Operational");
			dis = new ObjectInputStream(socket.getInputStream());		//Create Input Stream
			System.out.println("Connection Fully Operational");
			
		} catch (IOException e) {

			System.out.println("Error Creating Stream " + e);
		}      	
	}
	
	
	//Write one String
	public void writeString(String string) {
		
		try {
			dos.writeUTF(string);
		} catch (IOException e) {
			
			System.out.println("Error Sending String " + e);
		}
	}
	
	
	//Read one String
	public String readString() {
		
		try {
			return dis.readUTF();
		} catch (IOException e) {

			System.out.println("Error Reading String " + e);
		}
		
		return "";
	}
	

	//Read data in the form of an ArrayList of Strings
	public void writeStrings(ArrayList<String> output) {

		try {
			
			dos.writeInt(output.size());				//Size of ArrayList so reader knows when to stop reading

			for (int i = 0; i < output.size(); i++)
				dos.writeUTF(output.get(i));			//Read this UTF string
			
			dos.flush();								//Send any buffered data

		} catch (IOException e) {

			System.out.println("Error Writing Strings " + e);
		}
	}
	
	
	//Read data in the form of an ArrayList of Strings
	public ArrayList<String> readStrings() {
		
		ArrayList<String> strings = new ArrayList<>();
		
		try {
			
			int size = dis.readInt();			//Size of ArrayList

			//Read another String until the counter gets to the size of the ArrayList
			for (int i = 0; i < size; i++)
				strings.add(dis.readUTF());		//Read this UTF string

		} catch (IOException e) {

			System.out.println("Error Reading Strings " + e);
		}
		
		return strings;
	}
	

	//Write data in the form of an ArrayList of byte arrays
	public void writeData(ArrayList<byte[]> output) {

		try {
			
			dos.writeInt(output.size());					//Size of ArrayList so reader knows when to stop reading

			for (int i = 0; i < output.size(); i++) {
				
				dos.writeInt(output.get(i).length);					//Size of current byte array
				dos.write(output.get(i), 0, output.get(i).length);	//Content of current byte array
			}
			
			dos.flush();									//Once we finish writing this sends any buffered bytes

		} catch (IOException e) {

			System.out.println("Error Writing Bytes " + e);
		}
	}
	
	
	//Read data in the form of an ArrayList of byte arrays
	public ArrayList<byte[]> readData() {
		
		ArrayList<byte[]> bytes = new ArrayList<>();
		
		try {
			
			int size = dis.readInt();	//Size of ArrayList so we know when to stop reading

			for (int i = 0; i < size; i++) {
				
				int length = dis.readInt();								//Length of each byte array
				bytes.add(new byte[length]);							//Create an empty byte array of this length
				dis.readFully(bytes.get(bytes.size()-1), 0, length);	//Copy incoming data to this byte array
			}

		} catch (IOException e) {

			System.out.println("Error Reading Bytes " + e);
		}
		
		return bytes;
	}
	
	
	//Closes input and output streams
	public void close() {
		
		try {
			dos.close();
			dis.close();
		} catch (IOException e) {

			System.out.println("Error Closing Streams " + e);
		}
	}
}
