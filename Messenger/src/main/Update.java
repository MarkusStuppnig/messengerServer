package main;

public class Update implements Runnable {

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(2000);
			}catch(InterruptedException e) {}
			
			
			System.out.println("doing");
			Main.releaseOldConnections();
		}
	}
}
