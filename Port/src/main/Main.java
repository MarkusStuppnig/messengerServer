package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import client.Client;
import client.Release;
import distributor.Distributor;

//Error-404: Username not found
//Error-405: User already online
//Error-406: No Client response

public class Main {

    //Listener Port for Login(Standard: 4200)
    public static final int loginPort = 4200;
    
    //Infos to Clients
    public static ArrayList<Client> clients = new ArrayList<>();
    
    //Main Methode
    public static void main(String[] args) {
        setup();
    }
    
    //Setup Methode
    public static void setup() {
        
        //Starting Listener
        Thread distributor = new Thread(new Distributor());
        distributor.start();
    }
    
    //Get Free Port
    public static int useFreePort() {
        int port = 4201;
        
        List<Integer> portsInUse = new ArrayList<>();
        for(Client client : clients) {
            portsInUse.add(client.port);
        }
        
        if(portsInUse.size() > 0) {
            int[] ports = portsInUse.stream().mapToInt(i -> i).toArray();
            Arrays.sort(ports);
            
            port = ports[ports.length - 1] + 1;
            
        }

        return port;
    }
    
    //Chek if User is Online
    public static boolean isUserOnline(String username) {
        for(Client client : clients) {
            if(client.username.equals(username)) return true;
        }
        return false;
    }
    
    public static Client getClientByName(String username) {
        for(Client client : clients) {
            if(client.username.equals(username)) return client;
        }
        return null;
    }
    
    //Fehler: Muss noch rausgefunden werden!!!
    public static void releaseOldConnections() {
        
    	Iterator<Client> iterator = clients.iterator();
    	ArrayList<Client> clientsToRemove = new ArrayList<>();
    	
        while(true) {
        	iterator = clients.iterator();
        	
        	if(!iterator.hasNext()) break;
        	
        	Client client = iterator.next();
            client.sendMessage("Still-Using-Connection");
            
            Release release = new Release(client);
            
            Thread t = new Thread(release);
            t.start();
            
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e) {}
            
            if(!release.answered) {
            	clientsToRemove.add(client);
            }
            
            t.interrupt();
        }
        
        for(int i = 0; i < clientsToRemove.size(); i++) {
        	clientsToRemove.get(0).removeClient();
        }
    }
}