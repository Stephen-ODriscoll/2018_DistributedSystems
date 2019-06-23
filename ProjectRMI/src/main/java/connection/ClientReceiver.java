package connection;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import controller.GUIController;

public class ClientReceiver extends UnicastRemoteObject implements ClientInterface{
	
	private static final long serialVersionUID = 1L;
	GUIController control;

	protected ClientReceiver(GUIController control) throws RemoteException {
		super();
		
		this.control = control;
	}

	@Override
	public void check(ArrayList<String> newfileNames, ArrayList<byte[]> newFiles) throws RemoteException {
		
		control.checkChanges(newFiles, newfileNames);
	}

	@Override
	public void add(ArrayList<String> names, ArrayList<byte[]> toAdd) throws RemoteException {
		
		control.addFiles(names, toAdd);
	}

	
}
