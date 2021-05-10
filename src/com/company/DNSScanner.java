package com.company;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.concurrent.TimeUnit;


/**
 * DNSScanner class inherits from the IPv4Addresses class and Runnable interface<br/>
 * By default, this class is being executed by a thread but for test purposes method testScan can be used as well<br/>
 * It builds a DNS packet that is sent by query(), then a thread execute run() method<br/>
 * It scans all public IPv4 addresses limited by BEGIN, END variables<br/>
 * If the amplification of the sent packet is big enough it print out the IP of this server<br/>
 * If the toFile flag is TRUE it writes to DNS_Vulnerable.txt file output as well<br/>
 * Instance Variables:<br/>
 * static final int DNS_SERVER_PORT - represents DNS server port, set on 53<br/>
 * byte[] dnsFrame - byte array that represents DNS packet<br/>
 * @see IPv4Addresses
 */
public class DNSScanner extends IPv4Addresses implements Runnable {

    private static final int DNS_SERVER_PORT = 53;
    byte[] dnsFrame;

    /**
     * Constructor<br/>
     * It removes file DNS_Vulnerable.txt if exists to avoid appending new output to the old one<br/>
     * Builds dnsFrame byte array using the buildPacket() method<br/>
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     * @throws IOException
     */
    public DNSScanner(int begin, int end) throws IOException {
        packetType = "DNS";
        fileName = "DNS_Vulnerable.txt";
        File file = new File(fileName);
        if(file.exists()){
           file.delete();
        }
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
    }

    /**
     * UNUSED<br/>
     * Scans IPv4 addresses pool limited by BEGIN, END variables<br/>
     * Used when DNSScanner class was not executed by a thread<br/>
     * @throws IOException
     */
    public void doStuff() throws IOException {
        scan();
    }

    /**
     * Scans IPv4 addresses pool limited by BEGIN, END variables<br/>
     * This method is being executed by a thread only<br/>
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
     * UNUSED<br/>
     * Method wrote to find the best amplification with a variety of different types of queries on different sites<br/>
     * All queries were being tested on 8.8.8.8 IP address<br/>
     * @param domain represents a domain
     * @param type represents a type of query
     * @param address represents IP address on which method sends a query
     * @return int This returns 0 if an error is being occurred or number of bytes received form DNS server on specific query
     * @throws IOException
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
     * Builds a DNS packet<br/>
     * build packet is being saved to dnsFrame, which is a byte array<br/>
     * @throws IOException
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
     * Creates a socket with UDP transport protocol<br/>
     * Sends a query to a specific IP address, then waits a limited time for an answer<br/>
     * If an answer was received it checks its length<br/>
     * When conditions were met, method prints out the message and write to File if toFile flag equals TRUE<br/>
     * @param serverAddress represents an IP address on which method sends a query
     * @throws IOException
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
                    System.out.println(dnsReqPacket.getLength() + " bytes sent;\nDNS IP address " + serverAddress.toString() + " " + packet.getLength() + " bytes received");
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
