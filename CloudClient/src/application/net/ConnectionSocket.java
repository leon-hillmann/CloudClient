package application.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import application.Command;

public class ConnectionSocket {

	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private String ipAdd;
	private int port;

	public ConnectionSocket(String ipAddress, int port) throws UnknownHostException, IOException, NoSuchAlgorithmException{
		this.ipAdd = ipAddress;
		this.port = port;
		socket = new Socket(ipAddress, port);
		dos = new DataOutputStream(os = socket.getOutputStream());
		dis = new DataInputStream(is = socket.getInputStream());
		ois = new ObjectInputStream(is);
		oos = new ObjectOutputStream(os);
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

	public void setOutputCipher(Cipher c){
		try {
			dos = null;
			oos = null;
			System.out.println("Create CipherOutputStream");
			CipherOutputStream cos = new CipherOutputStream(os = socket.getOutputStream(), c);
			System.out.println("Create DataOutputStream");
			dos = new DataOutputStream(cos);
			System.out.println("Create ObjectOutputStream");
			oos = new ObjectOutputStream(dos);
			System.out.println("----------Done!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setInputCipher(Cipher c){
		try {
			dis = null;
			ois = null;
			System.out.println("Create CipherInputStream");
			CipherInputStream cis = new CipherInputStream(is = socket.getInputStream(), c);
			System.out.println("Create DataInputStream");
			dis = new DataInputStream(cis);
			System.out.println("----------Done!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendObject(Object o){
		try{
			sendCommand(Command.OBJECT_TRANSMISSION);
			oos.writeObject(o);
			oos.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public Object sendObjectrCommand(Command cmd) throws IOException, ClassNotFoundException{
		dos.writeInt(cmd.getCode());
		dos.flush();
		Command server_cmd = readCommand();
		if(server_cmd == Command.OBJECT_TRANSMISSION){
			Object o = ois.readObject();
			return o;
		}else if(server_cmd == Command.UNKNOWN){
			System.out.println("Command sent to server was invalid");
		}else{
			System.out.println("Unknown Command");
		}
		return null;
	}

	public void waitUntilOK(){
		System.out.println("Waiting for server ok");
		try{
			Command c;
			while((c = Command.get(dis.readInt())) != Command.OK){
				System.out.println(c);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void close(){
		try{
			sendCommand(Command.CLOSE);
			dis.close();
			dos.close();
			oos.close();
			ois.close();
			socket.close();
			System.out.println("Connection closed");
		}catch(IOException e){

		}
	}

}
