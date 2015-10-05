package middleware;



import server.Trace;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;


public class ResourceManagerImpl implements Runnable{

    ServerSocket sServer;

    protected int serverPort;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped    = false;

    protected RMHashtable itemHT = new RMHashtable();

    //constructor that starts the middleware as a server for accepting requests from client
    public ResourceManagerImpl(int port) {
        this.serverPort = port;


    }

    //handles multi client connections
    public void run(){

        openServerSocket();
        middlewareResponseThread middleResponse;

        while(!isStopped()){

            Socket clientSocket = null;
            try {
                System.out.println("Waiting for client on port : "+ this.serverPort);
                clientSocket = this.serverSocket.accept();
                System.out.println("Middleware just connected to: " + clientSocket.getRemoteSocketAddress());

                //invoke the data input/output thread
                middleResponse = new middlewareResponseThread(clientSocket, itemHT);

                Thread inputOutput = new Thread(middleResponse);
                inputOutput.start();


            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }



}

    //inner class for the middleware client thread (thread use for sending commands to server
class middlewareResponseThread implements server.ws.ResourceManager, Runnable{

        String f_host = "localhost";
        int f_port = 8080;

        String c_host = "localhost";
        int c_port = 8082;

        String r_host = "localhost";
        int r_port = 8084;

        //clients[0] for flight server
        //clients[1] for car server
        //clients[2] for room server
        Socket[] clients = new Socket[3];

        //code for Client imported from server
        protected RMHashtable m_itemHT = new RMHashtable();

        Socket m_server;
        DataInputStream inStreamFromClient;

        DataOutputStream confirmToClient;
        Vector arguments = new Vector();


        public middlewareResponseThread(Socket server, RMHashtable RMtable) {

            this.m_server = server;
            this.m_itemHT = RMtable;
        }




        // Basic operations on RMItem //

        // Read a data item.
        private RMItem readData(int id, String key) {
            synchronized(m_itemHT) {
                return (RMItem) m_itemHT.get(key);
            }
        }

        // Write a data item.
        private void writeData(int id, String key, RMItem value) {
            synchronized(m_itemHT) {
                m_itemHT.put(key, value);
            }
        }

        // Remove the item out of storage.
        protected RMItem removeData(int id, String key) {
            synchronized(m_itemHT) {
                return (RMItem) m_itemHT.remove(key);
            }
        }



        @Override
        public void run() {
            //1. receives the commands from the client and sends back a confirmation message to the client
            //2. send the commands to the server
            //3. treat the customer hash table here
            String command = "";
            int id;
            int flightNumber;
            int flightPrice;
            int numSeats;
            boolean room;
            boolean car;
            int price;
            int numRooms;
            int numCars;
            String location;

            try {

                //middleware receives the command from the client
                inStreamFromClient = new DataInputStream(m_server.getInputStream());


                command = inStreamFromClient.readUTF();


            } catch (IOException e) {
                System.out.println("FAIL to receive messages from client");
                command = "wrongcommand";

            }

            command = command.trim();
            arguments = parse(command);
            System.out.println("Client's command: "+ command);
            //decide which of the commands this was

            switch(findChoice((String) arguments.elementAt(0))) {


                case 2:  //new flight
                    if (arguments.size() != 5) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Adding a new Flight using id: " + arguments.elementAt(1));
                    System.out.println("Flight number: " + arguments.elementAt(2));
                    System.out.println("Add Flight Seats: " + arguments.elementAt(3));
                    System.out.println("Set Flight Price: " + arguments.elementAt(4));

                    try {
                        id = getInt(arguments.elementAt(1));
                        flightNumber = getInt(arguments.elementAt(2));
                        numSeats = getInt(arguments.elementAt(3));
                        flightPrice = getInt(arguments.elementAt(4));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        if (addFlight(id, flightNumber, numSeats, flightPrice)) {
                            System.out.println("Flight added");
                            confirmToClient.writeUTF("MESSAGE: Flight added to " + f_host +":" +f_port);

                        }
                        else {
                            System.out.println("Flight could not be added");
                            confirmToClient.writeUTF("MESSAGE: Flight could not be added to" + f_host +":" +f_port);
                        }
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 3:  //new car
                    if (arguments.size() != 5) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Adding a new car using id: " + arguments.elementAt(1));
                    System.out.println("car Location: " + arguments.elementAt(2));
                    System.out.println("Add Number of cars: " + arguments.elementAt(3));
                    System.out.println("Set Price: " + arguments.elementAt(4));
                    try {
                        id = getInt(arguments.elementAt(1));
                        location = getString(arguments.elementAt(2));
                        numCars = getInt(arguments.elementAt(3));
                        price = getInt(arguments.elementAt(4));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        if (addCars(id, location, numCars, price)) {
                            System.out.println("cars added");
                            confirmToClient.writeUTF("MESSAGE: cars added to: " + c_host + ":" +c_port);
                        }
                        else {
                            System.out.println("cars could not be added");
                            confirmToClient.writeUTF("MESSAGE: cars could not be added to: "  + c_host + ":" +c_port);
                        }
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 4:  //new room
                    if (arguments.size() != 5) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Adding a new room using id: " + arguments.elementAt(1));
                    System.out.println("room Location: " + arguments.elementAt(2));
                    System.out.println("Add Number of rooms: " + arguments.elementAt(3));
                    System.out.println("Set Price: " + arguments.elementAt(4));
                    try {
                        id = getInt(arguments.elementAt(1));
                        location = getString(arguments.elementAt(2));
                        numRooms = getInt(arguments.elementAt(3));
                        price = getInt(arguments.elementAt(4));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());

                        if (addRooms(id, location, numRooms, price)) {
                            System.out.println("rooms added");
                            confirmToClient.writeUTF("MESSAGE: rooms added to: " + r_host + ":"+r_port);
                        }
                        else {
                            System.out.println("rooms could not be added");
                            confirmToClient.writeUTF("MESSAGE: rooms could not be added to: " + r_host + ":"+r_port);
                        }
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 5:  //new Customer
                    if (arguments.size() != 2) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Adding a new Customer using id: " + arguments.elementAt(1));
                    try {
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        id = getInt(arguments.elementAt(1));
                        int customer = newCustomer(id);
                        System.out.println("new customer id: " + customer);
                        confirmToClient.writeByte(id);
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 6: //delete Flight
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Deleting a flight using id: " + arguments.elementAt(1));
                    System.out.println("Flight Number: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        flightNumber = getInt(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        if (deleteFlight(id, flightNumber)) {
                            System.out.println("Flight Deleted");
                            confirmToClient.writeUTF("MESSAGE: Flight deleted in: " + f_host + ":" + f_port);
                        }
                        else {
                            System.out.println("Flight could not be deleted");
                            confirmToClient.writeUTF("MESSAGE: Flight could not be deleted in: " + f_host + ":" + f_port);
                        }
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 7: //delete car
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Deleting the cars from a particular location  using id: " + arguments.elementAt(1));
                    System.out.println("car Location: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        location = getString(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        if (deleteCars(id, location)) {
                            System.out.println("cars Deleted");
                            confirmToClient.writeUTF("MESSAGE: cars deleted in: " + c_host + ":" +c_port);
                        }
                        else {
                            System.out.println("cars could not be deleted");
                            confirmToClient.writeUTF("MESSAGE: cars could not be deleted in: " + c_host + ":" +c_port);
                        }
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 8: //delete room
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Deleting all rooms from a particular location  using id: " + arguments.elementAt(1));
                    System.out.println("room Location: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        location = getString(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());

                        if (deleteRooms(id, location)) {
                            System.out.println("rooms Deleted");
                            confirmToClient.writeUTF("MESSAGE: rooms deleted in: " + r_host + ":" + r_port);
                        }
                        else {
                            System.out.println("rooms could not be deleted");
                            confirmToClient.writeUTF("MESSAGE: rooms could not be deleted in: " + r_host + ":" + r_port);
                        }
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 9: //delete Customer
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Deleting a customer from the database using id: " + arguments.elementAt(1));
                    System.out.println("Customer id: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        int customer = getInt(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        boolean deleteStatus = deleteCustomer(id, customer);
                        if (deleteStatus) {
                            System.out.println("Customer Deleted");
                        }
                        else {
                            System.out.println("Customer could not be deleted");
                        }
                        confirmToClient.writeBoolean(deleteStatus);
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 10: //querying a flight
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Querying a flight using id: " + arguments.elementAt(1));
                    System.out.println("Flight number: " + arguments.elementAt(2));
                    try {

                        id = getInt(arguments.elementAt(1));
                        flightNumber = getInt(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        int seats = queryFlight(id, flightNumber);
                        System.out.println("Number of seats available: " + seats);
                        confirmToClient.writeUTF("MESSAGE: Number of seats available: " + seats);

                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 11: //querying a car Location
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Querying a car location using id: " + arguments.elementAt(1));
                    System.out.println("car location: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        location = getString(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        numCars = queryCars(id, location);
                        System.out.println("number of cars at this location: " + numCars);
                        confirmToClient.writeUTF("MESSAGE: number of cars at this location: " + numCars);
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 12: //querying a room location
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Querying a room location using id: " + arguments.elementAt(1));
                    System.out.println("room location: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        location = getString(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        numRooms = queryRooms(id, location);
                        System.out.println("number of rooms at this location: " + numRooms);
                        confirmToClient.writeUTF("MESSAGE:  number of rooms at this location: " + numRooms);
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 13: //querying Customer Information
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Querying Customer information using id: " + arguments.elementAt(1));
                    System.out.println("Customer id: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        int customer = getInt(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        String bill = queryCustomerInfo(id, customer);
                        System.out.println("Customer info: " + bill);
                        confirmToClient.writeUTF("MESSAGE: Customer info: " + bill);
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 14: //querying a flight Price
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Querying a flight Price using id: " + arguments.elementAt(1));
                    System.out.println("Flight number: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        flightNumber = getInt(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        price = queryFlightPrice(id, flightNumber);
                        System.out.println("Price of a seat: " + price);
                        confirmToClient.writeUTF("MESSAGE: Price of a seat: " + price);
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 15: //querying a car Price
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Querying a car price using id: " + arguments.elementAt(1));
                    System.out.println("car location: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        location = getString(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        price = queryCarsPrice(id, location);
                        System.out.println("Price of a car at this location: " + price);
                        confirmToClient.writeUTF("MESSAGE: Price of a car at this location: " + price);
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 16: //querying a room price
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Querying a room price using id: " + arguments.elementAt(1));
                    System.out.println("room Location: " + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        location = getString(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        price = queryRoomsPrice(id, location);
                        System.out.println("Price of rooms at this location: " + price);
                        confirmToClient.writeUTF("MESSAGE: Price of rooms at this location: " + price);
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 17:  //reserve a flight
                    if (arguments.size() != 4) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Reserving a seat on a flight using id: " + arguments.elementAt(1));
                    System.out.println("Customer id: " + arguments.elementAt(2));
                    System.out.println("Flight number: " + arguments.elementAt(3));
                    try {
                        id = getInt(arguments.elementAt(1));
                        int customer = getInt(arguments.elementAt(2));
                        flightNumber = getInt(arguments.elementAt(3));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        boolean reserveStatus = reserveFlight(id, customer, flightNumber);
                        if (reserveStatus) {
                            System.out.println("Flight Reserved");
                        }
                        else {
                            System.out.println("Flight could not be reserved.");
                        }
                        confirmToClient.writeBoolean(reserveStatus);
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 18:  //reserve a car
                    if (arguments.size() != 4) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Reserving a car at a location using id: " + arguments.elementAt(1));
                    System.out.println("Customer id: " + arguments.elementAt(2));
                    System.out.println("Location: " + arguments.elementAt(3));
                    try {
                        id = getInt(arguments.elementAt(1));
                        int customer = getInt(arguments.elementAt(2));
                        location = getString(arguments.elementAt(3));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        if (reserveCar(id, customer, location)) {
                            System.out.println("car Reserved");
                            confirmToClient.writeUTF("MESSAGE: car reserved");
                        }
                        else {
                            System.out.println("car could not be reserved.");
                            confirmToClient.writeUTF("MESSAGE: car could not be reserved");
                        }
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 19:  //reserve a room
                    if (arguments.size() != 4) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Reserving a room at a location using id: " + arguments.elementAt(1));
                    System.out.println("Customer id: " + arguments.elementAt(2));
                    System.out.println("Location: " + arguments.elementAt(3));
                    try {
                        id = getInt(arguments.elementAt(1));
                        int customer = getInt(arguments.elementAt(2));
                        location = getString(arguments.elementAt(3));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        if (reserveRoom(id, customer, location)) {
                            System.out.println("room Reserved");
                            confirmToClient.writeUTF("MESSAGE: room reserved");
                        }
                        else {
                            System.out.println("room could not be reserved.");
                            confirmToClient.writeUTF("MESSAGE: room could not be reserved");
                        }
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 20:  //reserve an Itinerary
                    if (arguments.size()<7) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Reserving an Itinerary using id: " + arguments.elementAt(1));
                    System.out.println("Customer id: " + arguments.elementAt(2));
                    for (int i = 0; i<arguments.size()-6; i++)
                        System.out.println("Flight number" + arguments.elementAt(3 + i));
                    System.out.println("Location for car/room booking: " + arguments.elementAt(arguments.size()-3));
                    System.out.println("car to book?: " + arguments.elementAt(arguments.size()-2));
                    System.out.println("room to book?: " + arguments.elementAt(arguments.size()-1));
                    try {
                        id = getInt(arguments.elementAt(1));
                        int customer = getInt(arguments.elementAt(2));
                        Vector flightNumbers = new Vector();
                        for (int i = 0; i < arguments.size() - 6; i++)
                            flightNumbers.addElement(arguments.elementAt(3 + i));
                        location = getString(arguments.elementAt(arguments.size()-3));
                        car = getBoolean(arguments.elementAt(arguments.size()-2));
                        room = getBoolean(arguments.elementAt(arguments.size()-1));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        if (reserveItinerary(id, customer, flightNumbers,
                                location, car, room)) {
                            System.out.println("Itinerary Reserved");
                            confirmToClient.writeUTF("MESSAGE: Itinerary Reserved.");
                        }
                        else {
                            System.out.println("Itinerary could not be reserved");
                            confirmToClient.writeUTF("MESSAGE: Itinerary could not be reserved");
                        }
                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 21:  //quit the client
                    if (arguments.size() != 1) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Quitting client.");
                    return;

                case 22:  //new Customer given id
                    if (arguments.size() != 3) {
                        wrongNumber();
                        break;
                    }
                    System.out.println("Adding a new Customer using id: "
                            + arguments.elementAt(1)  +  " and cid "  + arguments.elementAt(2));
                    try {
                        id = getInt(arguments.elementAt(1));
                        int customer = getInt(arguments.elementAt(2));
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        boolean c = newCustomerId(id, customer);
                        System.out.println("new customer id: " + customer);


                        confirmToClient.writeUTF("MESSAGE: new customer id: " + customer);


                    }
                    catch(Exception e) {
                        System.out.println("EXCEPTION: ");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                default:
                    System.out.println("The interface does not support this command.");
                    try {
                        confirmToClient = new DataOutputStream(m_server.getOutputStream());
                        confirmToClient.writeUTF("MESSAGE: The middleware interface does not support this command.");
                    } catch (IOException e) {
                       System.out.println("FAIL to write back to the client from the middleware");
                    }
                    break;
            }


        }//end of run

        public int findChoice(String argument) {
            if (argument.compareToIgnoreCase("help") == 0)
                return 1;
            else if (argument.compareToIgnoreCase("newflight") == 0)
                return 2;
            else if (argument.compareToIgnoreCase("newcar") == 0)
                return 3;
            else if (argument.compareToIgnoreCase("newroom") == 0)
                return 4;
            else if (argument.compareToIgnoreCase("newcustomer") == 0)
                return 5;
            else if (argument.compareToIgnoreCase("deleteflight") == 0)
                return 6;
            else if (argument.compareToIgnoreCase("deletecar") == 0)
                return 7;
            else if (argument.compareToIgnoreCase("deleteroom") == 0)
                return 8;
            else if (argument.compareToIgnoreCase("deletecustomer") == 0)
                return 9;
            else if (argument.compareToIgnoreCase("queryflight") == 0)
                return 10;
            else if (argument.compareToIgnoreCase("querycar") == 0)
                return 11;
            else if (argument.compareToIgnoreCase("queryroom") == 0)
                return 12;
            else if (argument.compareToIgnoreCase("querycustomer") == 0)
                return 13;
            else if (argument.compareToIgnoreCase("queryflightprice") == 0)
                return 14;
            else if (argument.compareToIgnoreCase("querycarprice") == 0)
                return 15;
            else if (argument.compareToIgnoreCase("queryroomprice") == 0)
                return 16;
            else if (argument.compareToIgnoreCase("reserveflight") == 0)
                return 17;
            else if (argument.compareToIgnoreCase("reservecar") == 0)
                return 18;
            else if (argument.compareToIgnoreCase("reserveroom") == 0)
                return 19;
            else if (argument.compareToIgnoreCase("itinerary") == 0)
                return 20;
            else if (argument.compareToIgnoreCase("quit") == 0)
                return 21;
            else if (argument.compareToIgnoreCase("newcustomerid") == 0)
                return 22;
            else if (argument.compareToIgnoreCase("wrongcommand") == 0)
                return 23;
            else
                return 666;
        }

        public Vector parse(String command) {
            Vector arguments = new Vector();
            StringTokenizer tokenizer = new StringTokenizer(command, ",");
            String argument = "";
            while (tokenizer.hasMoreTokens()) {
                argument = tokenizer.nextToken();
                argument = argument.trim();
                arguments.add(argument);
            }
            return arguments;
        }

        public void wrongNumber() {
            System.out.println("The number of arguments provided in this command are wrong.");
            System.out.println("Type help, <commandname> to check usage of this command.");
        }

        public int getInt(Object temp) throws Exception {
            try {
                return (new Integer((String)temp)).intValue();
            }
            catch(Exception e) {
                throw e;
            }
        }

        public boolean getBoolean(Object temp) throws Exception {
            try {
                return (new Boolean((String)temp)).booleanValue();
            }
            catch(Exception e) {
                throw e;
            }
        }

        public String getString(Object temp) throws Exception {
            try {
                return (String)temp;
            }
            catch (Exception e) {
                throw e;
            }
        }



        public boolean addFlight(int id, int flightNumber, int numSeats, int flightPrice) {
            //send the command to the flight server

            boolean status = false;
            try {
                clients[0] = new Socket(f_host, f_port);
                System.out.println("middleware connected to the flight server");
                OutputStream outToServer = clients[0].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("newflight,%d,%d,%d,%d",id, flightNumber, numSeats, flightPrice);
                out.writeUTF(request);
                InputStream inFromServer = clients[0].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                String resp = in.readUTF();
                if (resp.compareToIgnoreCase("flight added") == 0) {
                    status = true;
                }
                else {
                    status = false;
                }

                clients[0].close();
            } catch (IOException e) {
                System.out.println("FAIL to connect to flight server");
            }

            return status;
        }

        public boolean deleteFlight(int id, int flightNumber) {

            boolean status = false;

            try {
                clients[0] = new Socket(f_host, f_port);
                System.out.println("middleware connected to the flight server");
                OutputStream outToServer = clients[0].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("deleteflight,%d,%d",id, flightNumber);
                out.writeUTF(request);
                InputStream inFromServer = clients[0].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                String resp = in.readUTF();

                if(resp.compareToIgnoreCase("flight deleted") == 0) {
                    status = true;
                }
                else {
                    status = false;
                }
                clients[0].close();
            } catch (IOException e) {
                System.out.println("FAIL to connect to flight server");
            }


            return status;
        }


        public int queryFlight(int id, int flightNumber) {

            int seats = 0;
            try {
                clients[0] = new Socket(f_host, f_port);
                System.out.println("middleware connected to the flight server");
                OutputStream outToServer = clients[0].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("queryflight,%d,%d",id, flightNumber);
                out.writeUTF(request);
                InputStream inFromServer = clients[0].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);

                //send back the seat number from the server
                int resp = in.readByte();
                seats = resp;

                clients[0].close();
            } catch (IOException e) {
                System.out.println("FAIL to connect to the flight server");
            }

            return seats;
        }


        public int queryFlightPrice(int id, int flightNumber) {
            int flightprice = 0;
            try {
                clients[0] = new Socket(f_host, f_port);
                System.out.println("middleware connected to the flight server");
                OutputStream outToServer = clients[0].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("queryflightprice,%d,%d",id, flightNumber);
                out.writeUTF(request);
                InputStream inFromServer = clients[0].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);

                flightprice = in.readByte();
                clients[0].close();
            } catch (IOException e) {
                System.out.println("FAIL to connect to the flight server");
            }

            return flightprice;
        }


        public boolean addCars(int id, String location, int numCars, int carPrice) {
            boolean status = false;

            try {
                clients[1] = new Socket(c_host, c_port);
                OutputStream outToServer = clients[1].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("newcar,%d,%s,%d,%d",id, location, numCars, carPrice);
                out.writeUTF(request);
                InputStream inFromServer = clients[1].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);

                String resp = in.readUTF();
                if (resp.compareToIgnoreCase("cars added") == 0) {
                    status = true;
                }
                else {
                    status = false;
                }

            } catch (IOException e) {
                System.out.println("FAIL to connect to the car server");
            }

            return status;
        }


        public boolean deleteCars(int id, String location) {
            boolean status = false;

            try {
                clients[1] = new Socket(c_host, c_port);
                OutputStream outToServer = clients[1].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("deletecar,%d,%s",id, location);
                out.writeUTF(request);
                InputStream inFromServer = clients[1].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                String resp = in.readUTF();
                if (resp.compareToIgnoreCase("cars deleted") == 0) {
                    status = true;
                }
                else {
                    status = false;
                }
            } catch (IOException e) {
                System.out.println("FAIL to connect to the car server");
            }



            return status;
        }


        public int queryCars(int id, String location) {

            int carNum = 0;
            try {
                clients[1] = new Socket(c_host, c_port);
                OutputStream outToServer = clients[1].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("querycar,%d,%s",id, location);
                out.writeUTF(request);
                InputStream inFromServer = clients[1].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                carNum = in.readByte();

            } catch (IOException e) {
                System.out.println("FAIL to connect to the car server");
            }

            return carNum;
        }


        public int queryCarsPrice(int id, String location) {
            int carprice = 0;
            try {
                clients[1] = new Socket(c_host, c_port);
                OutputStream outToServer = clients[1].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("querycarprice,%d,%s",id, location);
                out.writeUTF(request);
                InputStream inFromServer = clients[1].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);

                carprice = in.readByte();

                clients[1].close();

            } catch (IOException e) {
                System.out.println("FAIL to connect to the car server");
            }

            return carprice;
        }


        public boolean addRooms(int id, String location, int numRooms, int roomPrice) {

            boolean status = false;
            try {
                clients[2] = new Socket(r_host, r_port);
                OutputStream outToServer = clients[2].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("newroom,%d,%s,%d,%d",id, location, numRooms, roomPrice);
                out.writeUTF(request);
                InputStream inFromServer = clients[2].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);

                String resp = in.readUTF();
                if (resp.compareToIgnoreCase("rooms added") == 0) {
                    status = true;
                }
                else{
                    status = false;
                }
                clients[2].close();

            } catch (IOException e) {
                System.out.println("FAIL to connect to the car server");
            }

            return status;
        }


        public boolean deleteRooms(int id, String location) {

            boolean status = false;
            try {
                clients[2] = new Socket(r_host, r_port);
                OutputStream outToServer = clients[2].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("deleteroom,%d,%s",id, location);
                out.writeUTF(request);
                InputStream inFromServer = clients[2].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);

                String resp = in.readUTF();
                if (resp.compareToIgnoreCase("rooms deleted") == 0) {
                    status = true;
                }
                else {
                    status = false;
                }
                clients[2].close();

            } catch (IOException e) {
                System.out.println("FAIL to connect to the car server");
            }


            return status;
        }


        public int queryRooms(int id, String location) {
            int roomNum = 0;
            try {
                clients[2] = new Socket(r_host, r_port);
                OutputStream outToServer = clients[2].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("queryroom,%d,%s",id, location);
                out.writeUTF(request);
                InputStream inFromServer = clients[2].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);

                roomNum = in.readByte();
                clients[2].close();
            } catch (IOException e) {
                System.out.println("FAIL to connect to the room server");
                roomNum = -1;
            }

            return roomNum;
        }


        public int queryRoomsPrice(int id, String location) {

            int roomprice = 0;
            try {
                clients[2] = new Socket(r_host, r_port);
                OutputStream outToServer = clients[2].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("queryroomprice,%d,%s",id, location);
                out.writeUTF(request);
                InputStream inFromServer = clients[2].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                roomprice = in.readByte();
                clients[2].close();

            } catch (IOException e) {
                System.out.println("FAIL to connect to the room server");
                roomprice = -1;
            }

            return roomprice;
        }


        public int newCustomer(int id) {

            Trace.info("INFO: RM::newCustomer(" + id + ") called.");
            // Generate a globally unique Id for the new customer.
            int customerId = Integer.parseInt(String.valueOf(id) +
                    String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                    String.valueOf(Math.round(Math.random() * 100 + 1)));
            Customer cust = new Customer(customerId);
            writeData(id, cust.getKey(), cust);
            Trace.info("RM::newCustomer(" + id + ") OK: " + customerId);

            return customerId;

        }


        public boolean newCustomerId(int id, int customerId) {

            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerId + ") called.");
            Customer cust = (Customer) readData(id, Customer.getKey(customerId));
            if (cust == null) {
                cust = new Customer(customerId);
                writeData(id, cust.getKey(), cust);
                Trace.info("INFO: RM::newCustomer(" + id + ", " + customerId + ") OK.");
                return true;
            } else {
                Trace.info("INFO: RM::newCustomer(" + id + ", " +
                        customerId + ") failed: customer already exists.");
                return false;
            }

        }


        public boolean deleteCustomer(int id, int customerId) {

            Trace.info("RM::deleteCustomer(" + id + ", " + customerId + ") called.");
            Customer cust = (Customer) readData(id, Customer.getKey(customerId));
            if (cust == null) {
                Trace.warn("RM::deleteCustomer(" + id + ", "
                        + customerId + ") failed: customer doesn't exist.");
                return false;
            } else {
                // Increase the reserved numbers of all reservable items that
                // the customer reserved.
                RMHashtable reservationHT = cust.getReservations();
                for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {
                    String reservedKey = (String) (e.nextElement());
                    ReservedItem reservedItem = cust.getReservedItem(reservedKey);
                    Trace.info("RM::deleteCustomer(" + id + ", " + customerId + "): "
                            + "deleting " + reservedItem.getCount() + " reservations "
                            + "for item " + reservedItem.getKey());
                    int itemId = reservedItem.getId();
                    int count = reservedItem.getCount();

                    Trace.info("RM::deleteCustomer(" + id + ", " + customerId + "): ");
                    //car
                    if(reservedItem.getType() == 1){
                        if(!(updateDeleteCustomer(f_host, f_port, itemId, reservedItem.getKey(), count))){
                            return false;
                            //error
                        }
                    }
                    //room
                    else if (reservedItem.getType() == 2){
                        if(!(updateDeleteCustomer(c_host, c_port, itemId, reservedItem.getKey(), count))){
                            return false;
                            //error
                        }
                    }
                    else if (reservedItem.getType() == 3){
                        if(!(updateDeleteCustomer(r_host, r_port, itemId, reservedItem.getKey(), count))){
                            return false;
                            //error
                        }
                    }

                }
                // Remove the customer from the storage.
                removeData(id, cust.getKey());
                Trace.info("RM::deleteCustomer(" + id + ", " + customerId + ") OK.");
                return true;
            }

        }


        public String queryCustomerInfo(int id, int customerId) {

            Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId + ") called.");
            Customer cust = (Customer) readData(id, Customer.getKey(customerId));
            if (cust == null) {
                Trace.warn("RM::queryCustomerInfo(" + id + ", "
                        + customerId + ") failed: customer doesn't exist.");
                // Returning an empty bill means that the customer doesn't exist.
                return "";
            } else {
                String s = cust.printBill();
                Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId + "): \n");
                System.out.println(s);
                return s;
            }

        }

        // Reserve an item.
        protected boolean reserveItem(int id, int customerId, String location, String key, int itemInfo, int itemID) {
            //get item info

            int count = -1;
            int price = -1;
            switch(itemInfo){
                case 1: //item = flightProxy.proxy.getItemInfo(id,key);
                    count = queryFlight(id, Integer.parseInt(location));
                    price = queryFlightPrice(id, Integer.parseInt(location));
                    break;
                case 2: //item = carProxy.proxy.getItemInfo(id,location);
                    count = queryCars(id, location);
                    price = queryCarsPrice(id, location);
                    break;
                case 3: //item = roomProxy.proxy.getItemInfo(id,key);
                    count = queryRooms(id, location);
                    price = queryRoomsPrice(id, location);
                    break;
            }
        /*info[0] = item.getLocation();
            info[1] = String.valueOf(item.getCount());
            info[2] = item.getKey();
            info[3] = String.valueOf(item.getPrice());
            info[4] = String.valueOf(true);
        */
            if (count == -1){
                Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", "
                        + key  + ") failed: item doesn't exist.");
                return false;
            }
            Trace.info("RM::reserveItem(" + id + ", " + customerId + ", "
                    + key + ", " + location + ") called.");
            // Read customer object if it exists (and read lock it).
            Customer cust = (Customer) readData(id, Customer.getKey(customerId));
            if (cust == null) {
                Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", "
                        + key + ", " + location + ") failed: customer doesn't exist.");
                return false;
            }

            // Check if the item is available.
            //ReservableItem item = (ReservableItem) readData(id, key);
            if (count == -1) {
                Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", "
                        + key + ", " + location + ") failed: item doesn't exist.");
                return false;
            } else if (count == 0) {
                Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", "
                        + key + ", " + location + ") failed: no more items.");
                return false;
            } else {
                // Do reservation
                cust.reserve(key, location, price, itemInfo, itemID); //change location maybe
                writeData(id, cust.getKey(), cust);

                // Decrease the number of available items in the storage.
                boolean update = true;
                switch(itemInfo){
                    case 1:
                        update = updateItemInfo(f_host, f_port, id, key);
                        break;
                    case 2:
                        update = updateItemInfo(c_host, c_port, id, key);
                        break;
                    case 3:
                        update = updateItemInfo(r_host, r_port, id, key);
                        break;
                }
                if (!update){
                    Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", "
                            + key + ", " + location + ") failed: update item info.");
                    return false;
                }

                Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", "
                        + key + ", " + location + ") OK.");
                return true;
            }
        }





        @Override
        public boolean updateItemInfo(String serverhost, int port, int id, String key) {

            boolean status = false;
            //send the command to the appropriate server
            try {
                Socket connectingToServer = new Socket(serverhost, port);
                OutputStream outToServer = connectingToServer.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("updateiteminfo,%d,%s",id, key);
                out.writeUTF(request);
                InputStream inFromServer = connectingToServer.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                String resp = in.readUTF();
                if (resp.compareToIgnoreCase("item updated") == 0) {
                    System.out.println(resp);
                    status = true;
                }
                else {
                    System.out.println(resp);
                    status = false;

                }

            } catch (IOException e) {
                System.out.println("FAIL to connect to the server: " + serverhost + ":" + port);
            }


            return status;

        }

        @Override
        public boolean updateDeleteCustomer(String host, int port, int id, String key, int count) {

            boolean status = false;
            //send the command to the appropriate server
            try {
                Socket connectingToServer = new Socket(host, port);
                OutputStream outToServer = connectingToServer.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("updatedeletecustomer,%d,%s,%d",id, key, count);
                out.writeUTF(request);
                InputStream inFromServer = connectingToServer.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                status = in.readBoolean();
                if (status) {
                    System.out.println("SUCCESSFULLY delete customer related reservation");
                }
                else {
                    System.out.println("FAIL to delete customer related reservation");
                }

            } catch (IOException e) {
                System.out.println("FAIL to connect to the server: " + host + ":" + port);
            }

            return status;

        }


        @Override
        public boolean reserveFlight(int id, int customerId, int flightNumber) {
            /** call methods from the flight server to execute actions **/
            //get flight key
            String key = getFlightKey(flightNumber);
            return reserveItem(id,customerId,String.valueOf(flightNumber),key,1,id);
        }

        @Override
        public boolean reserveCar(int id, int customerId, String location) {
            /** call methods from the car server to execute actions **/

            String key = getCarKey(location);
            return reserveItem(id,customerId,location,key,2,id);
        }

        @Override
        public boolean reserveRoom(int id, int customerId, String location) {
            /** call methods from the room server to execute actions **/

            String key = getRoomKey(location);
            return reserveItem(id, customerId,location, key, 3,id);
        }


        public boolean reserveItinerary(int id, int customerId, Vector flightNumbers, String location, boolean car, boolean room) {

            Iterator it = flightNumbers.iterator();

            while(it.hasNext()){
                if(!(reserveFlight(id,customerId,Integer.parseInt((String)it.next())))){
                    //error
                    return false;
                }
            }
            //there is a car
            if(!car){
                reserveCar(id,customerId,location);
            }
            //there is a room
            else if (!room){
                reserveRoom(id,customerId,location);
            }
            return true;
        }

        @Override
        public String getFlightKey(int flightnumber) {
            String resp = "";
            try {
                clients[0] = new Socket(f_host, f_port);
                OutputStream outToServer = clients[0].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("getflightkey,%d", flightnumber);
                out.writeUTF(request);
                InputStream inFromServer = clients[0].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                resp = in.readUTF();
                System.out.println(resp);

            } catch (UnknownHostException e) {
                System.out.println("FAIL to connect to the flight server");
            } catch (IOException e) {
                System.out.println("FAIL to output to the flight server");
            }
            return resp;
        }

            @Override
        public String getCarKey(String location){
            String resp = "";
                try {
                    clients[1] = new Socket(c_host,c_port);
                    OutputStream outToServer = clients[1].getOutputStream();
                    DataOutputStream out = new DataOutputStream(outToServer);
                    String request = String.format("getcarkey,%s", location);
                    out.writeUTF(request);
                    InputStream inFromServer = clients[1].getInputStream();
                    DataInputStream in = new DataInputStream(inFromServer);
                    resp = in.readUTF();
                    System.out.println(resp);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return resp;
        }
        @Override
        public String getRoomKey(String location){
            String resp = "";
            try {
                clients[2] = new Socket(r_host,r_port);
                OutputStream outToServer = clients[2].getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String request = String.format("getroomkey,%s", location);
                out.writeUTF(request);
                InputStream inFromServer = clients[2].getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                resp = in.readUTF();
                System.out.println(resp);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resp;
        }














    }


