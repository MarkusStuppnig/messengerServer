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
    
    //Der Thread des Clients
    public Thread thread;   
    
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
    
    public boolean hasAnswered;
    
    //Wenn der neue Listener erstellt wird, wird ihm gesagt, auf welchen Port er hören soll
    public Client(String username, int port) {
        this.username = username;
        this.port = port;
        
        this.connectedToOtherClient = false;
        this.hasAnswered = false;
        
        Main.clients.add(this);
    }
    
    @Override
    public void run() {
        
        try {
            @SuppressWarnings("resource")
            ServerSocket serverSocket = new ServerSocket(this.port);
            this.socket = serverSocket.accept();
            
            this.iStream = this.socket.getInputStream();
            this.oStream = this.socket.getOutputStream();
            
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.iStream));
            
            //Check if Username is right
            final String login_message = this.getMessage();
            
            if(login_message.startsWith("Login-In-With-Username:")) {
                if(login_message.substring("Login-In-With-Username:".length()).equals(this.username)) {
                    this.sendMessage("Successfully-Logged-In");
                    System.out.println(this.username + ": Logged in");
                    
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
        
        //Listening Clients Messages
        while(true) {
            final String message = this.getMessage();
            
            if(message != null) {
            	
            	if(message.equals("Still-Using")) {
        			this.hasAnswered = true;
        		}
            	if(message.equals("force-exit")) {
            		Main.shutdown();
            	}
            
				System.out.println(this.username + ": wrote \"" + message + "\"");
				
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
				
				
				if(message.startsWith("Message:")) {
					final String messageToClient = message.substring("Message:".length());
					
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
			return this.bufferedReader.readLine();
		}catch (IOException e) {
			return null;
		}
    }
    
    //Method which removes Client completely
    public void removeClient() {
        try {
        	//Sending Infos
        	System.out.println(this.username + ": Removed from System");
        	this.sendMessage("You got removed from the System.");
        	
        	//Closing Thread and Streams
        	this.thread.interrupt();
        	this.bufferedReader.close();
            this.socket.close();
            
            //Removing From Main Array
            Main.clients.remove(this);
            
        }catch (IOException e) {}
    }
}