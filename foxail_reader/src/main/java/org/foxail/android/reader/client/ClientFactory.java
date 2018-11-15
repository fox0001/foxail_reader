package org.foxail.android.reader.client;

import java.util.HashMap;
import java.util.Map;

public class ClientFactory {
	
	private Map<String, String> clientMap = new HashMap<>();
	
	private static ClientFactory instance;
	
	private Map<String, Client> clients = new HashMap<>();
	
	public static ClientFactory getInstance()  {
		if (instance == null) {
			instance = new ClientFactory();
			instance.clientMap.put("cnbeta", "org.foxail.android.reader.client.CnBetaClient");
		}
		return instance;
	}
	
	public Client getClient(String clientName) {
		if (!clientMap.containsKey(clientName)) {
			return null;
		}
		
		Client client = clients.get(clientName);
		if (client == null) {
			String className = clientMap.get(clientName);
			try{
				client = (Client) Class.forName(className).newInstance();
			}catch(Exception e){
				
			}
			clients.put(clientName, client);
		}
		return client;
	}

}
