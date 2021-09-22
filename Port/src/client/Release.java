package client;

public class Release implements Runnable {

	private Client client;
	public boolean answered;

	public Release(Client client) {
		this.client = client;
		this.answered = false;
	}
	
	@Override
	public void run() {
		String message = this.client.getMessage();
		
		if(message.equals("Still-Using")) {
			this.answered = true;
		}
	}
}