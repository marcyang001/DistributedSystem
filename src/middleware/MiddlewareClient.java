package middleware;

import client.Client;

import java.net.MalformedURLException;

/**
 * Created by marcyang on 2015-10-01.
 */
public class MiddlewareClient extends WSMiddleware{

    public MiddlewareClient(String serviceName, String serviceHost, int servicePort)
            throws MalformedURLException {
        super(serviceName, serviceHost, servicePort);
    }

    public static void main(String[] args) {
        try {

            if (args.length != 3) {
                System.out.println("Usage: MyClient <service-name> "
                        + "<service-host> <service-port>");
                System.exit(-1);
            }

            String serviceName = args[0];
            String serviceHost = args[1];
            int servicePort = Integer.parseInt(args[2]);

            Client client = new Client(serviceName, serviceHost, servicePort);

            client.run();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }



}
