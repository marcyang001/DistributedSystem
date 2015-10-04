package server.ws;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;
import java.util.Vector;

import server.ResourceManagerImpl;

public class ServerThread implements Runnable {



    private ServerSocket serverSocket;
    ResourceManager proxy = new ResourceManagerImpl();
    public ServerThread(int port) throws IOException
    {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run()
    {
        while(true)
        {
            try
            {
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("server connected to "
                        + server.getRemoteSocketAddress());

                ServerResponseThread serverResponse = new ServerResponseThread(server, proxy);
                Thread t = new Thread(serverResponse);
                t.start();

            }catch(SocketTimeoutException s)
            {
                System.out.println("Socket timed out!");
                break;
            }catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }
}

class ServerResponseThread implements Runnable {

    Socket m_server;
    ResourceManager m_proxy;
    Vector arguments = new Vector();

    public ServerResponseThread(Socket server, ResourceManager proxy) {
        this.m_server = server;
        this.m_proxy = proxy;

    }


    @Override
    public void run() {

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

        String command = "";



        //server receives commands from the middleware
        DataInputStream in;
        DataOutputStream out = null;

        try {
            in = new DataInputStream(m_server.getInputStream());
            command = in.readUTF();
            System.out.println("The entered command is " + command);

        } catch (IOException e) {
            e.printStackTrace();
        }


        //remove heading and trailing white space
        command = command.trim();
        arguments = parse(command);

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
                    out = new DataOutputStream(m_server.getOutputStream());
                    if (this.m_proxy.addFlight(id, flightNumber, numSeats, flightPrice)) {
                        System.out.println("Flight added");

                        out.writeUTF("Flight added");
                    }
                    else {
                        System.out.println("Flight could not be added");
                        out.writeUTF("Flight could not be added");
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
                    out = new DataOutputStream(m_server.getOutputStream());
                    if (this.m_proxy.addCars(id, location, numCars, price)) {
                        System.out.println("cars added");
                        out.writeUTF("cars added");
                    }
                    else {
                        System.out.println("cars could not be added");
                        out.writeUTF("cars could not be added");
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
                    out = new DataOutputStream(m_server.getOutputStream());
                    if (this.m_proxy.addRooms(id, location, numRooms, price)) {
                        System.out.println("rooms added");
                        out.writeUTF("rooms added");
                    }
                    else {
                        System.out.println("rooms could not be added");
                        out.writeUTF("rooms could not be added");
                    }
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;
/*
            case 5:  //new Customer
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new Customer using id: " + arguments.elementAt(1));
                try {
                    id = getInt(arguments.elementAt(1));
                    int customer = proxy.newCustomer(id);
                    System.out.println("new customer id: " + customer);
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;
*/


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
                    out = new DataOutputStream(m_server.getOutputStream());
                    if (m_proxy.deleteFlight(id, flightNumber)) {
                        System.out.println("Flight Deleted");
                        out.writeUTF("Flight Deleted");
                    }
                    else {
                        System.out.println("Flight could not be deleted");
                        out.writeUTF("Flight could not be deleted");
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
                    out = new DataOutputStream(m_server.getOutputStream());
                    if (m_proxy.deleteCars(id, location)) {
                        System.out.println("cars Deleted");
                        out.writeUTF("cars Deleted");
                    }
                    else {
                        System.out.println("cars could not be deleted");
                        out.writeUTF("cars could not be deleted");
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
                    out = new DataOutputStream(m_server.getOutputStream());
                    if (m_proxy.deleteRooms(id, location)) {
                        System.out.println("rooms Deleted");
                        out.writeUTF("rooms Deleted");
                    }
                    else {
                        System.out.println("rooms could not be deleted");
                        out.writeUTF("rooms could not be deleted");
                    }
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;
/*
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

                    if (proxy.deleteCustomer(id, customer))
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
*/


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
                    out = new DataOutputStream(m_server.getOutputStream());
                    int seats = m_proxy.queryFlight(id, flightNumber);
                    System.out.println("Number of seats available: " + seats);
                    out.writeByte(seats);
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
                    out = new DataOutputStream(m_server.getOutputStream());
                    numCars = this.m_proxy.queryCars(id, location);
                    System.out.println("number of cars at this location: " + numCars);
                    out.writeByte(numCars);
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
                    out = new DataOutputStream(m_server.getOutputStream());
                    numRooms = m_proxy.queryRooms(id, location);
                    System.out.println("number of rooms at this location: " + numRooms);
                    out.writeByte(numRooms);
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;
/*
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

                    String bill = proxy.queryCustomerInfo(id, customer);
                    System.out.println("Customer info: " + bill);
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;
*/

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
                    out = new DataOutputStream(m_server.getOutputStream());
                    price = m_proxy.queryFlightPrice(id, flightNumber);
                    System.out.println("Price of a seat: " + price);
                    out.writeByte(price);
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
                    out = new DataOutputStream(m_server.getOutputStream());
                    price = m_proxy.queryCarsPrice(id, location);
                    System.out.println("Price of a car at this location: " + price);
                    out.writeByte(price);
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
                    out = new DataOutputStream(m_server.getOutputStream());
                    price = m_proxy.queryRoomsPrice(id, location);
                    System.out.println("Price of rooms at this location: " + price);
                    out.writeByte(price);
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;
/*
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

                    if (proxy.reserveFlight(id, customer, flightNumber))
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

                    if (proxy.reserveCar(id, customer, location))
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

                    if (proxy.reserveRoom(id, customer, location))
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
                    for (int i = 0; i < arguments.size()-6; i++)
                        flightNumbers.addElement(arguments.elementAt(3 + i));
                    location = getString(arguments.elementAt(arguments.size()-3));
                    car = getBoolean(arguments.elementAt(arguments.size()-2));
                    room = getBoolean(arguments.elementAt(arguments.size()-1));

                    if (proxy.reserveItinerary(id, customer, flightNumbers,
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
*/

            case 21:  //quit the client
                if (arguments.size() != 1) {
                    wrongNumber();
                    break;
                }
                System.out.println("Quitting client.");
                return;

            /*
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

                    boolean c = proxy.newCustomerId(id, customer);
                    System.out.println("new customer id: " + customer);
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            */


            default:
                System.out.println("The interface does not support this command.");
                try {
                    out.writeUTF("Server does not support this command");
                } catch (IOException e) {
                    System.out.println("FAIL to write back to the server");
                }
                break;
        }






        /*

        synchronized (this) {
            m_proxy.addFlight(1, 1, 1, 1);
        }


        */




    }//end of run method

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
}
