package hw5.hangman;


import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;


public class ClientSocket implements Runnable {

    private Socket clientSocket;
    private String serverAddress;
    int serverPortInt;
    private BufferedInputStream in = null;
    private BufferedOutputStream out = null;

    private static String msg;
    private static String msg2;
    private final BlockingQueue queue;

    private String ReceivedDataFromServer;
    private String ReceivedDataFromMain;

    // CONSTRUCTOR
    public ClientSocket(String serverAddress, int serverPortInput, BlockingQueue queue) {
        this.serverAddress = serverAddress;
        this.serverPortInt = serverPortInput;
        this.queue = queue;
    }

    // This will run automatically when you start the thread
    public void run() {
        // Initialize socket connection
        String connResult = InitializeConnection();
        Sleep(100);
        //SendToMain(connResult);
        //Sleep(500); // Sleep to give MainClient time to take the data

        // First time receive message from server
        String msga = null;
        msga = ReceiveFromServer();

        // Send word and attempt to main, unsplitted
        SendToMain(msga);
        Sleep(500); // Sleep to give MainClient time to take the data
        // While playing game
        String update;
        while (true) {
            update = ReceiveFromMain();
            SendToServer(update);
            if (update == "ENDGAME")
                break; // Break if receive EKZIT from Main
            Sleep(100);

            update = ReceiveFromServer();
            SendToMain(update);
            //SendToMain("UPDATED#9#9");
            Sleep(100);
        }

        // Program will reach here if it reach end
        CloseAll();
    }

    // FUNCTIONS
    private String InitializeConnection() {
        String output = "CONNFAIL";
        clientSocket = null;
        System.err.println("INSIDE INITIALIZE CONNECTION");
        // Initialize socket to server
        try {
            System.err.println("A1A " + serverAddress +" "+serverPortInt);
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(serverAddress, serverPortInt), 2000);
            System.err.println("A1");
            try {
                in = new BufferedInputStream(clientSocket.getInputStream());
                out = new BufferedOutputStream(clientSocket.getOutputStream());
                output = "CONNSUCCESS";
                System.err.println("B1");
            } catch (IOException e) {
                System.err.println("B2");
                System.err.println(e.toString());
                //ConnectionError();
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverAddress + ".");
            System.err.println("A2");
            System.err.println(e.toString());
            //ConnectionError();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + serverAddress + "");
            System.err.println("A3");
            System.err.println(e.toString());
            //ConnectionError();
        }

        // Open IO stream

        return output;
    }

    // Convert string message to bytestream and send it
    private void SendToServer(String rawMsg) {
        try {
            byte[] toServer = rawMsg.getBytes();
            out.write(toServer, 0, toServer.length);
            out.flush();
        } catch (IOException e) {
            System.err.println(e.toString());
            //ConnectionError();
        }
    }

    // Receive bytestream from server, convert to string, return it
    private String ReceiveFromServer() {
        String fromServerString = "";

        try {
            byte[] fromServer = new byte[40];
            int n = in.read(fromServer, 0, fromServer.length);
            fromServerString = new String(fromServer);
        } catch (IOException e) {
            System.err.println(e.toString());
           // ConnectionError();
        }
        return fromServerString;
    }

    // Send data to main thread using LinkedBlockingQueue
    private void SendToMain(String msg) {
        try {

            queue.put(msg);
        } catch (InterruptedException e) {
            System.err.println(e.toString());
           // ConnectionError();
        }
    }

    // Receive data from main thread using LinkedBlockingQueue
    private String ReceiveFromMain() {
        try {
            msg = (String) queue.take();
        } catch (InterruptedException e) {
            System.err.println(e.toString());
            //ConnectionError();
        }
        return msg;
    }

    // To close all kind of connection with server
    private void CloseAll() {
        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    private void Sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


/*
    private void ConnectionError(){
        AlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog;
        alertDialogBuilder.setMessage("Connection error. Check your connection and make sure you enter the correct IP and Port.");

        alertDialogBuilder.setPositiveButton("Try again");
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }*/
}


