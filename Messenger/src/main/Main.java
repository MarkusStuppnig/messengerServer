package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import client.Client;
import distributor.Distributor;

//----------Errors-----------
//Error-404: Username not found
//Error-405: User already online
//Error-406: No Client response

//----------Commands---------
//exit: 	Shutdown Server
//send:		Check connected Clients
//clients:	List all connected Clients
//version:  Prints Version

public class Main {

	//Version
	public static final double version = 0.45;
	
    //Listener Port for Login(Standard: 4200)
    public static final int loginPort = 4200;
    
    //Infos to Clients
    public static ArrayList<Client> clients = new ArrayList<>();
    
    //Thread
    public static Thread mainScanner;
    
    //Main Methode
    public static void main(String[] args) {
        setup();
    }
    
    //Setup Methode
    public static void setup() {
        
        //Starting Listener
        Thread distributor = new Thread(new Distributor());
        distributor.start();
        
        mainScanner = new Thread(new MainScanner());
        mainScanner.start();
    }
    
    public static void shutdown() {
    	
    	//Shutdown
    	System.out.println("Server Shutdown");
    	
    	//Closing Threads
    	mainScanner.interrupt();
    	
    	//Removing Clients from Array
    	for(int i = 0; i < Main.clients.size(); i++) {
    		Main.clients.get(0).removeClient();
		}
		
		System.exit(0);
    }
    
    //Get Free Port
    public static int getFreePort() {
    	
    	//Put All Used Ports in portsInUse
        List<Integer> portsInUse = new ArrayList<>();
        for(Client client : clients) {
        	portsInUse.add(client.port);
        }
        
        //Check littelest free port
        for(int i = 0; i <= portsInUse.size(); i++) {
        	if(!portsInUse.contains((Object) (4201 + i))) {
        		return (4201 + i);
        	}
        }
        
        return -1;
    }
    
    //Check if User is Online
    public static boolean isUserOnline(final String username) {
        for(Client client : clients) {
            if(client.username.equals(username)) return true;
        }
        return false;
    }
    
    public static Client getClientByName(final String username) {
    	for(Client client : clients) {
            if(client.username.equals(username)) return client;
        }
        return null;
    }
    
    public static void releaseOldConnections() {
    	
    	//Send All Clients
    	for(Client client : clients) {
    		client.sendMessage("Still-Using-Connection");
    	}
    	
    	//Wait 3 Seconds
    	try {
			Thread.sleep(3000);
		}catch (InterruptedException e) {}
    	
    	//Check All Clients
    	ArrayList<Client> clientsToRemove = new ArrayList<>();
    	for(Client client : clients) {
    		if(client.hasAnswered) {
    			System.out.println(client.username + ": has answered");
    			client.hasAnswered = false; //texotek erklärung
    		}else {
    			System.out.println(client.username + ": hasn't answered");
    			clientsToRemove.add(client);
    		}
    	}
    	
    	//Remove All Clients in List: clientsToRemove
    	Iterator<Client> iterator = clientsToRemove.iterator();
    	while(iterator.hasNext()) {
    		Client client = iterator.next();
    		client.removeClient();
    	}
    }
}