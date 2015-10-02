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
            System.out.println("SENT the addFlight command to the flight server");
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
        return 0;
    }

    @Override
    public int queryFlightPrice(int id, int flightNumber) {
        return 0;
    }

    @Override
    public boolean addCars(int id, String location, int numCars, int carPrice) {

        boolean carsAdded;
        carsAdded = carProxy.proxy.addCars(id,location, numCars, carPrice);
        if (carsAdded) {
            System.out.println("SENT the addCar command to the car server:" + f_host + ":" + f_port);
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
        return 0;
    }

    @Override
    public int queryCarsPrice(int id, String location) {
        return 0;
    }

    @Override
    public boolean addRooms(int id, String location, int numRooms, int roomPrice) {



        return false;
    }

    @Override
    public boolean deleteRooms(int id, String location) {
        return false;
    }

    @Override
    public int queryRooms(int id, String location) {
        return 0;
    }

    @Override
    public int queryRoomsPrice(int id, String location) {
        return 0;
    }

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

    @Override
    public boolean reserveFlight(int id, int customerId, int flightNumber) {
        return false;
    }

    @Override
    public boolean reserveCar(int id, int customerId, String location) {
        return false;
    }

    @Override
    public boolean reserveRoom(int id, int customerId, String location) {
        return false;
    }

    @Override
    public boolean reserveItinerary(int id, int customerId, Vector flightNumbers, String location, boolean car, boolean room) {
        return false;
    }
}
