package acsse.csc2b.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Create image server and accept connections with client
 * 
 * @author TF Salimon
 */
public class ImgServer {

	private ServerSocket server;
	private boolean running;
	
	/**
	 * 
	 * @param port port used by the server
	 */
	public ImgServer(int port) {
		try {
			server = new ServerSocket(port);
			running = true;
			startServer();
		} catch (IOException e) {
			System.err.println("Could not start server");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 */
	private void startServer() {
		System.out.println("Starting the server...");
		
		while(running) {
			try {
				Socket clientSocket = server.accept();
				System.out.println("Client connected");
				
				ImgHandler handler = new ImgHandler(clientSocket);
				Thread thread = new Thread(handler);
				thread.start();
				
			}catch(IOException e) {
				System.err.println("Could not connect to client");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Main point of application 
	 * 
	 * @param args The command line arguments
	 */
	public static void main(String[] args) {
		ImgServer s = new ImgServer(5432);
	}
}
