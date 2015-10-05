package main;

import middleware.ResourceManagerImpl;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("Starting the middleware thread");

        if (args.length != 8) {
            System.out.println(
                "Usage: java Main <service-name> <service-port> <deploy-dir>");
            System.exit(-1);
        }

        String serviceHost = args[0];
        int servicePort = Integer.parseInt(args[1]);

        String flighthost = args[2];
        int flightport = Integer.parseInt(args[3]);

        String carhost = args[4];
        int carport = Integer.parseInt(args[5]);

        String roomhost = args[6];
        int roomport = Integer.parseInt(args[7]);

        ResourceManagerImpl middleware = new ResourceManagerImpl(servicePort, flighthost, flightport, carhost, carport, roomhost, roomport);
        Thread t = new Thread (middleware);
        t.start();





    









    }



    
}
