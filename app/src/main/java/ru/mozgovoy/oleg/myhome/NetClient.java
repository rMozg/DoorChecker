package ru.mozgovoy.oleg.myhome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetClient {

    /**
     * Maximum size of buffer
     */
    public static final int BUFFER_SIZE = 2048;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private String host = null;
    private int port = 7999;

    public NetClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void connectWithServer() {
        try {
            if (socket == null) {
                socket = new Socket(this.host, this.port);
                out = new PrintWriter(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disConnectWithServer() {
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendDataWithString(String message) {
        if (message != null) {
            connectWithServer();
            out.write(message);
            out.flush();
        }
    }

    public String receiveDataFromServer() {
        try {
            String message = "";
            int charsRead = 0;
            char[] buffer = new char[BUFFER_SIZE];

            try {
                Thread.sleep(100);
            } catch (InterruptedException exc) {
            }

            while (in.ready()) {
                message += in.readLine() + "\n";
            }

//            while ((charsRead = in.read(buffer)) != -1) {
//                message += new String(buffer).substring(0, charsRead);
//            }

//            disConnectWithServer(); // disconnect server
            return message.trim();
        } catch (IOException e) {
            return "Error receiving response:  " + e.getMessage();
        }
    }


}