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
    private int BEGIN;
    private int END;
    byte[] dnsFrame;
    DatagramPacket packet;
    public DNSScanner(int begin, int end) throws IOException {
        File file = new File("DNS_Vulnerable.txt");
        if(file.exists()){
           file.delete();
        }
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
    }
    public void doStuff() throws IOException {
        scan();
    }
    @Override
    public void run() {
        try {
            Integer[] rawIPList = new Integer[] {0, 0, 0, 0};
            /** Generating all IPv4 addresses **/
            for(int i=BEGIN+1; i<END;i++){
                rawIPList[0]=i;
                for(int j=0; j<256;j++){
                    rawIPList[1]=j;
                    for(int k=0; k<256;k++){
                        System.out.println(i + "."+ j +"." + k +".0 reached");
                        rawIPList[2]=k;
                        for(int l=0; l<256;l++){
                            rawIPList[3]=l;
                            String address = rawIPList[0].toString()+"."+rawIPList[1].toString()+"."+rawIPList[2].toString()+"."+rawIPList[3].toString();
                            if (address.equals("0.0.0.0")) {
                                continue;
                            }
                            InetAddress serverAddress = InetAddress.getByName(address);
                            query(serverAddress);
                            Thread.yield();
                            //TimeUnit.MILLISECONDS.sleep(20);
                            try {
                                if (packet.getLength() >= dnsFrame.length * 1) {
                                    if (toFile) {
                                        writeToFile(serverAddress, packet);
                                    }
                                    System.out.println("DNS IP address " + serverAddress.toString() + " " + packet.getLength() + " bytes received");
                                }
                            }catch(NullPointerException e){}
                        }
                    }
                }
            }

        } catch (InterruptedIOException ex){
            System.out.println("DNSThread interrupted");
        } catch (IOException e) {
            e.printStackTrace();
        } //catch (InterruptedException e) {
           // e.printStackTrace();
        //}

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
    public synchronized void query(InetAddress serverAddress) throws IOException {

        /** sending DNS packet **/
        DatagramSocket socket = null;
        this.packet = null;
        try{
            socket = new DatagramSocket();
            DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, serverAddress, DNS_SERVER_PORT);
            socket.send(dnsReqPacket);
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            //Waiting for a response limited by 20 ms
            socket.setSoTimeout(20);
            socket.receive(packet);
            this.packet = packet;

            socket.close();
            //System.out.println("DNS IP address " + serverAddress.toString() + " " + packet.getLength() + " bytes received");
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
    }
}
