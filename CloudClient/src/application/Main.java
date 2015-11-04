package application;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

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
	SecretKey aes_key;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			connection = new ConnectionSocket("pizzakatze.no-ip.org", 1337);
			
			keyExchange();
			
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
	
	private void keyExchange() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException{
		System.out.println("Key exchange");
		System.out.println("Generating AES key");
		KeyGenerator aesgen = KeyGenerator.getInstance("AES");
		SecretKey aes_key = aesgen.generateKey();
		
		System.out.println("Requesting server key");
		PublicKey spub = getServerKey();
		connection.waitUntilOK();
		System.out.println("Encrypting connection");
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.ENCRYPT_MODE, spub);
		connection.setOutputCipher(c);
		System.out.println("Sending key");
		connection.sendCommand(Command.AES_TRANSMISSION);
		connection.sendObject(aes_key);
		
		connection.waitUntilOK();
		
		System.out.println("Encrypting the connection (AES)");
		Cipher cin = Cipher.getInstance("AES");
		Cipher cout = Cipher.getInstance("AES");
		cin.init(Cipher.DECRYPT_MODE, aes_key);
		cout.init(Cipher.ENCRYPT_MODE, aes_key);
		
		connection.setOutputCipher(cout);
		connection.setInputCipher(cin);
		this.aes_key = aes_key;
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
