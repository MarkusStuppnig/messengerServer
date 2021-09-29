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
				Main.shutdown();
			}
			
			if(message.equals("clients")) {
				System.out.println("\n----Clients----");
				for(Client client : Main.clients) {
					System.out.println(client.username);
				}
				System.out.println("\n----Clients----");
			}
			
			if(message.equals("version")) {
				System.out.println("Version: v" + Main.version);
			}
		}
	}
}