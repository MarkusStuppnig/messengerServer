package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import main.Main;
import wyoni.object.YObject;

public class Client implements Runnable {

    //Die Verbindung zum Client
    public Socket socket;
    public ServerSocket serverSocket;
    
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
    
    //ArrayLists to write
    public ArrayList<String> clientsToWrite;
    
    public boolean hasAnswered;
    
    //Wenn der neue Listener erstellt wird, wird ihm gesagt, auf welchen Port er hören soll
    public Client(String username, int port) {
        this.username = username;
        this.port = port;
        
        this.clientsToWrite = new ArrayList<String>();
        
        this.hasAnswered = false;
        
        Main.clients.add(this);
    }
    
    @Override
    public void run() {
        
        try {
            this.serverSocket = new ServerSocket(this.port);
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
			System.out.println(this.username + ": wrote \"" + message + "\"");

            if(message != null) {
            	
            	if(message.equals("Still-Using")) {
        			this.hasAnswered = true;
        		}
            	if(message.equals("force-exit")) {
            		System.out.println("Force-Exit from Client: " + this.username + ".");
            		Main.shutdown();
            	}
            
				if(message.startsWith("Connect-To-Client-With-Username:")) {
					String user = message.substring("Connect-To-Client-With-Username:".length());
					this.clientsToWrite.add(user);
					
			    	this.sendMessage("Your-Now-Connected-To:" + user);
				}
				
				
				if(message.startsWith("Message:")) {
					final String messageToClient = message.substring("Message:".length());
					
					for(int i = 0; i < this.clientsToWrite.size(); i++) {
						if(Main.isUserOnline(this.clientsToWrite.get(i))) {
							Client c = Main.getClientByName(this.clientsToWrite.get(i));
							
							c.sendMessage("Message-From-Client:" + this.username + ":" + messageToClient);
							
						}else {
							YObject file = new YObject("/root/ohta/messages/" + this.clientsToWrite.get(i) + ".json", true);
							
							ArrayList<String> arr;
							if(file.getArray("messages." + this.username) != null) {
								arr = file.getArray("messages." + this.username);
							}else {
								arr = new ArrayList<String>();
							}
							
							arr.add(messageToClient);
							file.putArray("messages." + this.username, arr);
						}
					}
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
        	this.sendMessage("You got removed from the System. (No Response)");
            
            //Removing From Main Array
            Main.clients.remove(this);
            if(Main.clients.contains(this)) System.out.println(this.username + " fail");

            
            //Closing Thread and Streams
        	this.thread.interrupt();
        	this.bufferedReader.close();
            this.socket.close();
            this.serverSocket.close();
            
        }catch (IOException e) {}
    }
}