package application;

import java.io.IOException;
import java.security.PublicKey;

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
	
	PublicKey serv_pub;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			connection = new ConnectionSocket("pizzakatze.no-ip.org", 1337);
			
			serv_pub = getServerKey();
			System.out.println(serv_pub.toString());
			
			rootDirectory = getServerRoot();
			if(rootDirectory == null){
				System.out.println("root is null");
				System.exit(0);
			}
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
	
	private PublicKey getServerKey(){
		return (PublicKey) getObjectFromServer(Command.PUBLIC_KEY_REQUEST);
	}
	
	private Object getObjectFromServer(Command c){
		try {
			Object o = connection.sendObjectrCommand(c);
			if(o == null)
				System.out.println("Received null");
			return o;
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public CloudFile getServerRoot(){
		return (CloudFile) getObjectFromServer(Command.GET_ROOT_FILE);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
