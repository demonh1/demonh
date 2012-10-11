package com.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author demonh
 */

public class ClientThread {
	ObjectInputStream sInput;
	ObjectOutputStream sOutput;
	Socket socket;
	private int port;
	private boolean keepGoing;
	private static int conectionId;
	int id;
	String userName;
	String date;
	ChatMessage cm;
	private ServerGUI sg;
	private SimpleDateFormat sdf;
	private ArrayList<ClientThread> al;

	public ClientThread(Socket socket) {

		// TODO Auto-generated constructor stub
		id = ++conectionId;
		this.socket = socket;
		System.out
				.println("Thread trying to create Object Input/Output Streams");

		try {
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			sInput = new ObjectInputStream(socket.getInputStream());
			// read the username
			try {
				userName = (String) sInput.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			display(userName + " just connected.");

		} catch (IOException e) {
			display("Exception creating new Input/output Streams: " + e);
			return;
		}

	}

	private void display(String msg) {
		// TODO Auto-generated method stub
		String time = sdf.format(new Date(port)) + " " + msg;
		if (sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");

	}

	public void start() {
		keepGoing = true;
		/* create socket server and wait for connection requests */
		try {
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections
			while (keepGoing) {
				// format message saying we are waiting
				display("Server waiting for Clients on port " + port + ".");

				Socket socket = serverSocket.accept(); // accept connection
				// if I was asked to stop
				if (!keepGoing)
					break;
				ClientThread t = new ClientThread(socket); // make a thread of
															// it
				al.add(t); // save it in the ArrayList
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for (int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					} catch (IOException ioE) {
						// not much I can do
					}
				}
			} catch (Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
			String msg = sdf.format(new Date())
					+ " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}

	}

	private void close() {
		// try to close the connection
		try {
			if (sOutput != null)
				sOutput.close();
		} catch (Exception e) {
		}
		try {
			if (sInput != null)
				sInput.close();
		} catch (Exception e) {
		}
		;
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
		}
	}

	public boolean writeMsg(String message) {
		if (!socket.isConnected()) {
			close();
			return false;
		}
		// write the message to the stream
		try {
			sOutput.writeObject(message);
		}
		// if an error occurs, do not abort just inform the user
		catch (IOException e) {
			display("Error sending message to " + userName);
			display(e.toString());
		}
		return false;
	}

}
