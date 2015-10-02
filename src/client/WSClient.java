package client;

import java.net.URL;
import java.net.MalformedURLException;
//import server.ws.ResourceManager;


public class WSClient {

    public client.ResourceManagerImplService service;

    public client.ResourceManager proxy;


    public WSClient(String serviceName, String serviceHost, int servicePort) 
    throws MalformedURLException {
    
        URL wsdlLocation = new URL("http", serviceHost, servicePort, 
                "/" + serviceName + "/service?wsdl");
                
        service = new client.ResourceManagerImplService(wsdlLocation);
        
        proxy = service.getResourceManagerImplPort();
    }

}
