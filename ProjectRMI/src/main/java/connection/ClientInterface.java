package connection;

import java.rmi.*;
import java.util.ArrayList;

public interface ClientInterface extends Remote {
	
	public void check(ArrayList<String> newfileNames, ArrayList<byte[]> newFiles) throws RemoteException;

	public void add(ArrayList<String> names, ArrayList<byte[]> toAdd) throws RemoteException;
}
