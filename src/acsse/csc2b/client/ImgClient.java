package acsse.csc2b.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Displays the javaFX img client
 * 
 * @author TF Salimon
 */
public class ImgClient  extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		ImgClientPane root = new ImgClientPane(primaryStage);
		Scene scene = new Scene(root, 700, 500);
		
		primaryStage.setTitle("Img Client");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
