package org.foxail.android.reader.client;

import java.util.HashMap;
import java.util.Map;

public class ClientFactory {
	
	private static ClientFactory instance;
	
	private Map<ClientSource, Client> clients = new HashMap<>();
	
	public static ClientFactory getInstance()  {
		if (instance == null) {
			instance = new ClientFactory();
		}
		return instance;
	}
	
	public Client getClient(ClientSource clientSource) {
		if (clientSource == null) {
			return null;
		}
		
		Client client = clients.get(clientSource);
		if (client == null) {
			try{
				client = (Client) clientSource.getClientClass().newInstance();
			}catch(Exception e){
				// do nothing
			}
			clients.put(clientSource, client);
		}
		return client;
	}

}
