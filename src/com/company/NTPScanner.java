package com.company;
/* 193.110.137.132 test*/

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;

/**
 * NTPScanner class inherits from the IPv4Addresses class and Runnable interface<br/>
 * By default, this class is being executed by a thread<br/>
 * It builds a NTP packet that is sent by query(), then a thread execute run() method<br/>
 * It scans all public IPv4 addresses limited by BEGIN, END variables<br/>
 * If the amplification of the sent packet is big enough it print out the IP of this server<br/>
 * If the toFile flag is TRUE it writes to NTP_Vulnerable.txt file output as well<br/>
 * Instance Variables:<br/>
 * static final int NTP_SERVER_PORT - represents NTP server port, set on 123<br/>
 * static boolean isYourFirstTime - the flag that checks if a given object of a class is its first created object<br/>
 * @see IPv4Addresses
 */
public class NTPScanner extends IPv4Addresses implements Runnable{

    private static final int NTP_SERVER_PORT = 123;
    private static boolean isYourFirstTime = true;
    /**
     * Constructor<br/>
     * It removes file NTP_Vulnerable.txt if exists to avoid appending new output to the old one<br/>
     * Builds messageUdp byte array using the {@link #buildPacket() buildPacket} method<br/>
     * Sets parameters: amplification, packetType, fileName corresponding to the NTPScanner<br/>
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     * @see #fileManager(boolean, int)
     * @see IPv4Addresses
     */
    public NTPScanner(int begin, int end) throws IOException {
        amplification = 1;
        packetType = "NTP";
        fileName = "NTP_Vulnerable.txt";
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
        if(isYourFirstTime){
            isYourFirstTime = fileManager(isYourFirstTime, messageUdp.length);
        }
    }
    
    /**
     * Builds a NTP packet which sends an NTPv2 request for READVAR control message<br/>
     * built packet is being saved to messageUdp byte array<br/>
     * @see IPv4Addresses
     */  
    protected void buildPacket() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        //LI-VN-Mode-R-E-M-Opcode (BIN) 00-010-110-0-0-0-00010
        dos.writeShort(0x1602);
        //Sequence (BIN) 0000000000000001
        dos.writeShort(0x0001);
        //Rest of the packet
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);
        dos.writeShort(0x0000);

        messageUdp = baos.toByteArray();
    }
    /**
     * Scans IPv4 addresses pool limited by BEGIN, END variables<br/>
     * This method is being executed by a thread only<br/>
     * @see #scan()
     */
    @Override
    public void run(){
        try {
            scan();
        } catch (InterruptedIOException ex){
            System.out.println("NTPThread interrupted");
        } catch (IOException e) {
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
        vulnerability(sendUdpPacket(serverAddress, NTP_SERVER_PORT, 40), messageUdp.length, serverAddress.toString(), "UDP");
    }
}
