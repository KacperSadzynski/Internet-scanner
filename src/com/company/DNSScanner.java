package com.company;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.concurrent.TimeUnit;


/**
 * DNSScanner class inherits from the IPv4Addresses class and Runnable interface
 * By default, this class is being executed by a thread but for test purposes method testScan can be used as well
 * It builds a DNS packet that is sent by query(), then a thread execute run() method
 * It scans all public IPv4 addresses limited by BEGIN, END variables
 * If the amplification of the sent packet is big enough it print out the IP of this server
 * If the toFile flag is TRUE it writes to DNS_Vulnerable.txt file output as well
 * Instance Variables:
 * static final int DNS_SERVER_PORT - represents DNS server port, set on 53
 * byte[] dnsFrame - byte array that represents DNS packet
 */
public class DNSScanner extends IPv4Addresses implements Runnable {

    private static final int DNS_SERVER_PORT = 53;
    byte[] dnsFrame;

    /**
     * Constructor
     * It removes file DNS_Vulnerable.txt if exists to avoid appending new output to the old one
     * Builds dnsFrame byte array using the buildPacket() method
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     * @throws IOException
     * @see IOException
     */
    public DNSScanner(int begin, int end) throws IOException {
        File file = new File("DNS_Vulnerable.txt");
        if(file.exists()){
           file.delete();
        }
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
    }

    /**
     * UNUSED
     * Scans IPv4 addresses pool limited by BEGIN, END variables
     * Used when DNSScanner class was not executed by a thread
     * @throws IOException
     * @see IOException
     */
    public void doStuff() throws IOException {
        scan();
    }

    /**
     * Scans IPv4 addresses pool limited by BEGIN, END variables
     * This method is being executed by a thread only
     */
    @Override
    public void run() {
        try {
            scan();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * UNUSED
     * Method wrote to find the best amplification with a variety of different types of queries on different sites
     * All queries were being tested on 8.8.8.8 IP address
     * @param domain represents a domain
     * @param type represents a type of query
     * @param address represents IP address on which method sends a query
     * @return int This returns 0 if an error is being occurred or number of bytes received form DNS server on specific query
     * @throws IOException
     * @see IOException
     */
    protected int testScan(String domain, String type, String address) throws  IOException{

        InetAddress current = InetAddress.getByName(address);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        int it = Integer.parseInt(type, 16);

        /* Building DNS packet */
        //Identifier
        dos.writeShort(0x1234);

        //Query parameters
        dos.writeShort(0x0000);

        //Number of questions
        dos.writeShort(0x0001);

        //Number of answers
        dos.writeShort(0x0000);

        //Number of authority records
        dos.writeShort(0x0000);

        //Number of additional records
        dos.writeShort(0x0000);

        //splitting domain into parts
        String[] domainParts = domain.split("\\.");

        //coding domain name in UTF-8 (HEX)
        for (int i = 0; i<domainParts.length; i++) {
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
            dos.writeByte(domainBytes.length);
            dos.write(domainBytes);
        }

        //The end of domain's name
        dos.writeByte(0x00);

        //Type of the query - the biggest response found for 000F type
        dos.writeShort(it);

        //Class 0x0001 = IN
        dos.writeShort(0x0001);

        byte[] dnsFrame = baos.toByteArray();

        //System.out.println("Sending: " + dnsFrame.length + " bytes to " + domain + " domain with typeID " + type);

        /* sending DNS packet */
        DatagramSocket socket = null;
        DatagramPacket dnsReqPacket = null;
        DatagramPacket packet = null;
        try{
            socket = new DatagramSocket();
            dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, current, DNS_SERVER_PORT);
            socket.send(dnsReqPacket);
            byte[] buf = new byte[2048];
            packet = new DatagramPacket(buf, buf.length);
            //Waiting for a response limited by 20 ms
            socket.setSoTimeout(20);
            socket.receive(packet);
            socket.close();
            FileWriter fileWriter = new FileWriter("DNS_test.txt", true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("Sending: " + dnsFrame.length + " bytes to " + domain + " domain with typeID " + type + " DNS IP address " + current.toString() + " " + packet.getLength() +" bytes received");
            printWriter.close();
            //System.out.println(packet.getLength() +" bytes received");
            return packet.getLength();
        }
        catch(Exception e){}
        finally {
             if (socket != null) {
                try {
                    socket.close();
                } catch (NullPointerException ex1) {
                    socket = null;
                }
             }
        }
        return 0;
    }

    /**
     * Builds a DNS packet
     * build packet is being saved to dnsFrame, which is a byte array
     * @throws IOException
     * @see IOException
     */
    protected void buildPacket() throws IOException {

        String domain = "Live.com";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        /** Building DNS packet **/

        //Identifier
        dos.writeShort(0x1234);

        //Query parameters
        dos.writeShort(0x0000);

        //Number of questions
        dos.writeShort(0x0001);

        //Number of answers
        dos.writeShort(0x0000);

        //Number of authority records
        dos.writeShort(0x0000);

        //Number of additional records
        dos.writeShort(0x0000);

        //splitting domain into parts
        String[] domainParts = domain.split("\\.");

        //coding domain name in UTF-8 (HEX)
        for (int i = 0; i<domainParts.length; i++) {
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
            dos.writeByte(domainBytes.length);
            dos.write(domainBytes);
        }

        //The end of domain's name
        dos.writeByte(0x00);

        //Type of the query
        dos.writeShort(0x0010);

        //Class 0x0001 = IN
        dos.writeShort(0x0001);
        dnsFrame = baos.toByteArray();
    }

    /**
     * The method that creates the file "DNS_Vulnerable.txt" and appends found results
     * It is synchronized to avoid sharing the same resources among threats
     * @param serverAddress used to represent IP address in a string, using toString method
     * @param packet used to write a number of received bytes, using getLength method
     * @throws IOException
     * @see IOException
     */
    protected synchronized void writeToFile(InetAddress serverAddress, DatagramPacket packet) throws IOException {
        FileWriter fileWriter = new FileWriter("DNS_Vulnerable.txt", true); //Set true for append mode
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DNS IP address " + serverAddress.toString() + " " + packet.getLength() +" bytes received");
        printWriter.close();
    }

    /**
     * Creates a socket with UDP transport protocol
     * Sends a query to a specific IP address, then waits a limited time for an answer
     * If an answer was received it checks its length
     * When conditions were met, method prints out the message and write to File if toFile flag equals TRUE
     * @param serverAddress represents an IP address on which method sends a query
     * @throws IOException
     * @see IOException
     */
    @Override
    public void query(InetAddress serverAddress) throws IOException {

        /* sending DNS packet */
        DatagramSocket socket = null;
        try{
            socket = new DatagramSocket();

            DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, serverAddress, DNS_SERVER_PORT);
            socket.send(dnsReqPacket);

            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            //Waiting for a response limited by 40 ms
            socket.setSoTimeout(40);
            socket.receive(packet);
            socket.close();
            try {
                if (packet.getLength() >= dnsFrame.length * 1) {
                    if (toFile) {
                        writeToFile(serverAddress, packet);
                    }
                    System.out.println("DNS IP address " + serverAddress.toString() + " " + packet.getLength() + " bytes received");
                }
            }catch(NullPointerException e){}
        }
        catch(SocketTimeoutException e) {
            //System.out.println("TimeoutException");
        }
        catch(SocketException e) {
            //System.out.println("SocketException");
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (NullPointerException ex1) {
                    socket = null;
                }
            }
        }
    }
}
