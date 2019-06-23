package connection;

import java.rmi.*;
import java.util.ArrayList;

public interface ServerInterface extends Remote {
	
    public void add(ClientInterface c) throws RemoteException;
    
    public void download(ClientInterface c, ArrayList<String> names) throws RemoteException;
    
    public void upload(ArrayList<String> names, ArrayList<byte[]> files) throws RemoteException;
    
    public void delete(ArrayList<String> names) throws RemoteException;
    
    public void remove(ClientInterface c) throws RemoteException;
}