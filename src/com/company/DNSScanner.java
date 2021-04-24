package com.company;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.concurrent.TimeUnit;


/**
 * DNSScanner class creates a socket bound to port 53 and a UDP packet for DNS protocol
 * It scans all public IPv4 addresses by sending to all available DNS servers query and waiting for respond
 * If the amplification of the sent packet is big enough it print out the IP of this server
 **/
public class DNSScanner extends IPv4Addresses implements Runnable {
    private static final int DNS_SERVER_PORT = 53;

    byte[] dnsFrame;
    public DNSScanner(int begin, int end) throws IOException {
        File file = new File("DNS_Vulnerable.txt");
        if(file.exists()){
           file.delete();
        }
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
    }
    public void doStuff() throws IOException {
        scan();
    }
    @Override
    public void run() {
        try {
            scan();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    protected int testScan(String domain, String type, String address) throws  IOException{
        //System.out.println(domain + type);
        InetAddress current = InetAddress.getByName(address);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        int it = Integer.parseInt(type, 16);

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

        //Type of the query - the biggest response found for 000F type
        dos.writeShort(it);

        //Class 0x0001 = IN
        dos.writeShort(0x0001);

        byte[] dnsFrame = baos.toByteArray();

        //System.out.println("Sending: " + dnsFrame.length + " bytes to " + domain + " domain with typeID " + type);

        /** sending DNS packet **/
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

    protected synchronized void writeToFile(InetAddress serverAddress, DatagramPacket packet) throws IOException {
        FileWriter fileWriter = new FileWriter("DNS_Vulnerable.txt", true); //Set true for append mode
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DNS IP address " + serverAddress.toString() + " " + packet.getLength() +" bytes received");
        printWriter.close();
    }

    @Override
    public void query(InetAddress serverAddress) throws IOException {

        /** sending DNS packet **/
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
            //System.out.println("Rzucono TimeoutException");
        }
        catch(SocketException e) {
            //System.out.println("Rzucono socketexception");
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
