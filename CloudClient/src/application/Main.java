package application;

import application.net.CloudFile;
import application.net.ConnectionSocket;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;


public class Main extends Application {
	Button bu_connect = new Button("Connect");
	ConnectionSocket connection;
	BrowserController browser;
	CloudFile rootDirectory;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			connection = new ConnectionSocket("pizzakatze.no-ip.org", 1337);
			
			rootDirectory = getServerRoot();
			primaryStage.setOnCloseRequest(e ->{
				connection.close();
			});
			browser = new BrowserController();
			Scene browserScene = new Scene(browser);
			browserScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(browserScene);
			primaryStage.show();
			
			browser.setCurrentFolder(rootDirectory);
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
