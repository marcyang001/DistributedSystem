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

    //constructor that creates proxies to each server
    public ResourceManagerImpl() {


    }



    @Override
    public boolean addFlight(int id, int flightNumber, int numSeats, int flightPrice) {



        System.out.println("Hello World!!!");

        try {
            flightProxy = new WSClient("flight", "localhost", 8080);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        return true;
    }

    @Override
    public boolean deleteFlight(int id, int flightNumber) {
        return false;
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
        return false;
    }

    @Override
    public boolean deleteCars(int id, String location) {
        return false;
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
