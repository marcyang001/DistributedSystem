package middleware;

import client.Client;
import client.WSClient;
import server.ws.ResourceManager;

import javax.jws.WebService;
import java.net.MalformedURLException;
import java.util.Vector;

//this class is implementating webservice interfaces (resourceManger)
@WebService(endpointInterface = "server.ws.ResourceManager")
public class ResourceManagerImpl implements server.ws.ResourceManager {

    WSClient flightProxy;
    WSClient carProxy;
    WSClient roomProxy;

    //flight server properties
    String f_name = "flight";
    String f_host = "localhost";
    int f_port = 8080;


    //car server properties
    String c_name = "car";
    String c_host = "localhost";
    int c_port = 8082;

    //room server properties
    String r_name = "room";
    String r_host = "localhost";
    int r_port = 8084;


    //constructor that creates proxies to each server
    public ResourceManagerImpl() {

        try {
            flightProxy = new WSClient(f_name, f_host, f_port);
            System.out.println("middleware is connected to the flight server: " +f_host + " " +f_port);

        } catch (MalformedURLException e) {
            System.out.println("Connecting to the flight server");
        }

        try {
            carProxy = new WSClient(c_name, c_host, c_port);
        } catch (MalformedURLException e) {
            System.out.println("Connecting to the car server " + c_host + " "+ c_port);
        }
/*
        try {
            roomProxy = new WSClient(r_name, r_host, r_port);
        } catch (MalformedURLException e) {
            System.out.println("Connecting to the room server");
        }
*/



    }



    @Override
    public boolean addFlight(int id, int flightNumber, int numSeats, int flightPrice) {

        boolean flightAdded;

        flightAdded = flightProxy.proxy.addFlight(id, flightNumber, numSeats, flightPrice);
        if (flightAdded) {
            System.out.println("SENT the addFlight command to the flight server:" + f_host + ":" + f_port);
        }
        else {
            System.out.println("FAIL to sent to flight server");
        }
        return flightAdded;
    }

    @Override
    public boolean deleteFlight(int id, int flightNumber) {

        boolean flightDeleted;

        flightDeleted = flightProxy.proxy.deleteFlight(id, flightNumber);

        if (flightDeleted) {
            System.out.println("DELETED flight " + flightNumber);
        }
        else {
            System.out.println("FAIL to delete flight");
        }

        return flightDeleted;
    }

    @Override
    public int queryFlight(int id, int flightNumber) {

        int flightNum = flightProxy.proxy.queryFlight(id, flightNumber);

        System.out.println("QUERY the flight with ID:" + id);

        return flightNum;
    }

    @Override
    public int queryFlightPrice(int id, int flightNumber) {

        int flightPrice = flightProxy.proxy.queryFlightPrice(id, flightNumber);

        System.out.println("QUERY the flight price with ID: " + id);

        return flightPrice;
    }

    @Override
    public boolean addCars(int id, String location, int numCars, int carPrice) {

        boolean carsAdded;
        carsAdded = carProxy.proxy.addCars(id,location, numCars, carPrice);
        if (carsAdded) {
            System.out.println("SENT the addCar command to the car server:" + c_host + ":" + c_port);
        }
        else {
            System.out.println("FAIL to add cars");
        }
        return carsAdded;
    }

    @Override
    public boolean deleteCars(int id, String location) {

        boolean carsDeleted;
        carsDeleted = carProxy.proxy.deleteCars(id, location);

        if(carsDeleted) {
            System.out.println("DELETE cars " + id);
        }
        else {
            System.out.println("FAIL to delete cars ");
        }

        return carsDeleted;
    }

    @Override
    public int queryCars(int id, String location) {

        int carNum = carProxy.proxy.queryCars(id, location);

        System.out.println("QUERY the car with ID: " + id);

        return carNum;
    }

    @Override
    public int queryCarsPrice(int id, String location) {

        int carPrice = carProxy.proxy.queryCarsPrice(id, location);

        System.out.println("QUERY the car price with ID: " + id);


        return carPrice;
    }

    @Override
    public boolean addRooms(int id, String location, int numRooms, int roomPrice) {

        boolean roomsAdded = roomProxy.proxy.addRooms(id, location, numRooms, roomPrice);

        if (roomsAdded) {
            System.out.println("EXECUTE the addRoom command to the room server: "+r_host +":"+r_port);
        }
        else {
            System.out.println("FAIL to add rooms to the room server: "+r_host + ":" +r_port);
        }

        return roomsAdded;
    }

    @Override
    public boolean deleteRooms(int id, String location) {

        boolean roomDeleted = roomProxy.proxy.deleteRooms(id, location);

        if (roomDeleted) {
            System.out.println("EXECUTE the deleteRoom command to the rooom server: "+r_host + ":" +r_port);
        }
        else {
            System.out.println("FAIL to delete rooms");
        }
        return roomDeleted;
    }

    @Override
    public int queryRooms(int id, String location) {

        int roomquery = roomProxy.proxy.queryRooms(id, location);
        System.out.println("QUERY the room with ID: "+ id);

        return roomquery;
    }

    @Override
    public int queryRoomsPrice(int id, String location) {
        return 0;
    }


    /** Do the customer logic in the middleware **/
    @Override
    public int newCustomer(int id) {
        return 0;
    }

    @Override
    public boolean newCustomerId(int id, int customerId) {
        return false;
    }

    @Override
    public boolean deleteCustomer(int id, int customerId) {
        return false;
    }

    @Override
    public String queryCustomerInfo(int id, int customerId) {
        return null;
    }

    /** Do the customer logic in the middleware **/

    /** Each customer needs to:
     * reserve flights
     * reserve cars
     * reserve room
     *
     * Thus, inside these methods, they need to call to their respective servers
     * such as flight server 8080, car server 8082 and room server 8084
     * in order to complete the transactions
     * **/

    @Override
    public boolean reserveFlight(int id, int customerId, int flightNumber) {

        /** call methods from the flight server to execute actions **/


        return false;
    }

    @Override
    public boolean reserveCar(int id, int customerId, String location) {

        /** call methods from the car server to execute actions **/


        return false;
    }

    @Override
    public boolean reserveRoom(int id, int customerId, String location) {

        /** call methods from the room server to execute actions **/

        return false;
    }

    @Override
    public boolean reserveItinerary(int id, int customerId, Vector flightNumbers, String location, boolean car, boolean room) {

        /** call methods from all three servers to execute actions **/

        return false;
    }
}
