package application.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import application.Command;

public class ConnectionSocket {

	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Cipher cin = null;
	private Cipher cout = null;
	private String ipAdd;
	private int port;

	public ConnectionSocket(String ipAddress, int port) throws UnknownHostException, IOException, NoSuchAlgorithmException{
		this.ipAdd = ipAddress;
		this.port = port;
		socket = new Socket(ipAddress, port);
		dos = new DataOutputStream(os = socket.getOutputStream());
		dis = new DataInputStream(is = socket.getInputStream());
	}

	private ServerCommand readCommand() throws IOException{
		Command cmd = Command.get(dis.readInt());
		List<Integer> argsList = new ArrayList<Integer>();
		int a;
		while((a = dis.readInt()) != Integer.MIN_VALUE)
			argsList.add(a);
		int [] args = new int[argsList.size()];
		for(int i = 0; i < args.length; i++){
			args[i] = argsList.get(i);
		}
		System.out.println("SERVER:> " + cmd + " with " + args.length * Integer.SIZE + "bytes of data.");
		return new ServerCommand(cmd, args);
	}

	public void sendCommand(Command cmd) throws IOException{
		sendCommand(cmd, null);
	}
	
	public void sendCommand(Command cmd, int[] args) throws IOException{
		dos.writeInt(cmd.getCode());
		if(args != null){
			for (int i : args){
				dos.writeInt(i);
			}
		}
		dos.writeInt(Integer.MIN_VALUE);
		dos.flush();
	}

	public void setOutputCipher(Cipher c){
		cout = c;
		os = new CipherOutputStream(os, c);
		dos = new DataOutputStream(os);
		System.out.println("Applied to Output");
	}

	public void setInputCipher(Cipher c){
		cin = c;
		is = new CipherInputStream(is, c);
		dis = new DataInputStream(is);
		System.out.println("Applied to Input");
	}
	/**
	 * schreibt das gegebene object in der OutputStream.
	 * Vorher Command.OBJECT_TRANSMISSION mit der länge (in bytes) des Objects.
	 * Verwendet cout zum verschlüsseln, wenn nicht null.
	 * @param o das zu sendende Object
	 */
	public void sendObject(Object o){
		try{
			byte [] objectBytes = getObjectBytes(o);
			sendCommand(Command.OBJECT_TRANSMISSION, new int[] {objectBytes.length});
			System.out.println("Sending " + objectBytes.length + " bytes long Object");
			dos.write(objectBytes);
			dos.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	/**
	 * gibt die bytes eines Objects zurück..
	 * @param o das object
	 * @return Object-Bytes
	 */
	private byte[] getObjectBytes(Object o){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(o);
			oos.flush();
			byte [] result = bos.toByteArray();
			oos.close();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(oos != null)
				try {
					oos.close();
				} catch (IOException e) {}
		}
		return null;
		
	}


	/**
	 * gibt ein Object aus einem byte[] zurück und entschlüsselt diese mit dem Cipher c.
	 * @param bytes die bytes
	 * @param c der cipher
	 * @return das Object
	 */
	
	private Object getBytesObject(byte[] bytes){
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = null;
			ois = new ObjectInputStream(bis);
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public Object sendObjectrCommand(Command cmd) throws IOException, ClassNotFoundException{
		dos.writeInt(cmd.getCode());
		dos.flush();
		ServerCommand server_command = readCommand();
		Command cmdRec = server_command.getCommand();
		if(cmdRec == Command.OBJECT_TRANSMISSION){
			int length = server_command.getArgs()[0];
			System.out.println("Object length: " + length);
			byte [] objectBytes = new byte[length];
			dis.read(objectBytes);
			return getBytesObject(objectBytes);
		}else if(cmdRec == Command.UNKNOWN){
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
