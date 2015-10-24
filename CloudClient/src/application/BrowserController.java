package application;

import java.io.IOException;

import application.net.CloudFile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

public class BrowserController extends AnchorPane{
	
	@FXML FlowPane content;
	
	public BrowserController(){
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Browser.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setCurrentFolder(CloudFile directory){
		for(CloudFile f : directory.getDirectoryContent()){
			Button button = new Button(f.get().getName());
			content.getChildren().add(button);
		}
	}
	
}
