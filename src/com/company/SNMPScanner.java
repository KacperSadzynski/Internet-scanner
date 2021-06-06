package com.company;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;

/**
 * SNMPScanner class inherits from the IPv4Addresses class and Runnable interface<br/>
 * By default, this class is being executed by a thread<br/>
 * It builds a SNMP packet that is sent by query(), then a thread execute run() method<br/>
 * It scans all public IPv4 addresses limited by BEGIN, END variables<br/>
 * If the amplification of the sent packet is big enough it prints out the IP of this server<br/>
 * If the toFile flag is TRUE it writes to SNMP_Vulnerable.txt file output as well<br/>
 * Instance Variables:<br/>
 * static final int SNMP_SERVER_PORT - represents SNMP server port, set on 161<br/>
 * static boolean isYourFirstTime - the flag that checks if a given object of a class is its first created object<br/>
 * @see IPv4Addresses
 */
public class SNMPScanner extends IPv4Addresses implements Runnable{

    private static final int SNMP_SERVER_PORT = 161;
    private static boolean isYourFirstTime = true;
    /**
     * Constructor<br/>
     * It removes file SNMP_Vulnerable.txt if exists to avoid appending new output to the old one using fileManager method<br/>
     * Builds messageUdp byte array using the {@link #buildPacket() buildPacket} method<br/>
     * Sets parameters: amplification, packetType, fileName corresponding to the SNMPScanner<br/>
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     * @see #fileManager(boolean, int)
     * @see IPv4Addresses
     */
    public SNMPScanner(int begin, int end) throws IOException {
        amplification = 2;
        packetType = "SNMP";
        fileName = "SNMP_Vulnerable.txt";
        this.BEGIN=begin;
        this.END=end;
        buildPacket();
        if(isYourFirstTime){
            isYourFirstTime = fileManager(isYourFirstTime, messageUdp.length);
        }
    }
    /**
     * Builds a SNMP packet which will ask about server's system description<br/>
     * built packet is being saved to messageUdp byte array<br/>
     * @see IPv4Addresses
     */
    protected void buildPacket() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        //Building SNMP packet
        dos.writeShort(0x3029);
        dos.writeShort(0x0201);
        dos.writeShort(0x0104);
        dos.writeShort(0x0670);
        dos.writeShort(0x7562);
        dos.writeShort(0x6c69);
        dos.writeShort(0x63a0);
        dos.writeShort(0x1c02);
        dos.writeShort(0x0401);
        dos.writeShort(0x0203);
        dos.writeShort(0x0402);
        dos.writeShort(0x0100);
        dos.writeShort(0x0201);
        dos.writeShort(0x0030);
        dos.writeShort(0x0e30);
        dos.writeShort(0x0c06);
        dos.writeShort(0x082b);
        dos.writeShort(0x0601);
        dos.writeShort(0x0201);
        dos.writeShort(0x0101);
        dos.writeShort(0x0005);
        dos.writeShort(0x00);
        messageUdp = baos.toByteArray();
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
        }
         catch (InterruptedIOException ex){
            System.out.println("SNMPThread interrupted");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Creates a socket with UDP transport protocol<br/>
     * Sends a query to a specific IP address, then waits a limited time for an answer<br/>
     * If an answer was received it checks its length<br/>
     * When conditions were met, method prints out the message and write to File if toFile flag equals TRUE<br/>
     * @param serverAddress represents an IP address on which method sends a query
     * @see #vulnerability(int, int, String, String)
     * @see #sendUdpPacket(InetAddress, int, int)
     * @see IPv4Addresses
     */
    @Override
    public void query(InetAddress serverAddress) {
        vulnerability(sendUdpPacket(serverAddress, SNMP_SERVER_PORT, 100), messageUdp.length, serverAddress.toString(), "UDP");
    }
}
