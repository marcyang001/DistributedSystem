package server.ws;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class Main extends Thread {


    public static void main(String[] args) {
        System.out.println("This is the main method for server");

        if (args.length != 3) {
            System.out.println(
                    "Usage: java Main <service-name> <service-port> <deploy-dir>");
            System.exit(-1);
        }
        String serviceName = args[0];
        int port = Integer.parseInt(args[1]);
        String deployDir = args[2];
        try
        {
            ServerThread s = new ServerThread(port);
            Thread t = new Thread(s);
            t.start();
        }catch(IOException e)
        {
            e.printStackTrace();
        }


    }




}





