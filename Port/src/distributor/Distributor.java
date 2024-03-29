package distributor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import client.Client;
import main.Main;

public class Distributor implements Runnable {

    //Boolean contains if Server is ready to handle next Connection
    private boolean openToConnect;
    
    @Override
    public void run() {
        
        openToConnect = true;
        
        while(true) {
            if(openToConnect) {
                openToConnect = false;
                
                try {
                    
                    //Accepting Connection from Client
                    ServerSocket serverSocket = new ServerSocket(Main.loginPort);
                    Socket socket = serverSocket.accept();
                    
                    InputStream iStream = socket.getInputStream();
                    OutputStream oStream = socket.getOutputStream();
                    
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(iStream));
                    
                    String login_message = bufferedReader.readLine();
                    
                    //Check if Login is right
                    if(login_message.startsWith("Connect-To-Network-With-Username:")) {
                        
                        String username = login_message.substring("Connect-To-Network-With-Username:".length());
                        
                        //Check if User is online
                        if(!Main.isUserOnline(username)) {
                            int port = Main.useFreePort();
                            
                            //Handle new Client With Username and Port
                            Thread t = new Thread(new Client(username, port));
                            t.start();
                            
                            //Acknowledge and final Port
                            oStream.write(new String("Acknowledge-With-Port:" + port + "\n").getBytes());
                            oStream.flush();
                            
                        }else {
                            //Error Connected User is already online
                            oStream.write(new String("Error-405: User already online").getBytes());
                            oStream.flush();
                        }
                    }
                    
                    //Close Connection
                    socket.close();
                    serverSocket.close();
                    openToConnect = true;
                    
                }catch(IOException e) {
                	e.printStackTrace();
                }
            }
        }
    }
}