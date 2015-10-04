package main;

import middleware.ResourceManagerImpl;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("Starting the middleware thread");

        if (args.length != 3) {
            System.out.println(
                "Usage: java Main <service-name> <service-port> <deploy-dir>");
            System.exit(-1);
        }

        String serviceName = args[0];
        int port = Integer.parseInt(args[1]);
        String deployDir = args[2];

        ResourceManagerImpl middleware = new ResourceManagerImpl(port);
        Thread t = new Thread (middleware);
        t.start();





    









    }



    
}
