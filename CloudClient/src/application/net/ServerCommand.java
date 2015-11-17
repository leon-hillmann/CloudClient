package application.net;

import application.Command;

public class ServerCommand {
	
	private Command cmd;
	private int [] args;
	
	public ServerCommand(Command c, int [] a){
		cmd = c;
		args = a;
	}
	
	public Command getCommand(){
		return cmd;
	}
	
	public int [] getArgs(){
		return args;
	}
	
}
