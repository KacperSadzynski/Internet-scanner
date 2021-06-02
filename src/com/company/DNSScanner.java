package com.company;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.concurrent.TimeUnit;


/**
 * DNSScanner class inherits from the IPv4Addresses class and Runnable interface<br/>
 * By default, this class is being executed by a thread<br/>
 * It builds a DNS packet that is sent by query(), then a thread execute run() method<br/>
 * It scans all public IPv4 addresses limited by BEGIN, END variables<br/>
 * If the amplification of the sent packet is big enough it prints out the IP of this server<br/>
 * If the toFile flag is TRUE it writes to DNS_Vulnerable.txt file output as well<br/>
 * Instance Variables:<br/>
 * static final int DNS_SERVER_PORT - represents DNS server port, set on 53<br/>
 * static boolean isYourFristTime - the flag that checks if a given object of a class is its first created object<br/>
 * @see IPv4Addresses
 */
public class DNSScanner extends IPv4Addresses implements Runnable {

    private static final int DNS_SERVER_PORT = 53;
    private static boolean isYourFirstTime = true;

    /**
     * Constructor<br/>
     * It removes file DNS_Vulnerable.txt if exists to avoid appending new output to the old one using fileManager method<br/>
     * Builds messageUdp byte array using the buildPacket() method<br/>
     * Sets parameters: amplification, packetType, fileName corresponding to the DNSScanner<br/>
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     * @throws IOException
     * @see #fileManager(boolean, int)
     * @see IPv4Addresses
     */
    public DNSScanner(int begin, int end) throws IOException {
        amplification = 1;
        packetType = "DNS";
        fileName = "DNS_Vulnerable.txt";
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
        if(isYourFirstTime){
            isYourFirstTime = fileManager(isYourFirstTime, messageUdp.length);
        }
    }

    /**
     * Scans IPv4 addresses pool limited by BEGIN, END variables<br/>
     * This method is being executed by a thread only<br/>
     * @see #scan()
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
     * Builds a DNS packet<br/>
     * builded packet is being saved to messageUdp byte array<br/>
     * @throws IOException
     * @see IPv4Addresses
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
        messageUdp = baos.toByteArray();
    }
    
    /**
     * Creates a socket with UDP transport protocol<br/>
     * Sends a query to a specific IP address, then waits a limited time for an answer<br/>
     * If an answer was received it checks its length<br/>
     * When conditions were met, method prints out the message and write to File if toFile flag equals TRUE<br/>
     * @param serverAddress represents an IP address on which method sends a query
     * @throws IOException
     * @see #vulnerability(int, int, String, String)
     * @see #sendUdpPacket(InetAddress, int, int)
     * @see IPv4Addresses
     */
    @Override
    public void query(InetAddress serverAddress) throws IOException {
        vulnerability(sendUdpPacket(serverAddress, DNS_SERVER_PORT, 40), messageUdp.length, serverAddress.toString(), "UDP");
    }
}
