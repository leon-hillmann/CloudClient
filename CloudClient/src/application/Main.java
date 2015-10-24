package application;
	
import java.io.IOException;

import application.net.CloudFile;
import application.net.ConnectionSocket;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;


public class Main extends Application {
	Button bu_connect = new Button("Connect");
	ConnectionSocket connection;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			connection = new ConnectionSocket("raspberrypi", 1337);
			
			bu_connect.setOnAction(e ->{

			});
			
			primaryStage.setOnCloseRequest(e ->{
				connection.close();
			});
			
			StackPane root = new StackPane();
			root.getChildren().add(bu_connect);
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public CloudFile getServerRoot(){
		try {
			Object o = connection.sendObjectrCommand(Command.GET_ROOT_FILE);
			if(o != null){
				if(o instanceof CloudFile){
					CloudFile f = (CloudFile) o;
					f.printAllSubFiles();
					return f;
				}
			}else
				System.out.println("Received null");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	
}
