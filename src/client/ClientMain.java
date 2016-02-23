package client;


import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

public class ClientMain {

    public ClientMain(String connect_host, int connected_port) {

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
            ClientMain mainClient = new ClientMain(serviceHost, servicePort);


            mainClient.run(serviceHost, servicePort);





        } catch(Exception e) {
            System.out.println("FAIL to connect the server");
        }
    }

    public void run(String serviceHost, int servicePort) throws IOException {



        String command = "";
        Vector arguments = new Vector();

        BufferedReader stdin =
                new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Client Interface");
        System.out.println("Type \"help\" for list of supported commands");

        outerloop:
        while (true) {

            while (true) {
                try {
                    System.out.println("Connecting to " + serviceHost + " on port " + servicePort);
                    Socket client = new Socket(serviceHost, servicePort);
                    System.out.println("Just connected to " + client.getRemoteSocketAddress());
                    System.out.println("Please enter the command");

                    //send messages to the middleware
                    DataOutputStream out = new DataOutputStream(client.getOutputStream());

                    //receive confirmation messages from middleware
                    DataInputStream inFromMiddleware = new DataInputStream(client.getInputStream());
                    command = stdin.readLine();
                    command = command.trim();
                    //to avoid crash caused by empty string input
                    if (command.isEmpty()) {
                        command = "wrongcommand";
                    }
                    arguments = parse(command);

                    int value = findChoice((String) arguments.elementAt(0));

                    if (value == 1) {
                        if (arguments.size() == 1)   //command was "help"
                            listCommands();
                        else if (arguments.size() == 2)  //command was "help <commandname>"
                            listSpecific((String) arguments.elementAt(1));
                        else  //wrong use of help command
                            System.out.println("Improper use of help command. Type help or help, <commandname>");
                    }
                    else if (value == 2) {
                        //new flight
                        String resp = "";
                        if (arguments.size() != 5) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);

                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }else {
                            System.out.println("SENDING a new Flight using id: " + arguments.elementAt(1));
                            System.out.println("Flight number: " + arguments.elementAt(2));
                            System.out.println("Add Flight Seats: " + arguments.elementAt(3));
                            System.out.println("Set Flight Price: " + arguments.elementAt(4));
                            out.writeUTF(command);

                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                    }
                    else if (value == 3) {
                        //new car
                        String resp = "";
                        if (arguments.size() != 5) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);


                        }else {
                            System.out.println("SENDING a new car using id: " + arguments.elementAt(1));
                            System.out.println("car Location: " + arguments.elementAt(2));
                            System.out.println("Add Number of cars: " + arguments.elementAt(3));
                            System.out.println("Set Price: " + arguments.elementAt(4));
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                    }
                    else if (value == 4) {
                        //new room
                        String resp = "";
                        if (arguments.size() != 5) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }else {
                            System.out.println("SENDING a new room using id: " + arguments.elementAt(1));
                            System.out.println("room Location: " + arguments.elementAt(2));
                            System.out.println("Add Number of rooms: " + arguments.elementAt(3));
                            System.out.println("Set Price: " + arguments.elementAt(4));

                            //client sends the newroom,1,1,1,1 command to the middleware
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);
                        }

                    }
                    else if (value == 5) {
                        //new Customer
                        if (arguments.size() != 2) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);


                        }else {
                            System.out.println("SENDING a new Customer using id: " + arguments.elementAt(1));
                            try {
                                //client sends the newcustomer,1 command to the middleware
                                out.writeUTF(command);
                                int customerID = inFromMiddleware.readByte();
                                System.out.println("MESSAGE: new customer id: " + customerID);

                            } catch (Exception e) {
                                System.out.println("EXCEPTION: ");
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (value == 6) {
                        //delete Flight
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);


                        }else {
                            System.out.println("SEND Deleting a flight using id: " + arguments.elementAt(1));
                            System.out.println("Flight Number: " + arguments.elementAt(2));
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                    }
                    else if (value == 7) {
                        //delete car
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);


                        }else {
                            System.out.println("Deleting the cars from a particular location  using id: " + arguments.elementAt(1));
                            System.out.println("car Location: " + arguments.elementAt(2));

                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);
                        }
                    }
                    else if (value == 8) {
                        //delete room
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }else {
                            System.out.println("Deleting all rooms from a particular location  using id: " + arguments.elementAt(1));
                            System.out.println("room Location: " + arguments.elementAt(2));
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                    }
                    else if (value == 9) {
                        //delete Customer
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            //resp = inFromMiddleware.readUTF();
                            //System.out.println(resp);


                        }else {
                            System.out.println("Deleting a customer from the database using id: " + arguments.elementAt(1));
                            System.out.println("Customer id: " + arguments.elementAt(2));
                            try {
                                //send the deletecustomer,1
                                out.writeUTF(command);
                                boolean respStatus = inFromMiddleware.readBoolean();
                                if (respStatus)
                                    System.out.println("MESSAGE: Customer deleted");
                                else
                                    System.out.println("MESSAGE: Customer not deleted");

                            } catch (Exception e) {
                                System.out.println("EXCEPTION: ");
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (value == 10) {
                        //querying a flight
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }else {
                            System.out.println("Querying a flight using id: " + arguments.elementAt(1));
                            System.out.println("Flight number: " + arguments.elementAt(2));
                            try {

                                //send the queryflight,1
                                out.writeUTF(command);

                                resp = inFromMiddleware.readUTF();
                                System.out.println(resp);

                            } catch (Exception e) {
                                System.out.println("EXCEPTION: ");
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (value == 11) {
                        //querying a car Location
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                        System.out.println("Querying a car location using id: " + arguments.elementAt(1));
                        System.out.println("car location: " + arguments.elementAt(2));

                        //send query car location
                        out.writeUTF(command);

                        resp = inFromMiddleware.readUTF();
                        System.out.println(resp);

                    }
                    else if (value == 12) {
                        //querying a room location
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                        System.out.println("Querying a room location using id: " + arguments.elementAt(1));
                        System.out.println("room location: " + arguments.elementAt(2));

                        //send a querying a room request
                        out.writeUTF(command);
                        resp = inFromMiddleware.readUTF();
                        System.out.println(resp);

                    }
                    else if (value == 13) {

                        //querying Customer Information
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            break;
                        }
                        System.out.println("Querying Customer information using id: " + arguments.elementAt(1));
                        System.out.println("Customer id: " + arguments.elementAt(2));
                        try {
                            //send a querying customer
                            out.writeUTF(command);

                            String resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                        catch(Exception e) {
                            System.out.println("EXCEPTION: ");
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }

                    }
                    else if (value == 14) {
                        //querying a flight Price
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                        System.out.println("Querying a flight Price using id: " + arguments.elementAt(1));
                        System.out.println("Flight number: " + arguments.elementAt(2));
                        try {
                            //send a querying flight price
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                        catch(Exception e) {
                            System.out.println("EXCEPTION: ");
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else if (value == 15) {
                        //querying a car Price
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                        System.out.println("Querying a car price using id: " + arguments.elementAt(1));
                        System.out.println("car location: " + arguments.elementAt(2));
                        try {

                            //send querying a car price
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);
                        }
                        catch(Exception e) {
                            System.out.println("EXCEPTION: ");
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else if (value == 16) {
                        //querying a room price
                        String resp = "";
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                        System.out.println("Querying a room price using id: " + arguments.elementAt(1));
                        System.out.println("room Location: " + arguments.elementAt(2));
                        try {
                            //send querying room price
                            out.writeUTF(command);
                            resp = inFromMiddleware.readUTF();
                            System.out.println(resp);
                        }
                        catch(Exception e) {
                            System.out.println("EXCEPTION: ");
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else if (value == 17) {
                        //reserve a flight
                        if (arguments.size() != 4) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            break;
                        }
                        System.out.println("Reserving a seat on a flight using id: " + arguments.elementAt(1));
                        System.out.println("Customer id: " + arguments.elementAt(2));
                        System.out.println("Flight number: " + arguments.elementAt(3));
                        try {
                            //send reserve flight command
                            out.writeUTF(command);
                            boolean resp = inFromMiddleware.readBoolean();
                            if (resp) {
                                System.out.println("MESSAGE: Flight reserved");
                            }
                            else {
                                System.out.println("MESSAGE: Flight could not be reserved");
                            }
                        }
                        catch(Exception e) {
                            System.out.println("EXCEPTION: ");
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else if (value == 18) {
                        //reserve a car
                        if (arguments.size() != 4) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            break;
                        }
                        System.out.println("Reserving a car at a location using id: " + arguments.elementAt(1));
                        System.out.println("Customer id: " + arguments.elementAt(2));
                        System.out.println("Location: " + arguments.elementAt(3));
                        try {
                            //send reserve a car request
                            out.writeUTF(command);
                            String resp = inFromMiddleware.readUTF();
                            System.out.println(resp);
                        }
                        catch(Exception e) {
                            System.out.println("EXCEPTION: ");
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else if (value == 19) {
                        //reserve a room
                        if (arguments.size() != 4) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            break;
                        }
                        System.out.println("Reserving a room at a location using id: " + arguments.elementAt(1));
                        System.out.println("Customer id: " + arguments.elementAt(2));
                        System.out.println("Location: " + arguments.elementAt(3));
                        try {
                            //send reserver a room request
                            out.writeUTF(command);
                            String resp = inFromMiddleware.readUTF();
                            System.out.println(resp);
                        }
                        catch(Exception e) {
                            System.out.println("EXCEPTION: ");
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else if (value == 20) {
                        //reserve an Itinerary
                        if (arguments.size()<7) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            break;
                        }

                        try {
                            //send itinerary command
                            out.writeUTF(command);

                            //receive confirmation
                            inFromMiddleware.readUTF();
                        }
                        catch(Exception e) {
                            System.out.println("EXCEPTION: ");
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else if (value == 21) {
                        System.out.println("Terminating the client");
                        client.close();
                        break outerloop;

                    }
                    else if (value == 22) {
                        //new Customer given id
                        if (arguments.size() != 3) {
                            wrongNumber();
                            command = "wrongcommand";
                            out.writeUTF(command);
                            break;
                        }
                        System.out.println("Adding a new Customer using id: "
                                + arguments.elementAt(1)  +  " and cid "  + arguments.elementAt(2));
                        try {
                            //send new customer given id
                            out.writeUTF(command);

                            //receive message from the middleware
                            String resp = inFromMiddleware.readUTF();
                            System.out.println(resp);

                        }
                        catch(Exception e) {
                            System.out.println("EXCEPTION: ");
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else {
                        System.out.println("MESSAGE: The interface does not support this command.");
                        command = "wrongcommand";
                        out.writeUTF(command);
                    }

                    //client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }// end of run

    public void sendToMiddleware(Socket client, String command) {

        try {

            OutputStream outToServer =  client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF(command);
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println("Server says " + in.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }



    }


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

    public void listCommands() {
        System.out.println("\nWelcome to the client interface provided to test your project.");
        System.out.println("Commands accepted by the interface are: ");
        System.out.println("help");
        System.out.println("newflight\nnewcar\nnewroom\nnewcustomer\nnewcustomerid\ndeleteflight\ndeletecar\ndeleteroom");
        System.out.println("deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer");
        System.out.println("queryflightprice\nquerycarprice\nqueryroomprice");
        System.out.println("reserveflight\nreservecar\nreserveroom\nitinerary");
        System.out.println("quit");
        System.out.println("\ntype help, <commandname> for detailed info (note the use of comma).");
    }


    public void listSpecific(String command) {
        System.out.print("Help on: ");
        switch(findChoice(command)) {
            case 1:
                System.out.println("Help");
                System.out.println("\nTyping help on the prompt gives a list of all the commands available.");
                System.out.println("Typing help, <commandname> gives details on how to use the particular command.");
                break;

            case 2:  //new flight
                System.out.println("Adding a new Flight.");
                System.out.println("Purpose: ");
                System.out.println("\tAdd information about a new flight.");
                System.out.println("\nUsage: ");
                System.out.println("\tnewflight, <id>, <flightnumber>, <numSeats>, <flightprice>");
                break;

            case 3:  //new car
                System.out.println("Adding a new car.");
                System.out.println("Purpose: ");
                System.out.println("\tAdd information about a new car location.");
                System.out.println("\nUsage: ");
                System.out.println("\tnewcar, <id>, <location>, <numberofcars>, <pricepercar>");
                break;

            case 4:  //new room
                System.out.println("Adding a new room.");
                System.out.println("Purpose: ");
                System.out.println("\tAdd information about a new room location.");
                System.out.println("\nUsage: ");
                System.out.println("\tnewroom, <id>, <location>, <numberofrooms>, <priceperroom>");
                break;

            case 5:  //new Customer
                System.out.println("Adding a new Customer.");
                System.out.println("Purpose: ");
                System.out.println("\tGet the system to provide a new customer id. (same as adding a new customer)");
                System.out.println("\nUsage: ");
                System.out.println("\tnewcustomer, <id>");
                break;


            case 6: //delete Flight
                System.out.println("Deleting a flight");
                System.out.println("Purpose: ");
                System.out.println("\tDelete a flight's information.");
                System.out.println("\nUsage: ");
                System.out.println("\tdeleteflight, <id>, <flightnumber>");
                break;

            case 7: //delete car
                System.out.println("Deleting a car");
                System.out.println("Purpose: ");
                System.out.println("\tDelete all cars from a location.");
                System.out.println("\nUsage: ");
                System.out.println("\tdeletecar, <id>, <location>, <numCars>");
                break;

            case 8: //delete room
                System.out.println("Deleting a room");
                System.out.println("\nPurpose: ");
                System.out.println("\tDelete all rooms from a location.");
                System.out.println("Usage: ");
                System.out.println("\tdeleteroom, <id>, <location>, <numRooms>");
                break;

            case 9: //delete Customer
                System.out.println("Deleting a Customer");
                System.out.println("Purpose: ");
                System.out.println("\tRemove a customer from the database.");
                System.out.println("\nUsage: ");
                System.out.println("\tdeletecustomer, <id>, <customerid>");
                break;

            case 10: //querying a flight
                System.out.println("Querying flight.");
                System.out.println("Purpose: ");
                System.out.println("\tObtain Seat information about a certain flight.");
                System.out.println("\nUsage: ");
                System.out.println("\tqueryflight, <id>, <flightnumber>");
                break;

            case 11: //querying a car Location
                System.out.println("Querying a car location.");
                System.out.println("Purpose: ");
                System.out.println("\tObtain number of cars at a certain car location.");
                System.out.println("\nUsage: ");
                System.out.println("\tquerycar, <id>, <location>");
                break;

            case 12: //querying a room location
                System.out.println("Querying a room Location.");
                System.out.println("Purpose: ");
                System.out.println("\tObtain number of rooms at a certain room location.");
                System.out.println("\nUsage: ");
                System.out.println("\tqueryroom, <id>, <location>");
                break;

            case 13: //querying Customer Information
                System.out.println("Querying Customer Information.");
                System.out.println("Purpose: ");
                System.out.println("\tObtain information about a customer.");
                System.out.println("\nUsage: ");
                System.out.println("\tquerycustomer, <id>, <customerid>");
                break;

            case 14: //querying a flight for price
                System.out.println("Querying flight.");
                System.out.println("Purpose: ");
                System.out.println("\tObtain price information about a certain flight.");
                System.out.println("\nUsage: ");
                System.out.println("\tqueryflightprice, <id>, <flightnumber>");
                break;

            case 15: //querying a car Location for price
                System.out.println("Querying a car location.");
                System.out.println("Purpose: ");
                System.out.println("\tObtain price information about a certain car location.");
                System.out.println("\nUsage: ");
                System.out.println("\tquerycarprice, <id>, <location>");
                break;

            case 16: //querying a room location for price
                System.out.println("Querying a room Location.");
                System.out.println("Purpose: ");
                System.out.println("\tObtain price information about a certain room location.");
                System.out.println("\nUsage: ");
                System.out.println("\tqueryroomprice, <id>, <location>");
                break;

            case 17:  //reserve a flight
                System.out.println("Reserving a flight.");
                System.out.println("Purpose: ");
                System.out.println("\tReserve a flight for a customer.");
                System.out.println("\nUsage: ");
                System.out.println("\treserveflight, <id>, <customerid>, <flightnumber>");
                break;

            case 18:  //reserve a car
                System.out.println("Reserving a car.");
                System.out.println("Purpose: ");
                System.out.println("\tReserve a given number of cars for a customer at a particular location.");
                System.out.println("\nUsage: ");
                System.out.println("\treservecar, <id>, <customerid>, <location>, <nummberofcars>");
                break;

            case 19:  //reserve a room
                System.out.println("Reserving a room.");
                System.out.println("Purpose: ");
                System.out.println("\tReserve a given number of rooms for a customer at a particular location.");
                System.out.println("\nUsage: ");
                System.out.println("\treserveroom, <id>, <customerid>, <location>, <nummberofrooms>");
                break;

            case 20:  //reserve an Itinerary
                System.out.println("Reserving an Itinerary.");
                System.out.println("Purpose: ");
                System.out.println("\tBook one or more flights.Also book zero or more cars/rooms at a location.");
                System.out.println("\nUsage: ");
                System.out.println("\titinerary, <id>, <customerid>, "
                        + "<flightnumber1>....<flightnumberN>, "
                        + "<LocationToBookcarsOrrooms>, <NumberOfcars>, <NumberOfroom>");
                break;


            case 21:  //quit the client
                System.out.println("Quitting client.");
                System.out.println("Purpose: ");
                System.out.println("\tExit the client application.");
                System.out.println("\nUsage: ");
                System.out.println("\tquit");
                break;

            case 22:  //new customer with id
                System.out.println("Create new customer providing an id");
                System.out.println("Purpose: ");
                System.out.println("\tCreates a new customer with the id provided");
                System.out.println("\nUsage: ");
                System.out.println("\tnewcustomerid, <id>, <customerid>");
                break;


            default:
                System.out.println(command);
                System.out.println("The interface does not support this command.");
                break;
        }
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
        System.out.println("MESSAGE: The number of arguments provided in this command are wrong.");
        System.out.println("MESSAGE: Type help, <commandname> to check usage of this command.");
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
