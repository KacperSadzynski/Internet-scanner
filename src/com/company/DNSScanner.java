package com.company;

import java.io.*;
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

    @Override
    public void query(InetAddress serverAddress) throws IOException {
        String domain = "google.com";

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
        dos.writeShort(0x000F);

        //Class 0x0001 = IN
        dos.writeShort(0x0001);

        byte[] dnsFrame = baos.toByteArray();

        //System.out.println("Sending: " + dnsFrame.length + " bytes");

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
            if(packet.getLength() >= dnsFrame.length*2){
                if (toFile) {
                    //File file = new File("DNS_Vulnerable.txt");
                    FileWriter fileWriter = new FileWriter("DNS_Vulnerable.txt", true); //Set true for append mode
                    PrintWriter printWriter = new PrintWriter(fileWriter);
                    printWriter.println("DNS IP address " + serverAddress.toString() + " " + packet.getLength() +" bytes received");
                    printWriter.close();
                }
                System.out.println("DNS IP address " + serverAddress.toString() + " " + packet.getLength() +" bytes received");
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
