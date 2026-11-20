package acsse.csc2b.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * img client GUI interface
 * 
 * @author TF Salimon
 */
public class ImgClientPane extends GridPane {
	
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private PrintWriter pw;
	private BufferedReader br;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String[] listData;
	
	//GUI:
	private Button btnConnect;
	private Button btnPULL;
	private TextField txtIDToRetrieve;
	private Label lblID;
	private Button btnDownload;
	private Button btnUpload;
	TextArea listArea;
	private TextArea responseArea;
	private Label lblList;
	private Label lblResponse;
	private ImageView imgView;
	private Button btnDisplay;
	private String fileToGetName ="";
	
	/**
	 * 
	 * @param stage
	 */
	public ImgClientPane(Stage stage) {
		setupUI();
		
		btnConnect.setOnAction((e)->{
			try {
				socket = new Socket("localhost",5432);
				os = socket.getOutputStream();
				is = socket.getInputStream();
				br = new BufferedReader(new InputStreamReader(is));
				pw  =new PrintWriter(os);
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);
				
				responseArea.appendText("Connected to the server\n");
				
			} catch (IOException e1) {
				responseArea.appendText("Could not establish connection to the server\n");
				e1.printStackTrace();
			}
		});
		
		btnPULL.setOnAction((e)->{
			
			sendCommand(pw, "LIST");
			
			String response = "";
			String line = readResponse(br);
			
			while(line != null && !line.equals("END")) {
				if(!line.isEmpty()) {
					response += line + "\n";
				}
				
				line = readResponse(br);
			}
			
			listArea.setText(response);
			listData = response.split("\n");
			
			responseArea.appendText("Image list has been received\n");
		});
		

		btnDownload.setOnAction((e)->{
			String imageID = txtIDToRetrieve.getText();
			String fileName = "";
			
			// get the filename using the id
			for(int i = 0; i < listData.length; i++) {
				StringTokenizer st = new StringTokenizer(listData[i]);
				
				String id = st.nextToken();
				String name = st.nextToken();
				
				if(id.equals(imageID)) {
					fileName = name;
					break;
				}
			}
			
			if(!fileName.isEmpty()) {
				
				sendCommand(pw, "DOWN " + imageID);
				
				int fileSize = Integer.parseInt(readResponse(br));
				
				if(fileSize > 0) {
					File file = new File("data/client/" + fileName);
					
					try {
						
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
							
							if(n == -1) {
								break;
							}
							
							fos.write(buffer, 0, n);
							fos.flush();
							
							totalBytes += n;
						}
						
						fos.close();
						
						fileToGetName = "data/client/" + fileName;
						
						responseArea.appendText("Image successfully downloaded\n");
						
					} catch(IOException ex) {
						responseArea.appendText("Failed too download image\n");
						ex.printStackTrace();
					}
				}
				else {
					responseArea.appendText("Image could not be found\n");
				}
			}
			else {
				responseArea.appendText("Error - Invalid image id\n");
			}
		});
		
		
		btnUpload.setOnAction((e)->{
			
			String imageID = txtIDToRetrieve.getText();
			
			FileChooser fc = new FileChooser();
			File file = fc.showOpenDialog(stage);
			
			if(file != null && !imageID.isEmpty()) {
				
				String fileName = file.getName();
				long fileSize = file.length();
				
				sendCommand(pw, "UP " + imageID + " " + fileName + " " + fileSize);
				
				try {
					FileInputStream fis = new FileInputStream(file);
					
					byte[] buffer = new byte[2048];
					int n = 0;
					
					while((n = fis.read(buffer)) > 0) {
						dos.write(buffer, 0, n);
						dos.flush();
					}
					
					fis.close();
					
					String response = readResponse(br);
					responseArea.appendText("Server response: " + response + "\n");
					
				} catch(IOException ex) {
					responseArea.appendText("Could not upload image\n");
					ex.printStackTrace();
				}
			}
			else {
				responseArea.appendText("Enter ID then select image\n");
			}
		});
		

		btnDisplay.setOnAction((e)->{
			try {
				FileInputStream fis = new FileInputStream(fileToGetName);
				
				Image image = new Image(fis);
				imgView.setImage(image);
				
				responseArea.appendText("Image displayed\n");
				
			} catch(FileNotFoundException ex) {
				responseArea.appendText("Image could not be displayed\n");
				ex.printStackTrace();
			}
		});
	}
	
	/**
	 * setup GUI interface
	 */
	private void setupUI()
	{
		setHgap(13);
		setVgap(18);
		
		btnConnect = new Button("Connect");
		btnPULL = new Button("PULL");
		btnDownload = new Button("Download");
		btnUpload = new Button("Upload");
		btnDisplay = new Button("Display Img");
		
		lblID = new Label("Enter img ID:");
		lblList = new Label("Img List:");
		lblResponse = new Label("Server response:");
		
		listArea = new TextArea();
		responseArea = new TextArea();
		
		txtIDToRetrieve = new TextField();
		
		imgView = new ImageView();
		
		add(btnConnect, 0, 0);
		add(btnPULL, 2, 0);
		add(lblList, 0, 1);
		add(listArea, 0, 2, 3, 1);
		add(lblID, 0, 3);
		add(txtIDToRetrieve, 1, 3, 2, 1);
		add(btnDownload, 0, 4);
		add(btnUpload, 2, 4);
		add(btnDisplay, 3, 5);
		add(lblResponse, 0, 5);
		add(responseArea, 0, 6, 2, 1);
		add(imgView, 3, 2);
	}

	/**
	 * 
	 * @param br
	 * @return
	 */
	private String readResponse(BufferedReader br)
	{
		String response = "";
		try {
			response= br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * 
	 * @param pw
	 * @param msg
	 */
	private void sendCommand(PrintWriter pw, String msg)
	{
		pw.println(msg);
		pw.flush();
	}
	

}
