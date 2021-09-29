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
			
			if(message.equalsIgnoreCase("send")) {
				Main.releaseOldConnections();
			}
			
			if(message.equalsIgnoreCase("exit")) {
				scan.close();
				Main.shutdown();
			}
			
			if(message.equalsIgnoreCase("clients")) {
				System.out.println("\n----Clients----");
				for(Client client : Main.clients) {
					System.out.println(client.username);
				}
				System.out.println("----Clients----");
			}
			
			if(message.equalsIgnoreCase("version")) {
				System.out.println("Version: v" + Main.version);
			}
		}
	}
}