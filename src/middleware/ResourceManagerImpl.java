package middleware;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;


public class ResourceManagerImpl implements Runnable{

    ServerSocket sServer;

    protected int serverPort;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;

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
                middleResponse = new middlewareResponseThread(clientSocket);

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

        Socket m_server;
        DataInputStream inStreamFromClient;

        DataOutputStream confirmToClient;
        Vector arguments = new Vector();
        public middlewareResponseThread(Socket server) {

            this.m_server = server;
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
                        id = getInt(arguments.elementAt(1));
                        int customer = newCustomer(id);
                        System.out.println("new customer id: " + customer);
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

                        if (deleteCustomer(id, customer))
                            System.out.println("Customer Deleted");
                        else
                            System.out.println("Customer could not be deleted");
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

                        if (reserveFlight(id, customer, flightNumber))
                            System.out.println("Flight Reserved");
                        else
                            System.out.println("Flight could not be reserved.");
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

                        if (reserveCar(id, customer, location))
                            System.out.println("car Reserved");
                        else
                            System.out.println("car could not be reserved.");
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

                        if (reserveRoom(id, customer, location))
                            System.out.println("room Reserved");
                        else
                            System.out.println("room could not be reserved.");
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

                        if (reserveItinerary(id, customer, flightNumbers,
                                location, car, room))
                            System.out.println("Itinerary Reserved");
                        else
                            System.out.println("Itinerary could not be reserved.");
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

                        boolean c = newCustomerId(id, customer);
                        System.out.println("new customer id: " + customer);
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
                System.out.println("FAIL to connect to the car server");
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
                System.out.println("FAIL to connect to the car server");
            }

            return roomprice;
        }


        public int newCustomer(int id) {

            //implemented from part 1


            return 0;
        }


        public boolean newCustomerId(int id, int customerId) {
            return false;
        }


        public boolean deleteCustomer(int id, int customerId) {
            return false;
        }


        public String queryCustomerInfo(int id, int customerId) {
            return null;
        }


        public boolean reserveFlight(int id, int customerId, int flightNumber) {




            return false;
        }


        public boolean reserveCar(int id, int customerId, String location) {




            return false;
        }


        public boolean reserveRoom(int id, int customerId, String location) {



            return false;
        }


        public boolean reserveItinerary(int id, int customerId, Vector flightNumbers, String location, boolean car, boolean room) {
            boolean status = false;


            return status;
        }











    }


