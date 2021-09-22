package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import main.Main;

public class Client implements Runnable {

    //Die Verbindung zum Client
    public Socket socket;
    
    //Hiermit wird zum Client eschrieben
    private OutputStream oStream = null;
    
    //Hiermit wird gelesen, was vom Client geschrieben wird
    private InputStream iStream = null;
    private BufferedReader bufferedReader = null;
    
    //Basic Infos werden abgespeichert
    public String username;
    public int port;
    
    public boolean connectedToOtherClient;
    public Client clientToWrite;
    
    //Wenn der neue Listener erstellt wird, wird ihm gesagt, auf welchen Port er hören soll
    public Client(String username, int port) {
        this.username = username;
        this.port = port;
        this.connectedToOtherClient = false;
        
        Main.clients.add(this);
    }
    
    @Override
    public void run() {
        
        System.out.println(this.username + " : Listening on Port " + this.port);
        
        try {
            @SuppressWarnings("resource")
            ServerSocket serverSocket = new ServerSocket(this.port);
            this.socket = serverSocket.accept();
            
            this.iStream = this.socket.getInputStream();
            this.oStream = this.socket.getOutputStream();
            
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.iStream));
            
            //Check if Username is right
            String login_message = this.getMessage();
            
            if(login_message.startsWith("Login-In-With-Username:")) {
                if(login_message.substring("Login-In-With-Username:".length()).equals(this.username)) {
                    this.sendMessage("Successfully-Logged-In");
                    
                }else {
                    this.sendMessage("Error-404: Username not found");
                    removeClient();
                }
            }else {
                removeClient();
            }
            
        }catch(IOException e) {
            removeClient();
        }
        
        //Release all other Client which aren't connected anymore
        Main.releaseOldConnections();
        
        //Listening Clients Messages
        while(true) {
            String message = this.getMessage();
			
			if(message == null) return;
			
			if(message.startsWith("Connect-To-Client-With-Username:")) {
				String user = message.substring("Connect-To-Client-With-Username:".length());
				
				if(Main.isUserOnline(user)) {
					Client client = Main.getClientByName(user);
					
					this.clientToWrite = client;
					this.connectedToOtherClient = true;
					
			    	client.clientToWrite = this;
			    	client.connectedToOtherClient = true;
			    	
			    	this.sendMessage("Your-Now-Connected-To:" + user);
			    	client.sendMessage("Your-Now-Connected-To:" + this.username);
			    	
				}else {
					this.sendMessage("Error-406: No Client response");
				}
			}
			
			if(this.connectedToOtherClient) {
				if(message.startsWith("Message:")) {
					String messageToClient = message.substring("Message:".length());
					
					this.clientToWrite.sendMessage("Message-From-Client:" + this.username + ":" + messageToClient);
				}
			}
        }
    }
    
    //Send message to Client Object
    public void sendMessage(String message) {
        try {
            this.oStream.write(new String(message + "\n").getBytes());
            this.oStream.flush();
            
        }catch(IOException e) {}
    }
    
    //Get Message from Client
    public String getMessage() {
    	try {
    		final String message = this.bufferedReader.readLine();
    		System.out.println(this.username + ": Got: \"" + message);
    		
    		if(message.equals("!stop")) {
    			System.exit(0);
    		}
			return message;
		}catch (IOException e) {
			return null;
		}
    }
    
    //Method which removes Client completely
    public void removeClient() {
        try {
            this.socket.close();
            Main.clients.remove(this);
            
            System.out.println(this.username + ": Removed of System");
            
        }catch (IOException e) {}
    }
}