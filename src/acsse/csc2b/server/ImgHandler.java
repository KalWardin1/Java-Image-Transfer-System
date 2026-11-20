package acsse.csc2b.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Handles requests from a image client
 * 
 * @author TF Salimon
 */
public class ImgHandler implements Runnable {
	private Socket incomingConnection;
	private OutputStream os;
	private InputStream is;
	private PrintWriter pw;
	private BufferedReader br;
	private DataOutputStream dos;
	private DataInputStream dis;
	
	/**
	 * 
	 * @param s
	 */
	public ImgHandler(Socket s) {
		this.incomingConnection = s;
		try {
			os = incomingConnection.getOutputStream();
			is = incomingConnection.getInputStream();
			pw = new PrintWriter(os);
			br = new BufferedReader(new InputStreamReader(is));
			dos = new DataOutputStream(os);
			dis = new DataInputStream(is);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
		System.out.println("Handling Client Requests");
		
		try {
			boolean running = true;
			
			while(running) {
				String request = br.readLine();
				
				if (request != null && !request.isEmpty()) {
					StringTokenizer st = new StringTokenizer(request);
					
					String command = st.nextToken();
					
					if (command.equals("LIST")) {
						String imgList = loadImgList();
						
						pw.println(imgList);
						pw.println("END");
						pw.flush();
						
						System.out.println("Image list sent");
					}
					else if(command.equals("DOWN")) {
						
						String imageID = st.nextToken();
						String fileName = getFileNameFromID(imageID);
						
						File file = new File("data/server/" + fileName);
						
						if(file.exists()) {
							// send the file size
							pw.println(file.length());
							pw.flush();
							
							System.out.println("File size: " + file.length());
							
							// send the bytes
							FileInputStream fis = new FileInputStream(file);
							
							byte[] buffer = new byte[2048];
							int n = 0;
							
							while((n = fis.read(buffer)) > 0) {
								dos.write(buffer, 0, n);
								dos.flush();
							}
							
							fis.close();
							System.out.println("Image sent to the client");
						}
						else {
							pw.println(-1);
							pw.flush();
							
							System.out.println("Image could not be found");
						}
					}
					else if(command.equals("UP")) {
						
						String imageID = st.nextToken();
						String fileName = st.nextToken();
						int fileSize = Integer.parseInt(st.nextToken());
						
						File file = new File("data/server/" + fileName);
						FileOutputStream fos = new FileOutputStream(file);
						
						byte[] buffer = new byte[2048];
						int n = 0;
						int totalBytes = 0;
						
						while(totalBytes < fileSize) {
							int remBytes = fileSize - totalBytes;
							int bytesToRead;
							
							if(remBytes < buffer.length) {
								bytesToRead = remBytes;
							}
							else {
								bytesToRead = buffer.length;
							}
							
							n = dis.read(buffer, 0, bytesToRead);
							
							if (n == -1) {
								break;
							}
							
							fos.write(buffer, 0, n);
							fos.flush();
							
							totalBytes += n;
						}
						
						fos.close();
						
						if(totalBytes == fileSize) {
							FileWriter fw = new FileWriter("data/server/ImgList.txt", true);
							BufferedWriter bw = new BufferedWriter(fw);
							
							bw.newLine();
							bw.write(imageID + " " + fileName);
							bw.close();
							
							pw.println("SUCCESS");
							pw.flush();
							
							System.out.println("Image successfully uploaded");
						}
						else {
							pw.println("FAILURE");
							pw.flush();
							
							System.out.println("Failed to upload image");
						}
					}
					else if(command.equals("CLOSE")) {
						running = false;
					}
				}
				else if(request == null) {
					running = false;
				}
			}
			
			incomingConnection.close();
			System.out.println("Client disconnected");
			
		}
		catch(IOException e)
		{
			System.err.println("Connection with client has been lost");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param searchID
	 * @return
	 */
	private String getFileNameFromID(String searchID)
	{
		String ret = "";
		File imglist = new File("data/server/ImgList.txt");
		try {
			
			Scanner txtIn = new Scanner(imglist);
			
			while(txtIn.hasNextLine()) {
				String line = txtIn.nextLine();
				
				if(!line.isEmpty()) {
					StringTokenizer st = new StringTokenizer(line);
					
					String id = st.nextToken();
					String fileName = st.nextToken();
					
					if(id.equals(searchID)) {
						ret = fileName;
						break;
					}
				}
				
			}
			
			txtIn.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
			
		return ret;
	}
	
	//Marks for Returning of image files list
	/**
	 * 
	 * @return
	 */
	private String loadImgList() {
		String ret = "";
		File imglist = new File("data/server/ImgList.txt");
		try {
			Scanner txtIn = new Scanner(imglist);
			
			while(txtIn.hasNextLine()) {
				String line = txtIn.nextLine();
				
				if(!line.isEmpty()) {
					ret += line + "\n";
				}
			}
			txtIn.close();
			
		}catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return ret; 
	}

}
