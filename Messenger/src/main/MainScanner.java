package main;

import java.util.Scanner;

import client.Client;

public class MainScanner implements Runnable {

	@Override
	public void run() {
		Scanner scan = new Scanner(System.in);
		String message;
		
		while(true) {
			message = scan.nextLine();
			
			if(message.equals("send")) {
				Main.releaseOldConnections();
			}
			
			if(message.equals("exit")) {
				scan.close();
				for(Client client : Main.clients) {
					client.removeClient();
				}
				
				System.exit(0);
			}
		}
	}
}