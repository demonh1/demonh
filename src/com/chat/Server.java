package com.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author demonh
 */

public class Server {
	private int port;
	private ServerGUI sg;
	private SimpleDateFormat sdf;
	private ArrayList<ClientThread> al;
	private boolean keepGoing;

	public Server(int port) {

		this(port, null);
	}

	public Server(int port, ServerGUI sg) {
		// TODO Auto-generated constructor stub
		this.sg = sg; // GUI or not
		this.port = port; // the port
		sdf = new SimpleDateFormat("Hh:mm:ss");
	}

	public static void main(String[] args) {
		int portNumber = 1500;
		switch (args.length) {
		case 0:
			break;
		default:
			System.out.println("Usage is: > java Server [portNumber]");
			return;

		case 1:
			try {
				portNumber = Integer.parseInt(args[0]);

			} catch (Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: > java Server [portNumber]");
				return;
			}

		}
		Server srv = new Server(portNumber);
		srv.start();
	}

	public void start() {

		keepGoing = true;

		// Create socket server and wait for connection requests
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

			}

			catch (Exception e) {
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

	/*
	 * For the GUI to stop the server
	 */
	protected void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		} catch (Exception e) {
			// nothing I can really do
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

	@SuppressWarnings("unused")
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
		// display message on console or GUI
		if (sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf); // append in the room window

		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for (int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// try to write to the Client if it fails remove it from the list
			if (!ct.writeMsg(messageLf)) {
				al.remove(i);
				display("Disconnected Client " + ct.userName
						+ " removed from list.");
			}
		}
	}

	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for (int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if (ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}

}
