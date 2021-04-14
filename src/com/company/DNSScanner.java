package com.company;

import java.io.*;
import java.math.BigInteger;
import java.net.*;


/**
 * DNSScanner class creates a socket bound to port 53 and a UDP packet for DNS protocol
 * It scans all public IPv4 addresses by sending to all available DNS servers query and waiting for respond
 * If the amplification of the sent packet is big enough it print out the IP of this server
 **/
public class DNSScanner extends IPv4Addresses {
    private static final int DNS_SERVER_PORT = 53;

    public DNSScanner() {
        File file = new File("DNS_Vulnerable.txt");
        if(file.exists()){
           file.delete();
        }
    }

    public void doStuff() throws IOException {
        scan();
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
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, current, DNS_SERVER_PORT);
        try{
            socket.send(dnsReqPacket);

            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
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
        catch(SocketTimeoutException e) {
            socket.close();
        }
        catch(SocketException e) {
            socket.close();
        }
        return 0;
    }

    @Override
    public void query(InetAddress serverAddress) throws IOException {
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

        //Type of the query - the biggest response found for 000F type
        dos.writeShort(0x0010);

        //Class 0x0001 = IN
        dos.writeShort(0x0001);

        byte[] dnsFrame = baos.toByteArray();
/*
        System.out.println("Sending: " + dnsFrame.length + " bytes");

        for (int i =0; i< dnsFrame.length; i++) {
            System.out.print("0x" + String.format("%x", dnsFrame[i]) + " " );
        }


 */
        /** sending DNS packet **/
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, serverAddress, DNS_SERVER_PORT);
        try{
            socket.send(dnsReqPacket);
            
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            //Waiting for a response limited by 20 ms
            socket.setSoTimeout(20);
            socket.receive(packet);

            socket.close();
            if(packet.getLength() >= dnsFrame.length*1){
                if (toFile) {
                    //File file = new File("DNS_Vulnerable.txt");
                    FileWriter fileWriter = new FileWriter("DNS_Vulnerable.txt", true); //Set true for append mode
                    PrintWriter printWriter = new PrintWriter(fileWriter);
                    printWriter.println("DNS IP address " + serverAddress.toString() + " " + packet.getLength() +" bytes received");
                    printWriter.close();
                }
                System.out.println("DNS IP address " + serverAddress.toString() + " " + packet.getLength() +" bytes received");
                /*
                for (int i = 0; i < packet.getLength(); i++) {
                    System.out.print(" 0x" + String.format("%x", buf[i]) + " " );
                }
                System.out.println("\n");


                DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf));
                System.out.println("Transaction ID: 0x" + String.format("%x", din.readShort()));
                System.out.println("Flags: 0x" + String.format("%x", din.readShort()));
                System.out.println("Questions: 0x" + String.format("%x", din.readShort()));
                System.out.println("Answers RRs: 0x" + String.format("%x", din.readShort()));
                System.out.println("Authority RRs: 0x" + String.format("%x", din.readShort()));
                System.out.println("Additional RRs: 0x" + String.format("%x", din.readShort()));

                int recLen = 0;
                while ((recLen = din.readByte()) > 0) {
                    byte[] record = new byte[recLen];

                    for (int i = 0; i < recLen; i++) {
                        record[i] = din.readByte();
                    }

                    System.out.println("Record: " + new String(record, "UTF-8"));
                }

                System.out.println("Record Type: 0x" + String.format("%x", din.readShort()));
                System.out.println("Class: 0x" + String.format("%x", din.readShort()));

                System.out.println("Field: 0x" + String.format("%x", din.readShort()));
                System.out.println("Type: 0x" + String.format("%x", din.readShort()));
                System.out.println("Class: 0x" + String.format("%x", din.readShort()));
                System.out.println("TTL: 0x" + String.format("%x", din.readInt()));

                short addrLen = din.readShort();
                System.out.println("Len: 0x" + String.format("%x", addrLen));

                System.out.print("Address: ");
                for (int i = 0; i < addrLen; i++ ) {
                    System.out.print("" + String.format("%d", (din.readByte() & 0xFF)) + ".");
                }

                 */
            }
        }
        catch(SocketTimeoutException e) {
            socket.close();
        }
        catch(SocketException e) {
            socket.close();
        }
    }
}
