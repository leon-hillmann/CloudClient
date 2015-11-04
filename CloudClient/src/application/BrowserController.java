package application;

import java.io.IOException;

import application.net.CloudFile;
import application.net.FileType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

public class BrowserController extends AnchorPane{
	
	@FXML FlowPane content;
	@FXML Button back;
	private CloudFile currentDir = null;
	
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
		
		back.setOnAction(e -> {
			if(currentDir == null)
				return;
			if(currentDir.getParent() == null)
				return;
			
			setCurrentFolder(currentDir.getParent());
		});
	}
	
	public void setCurrentFolder(CloudFile directory){
		if(directory.getType() != FileType.DIRECTORY)
			return;
		content.getChildren().removeAll(content.getChildren());
		for(CloudFile f : directory.getDirectoryContent()){
			Button button = new Button(f.get().getName());
			button.setOnAction(e -> {
				if(f.getType() == FileType.DIRECTORY){
					setCurrentFolder(f);
					System.out.println("DIR: " + f.get().getName());
				}else{
					System.out.println("FILE: " + f.get().getName());
				}
			});
			content.getChildren().add(button);
		}
		currentDir = directory;
	}
	
}
