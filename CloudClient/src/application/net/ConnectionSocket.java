package application.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import application.Command;

public class ConnectionSocket {
	
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	public ConnectionSocket(String ipAddress, int port) throws UnknownHostException, IOException{
		socket = new Socket(ipAddress, port);
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
	}
	
	private Command readCommand() throws IOException{
		Command cmd = Command.get(dis.readInt());
		System.out.println("SERVER:> " + cmd);
		return cmd;
	}
	
	public void sendCommand(Command cmd) throws IOException{
		dos.writeInt(cmd.getCode());
		dos.flush();
	}
	
	public Object sendObjectrCommand(Command cmd) throws IOException, ClassNotFoundException{
		dos.writeInt(cmd.getCode());
		dos.flush();
		Command server_cmd = readCommand();
		if(server_cmd == Command.OBJECT_TRANSMISSION){
			ObjectInputStream ois = new ObjectInputStream(dis);
			Object o = ois.readObject();
			return o;
		}else if(server_cmd == Command.UNKNOWN){
			System.out.println("Command sent to server was invalid");
		}else{
			System.out.println("Unknown Command");
		}
		return null;
	}
	
	public void close(){
		try{
			sendCommand(Command.CLOSE);
			dis.close();
			dos.close();
			socket.close();
			System.out.println("Connection closed");
		}catch(IOException e){
			
		}
	}
	
}
