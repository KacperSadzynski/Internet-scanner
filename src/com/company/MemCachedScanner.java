package com.company;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * MemCachedScanner class inherits from the IPv4Addresses class and Runnable interface<br/>
 * By default, this class is being executed by a thread<br/>
 * It builds a MemCached packet that is sent by query(), then a thread execute run() method<br/>
 * It scans all public IPv4 addresses limited by BEGIN, END variables<br/>
 * If the amplification of the sent packet is big enough it print out the IP of this server<br/>
 * If the toFile flag is TRUE it writes to MemCached_Vulnerable.txt file output as well<br/>
 * Instance Variables:<br/>
 * static final int MEMCACHED_SERVER_PORT - represents MemCached server port, set on 11211<br/>
 * static boolean isYourFirstTime - the flag that checks if a given object of a class is its first created object<br/>
 * @see IPv4Addresses
 */
public class MemCachedScanner extends IPv4Addresses implements Runnable {

    private static final int MEMCACHED_SERVER_PORT = 11211;
    private static boolean isYourFirstTime = true;

    /**
     * Constructor<br/>
     * It removes file MemCached_Vulnerable.txt if exists to avoid appending new output to the old one<br/>
     * Builds messageUdp byte array using the {@link #buildPacket() buildPacket} method<br/>
     * Writes to messageTCP String "stats\r\n" which will ask about server's stats<br/>
     * Sets parameters: amplification, packetType, fileName corresponding to the MemCachedScanner, messageTCPSize<br/>
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     * @see #fileManager(boolean, int)
     * @see IPv4Addresses
     */
    public MemCachedScanner(int begin, int end) throws IOException {
        amplification = 1;
        packetType = "MemCached";
        fileName = "MemCached_Vulnerable.txt";
        messageTCP = "stats\r\n";
        messageTCPSize = messageTCP.getBytes().length;
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
        if(isYourFirstTime){
            isYourFirstTime = fileManager(isYourFirstTime, messageUdp.length);
        }
    }

    /**
     * Builds a MemCached packet which will ask about server's stats<br/>
     * built packet is being saved to memCachedFrame, which is a byte array<br/>
     */
    protected void buildPacket() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        //Magic
        dos.writeShort(0x00);
        //Opcode
        dos.writeShort(0x00);
        //Key length
        dos.writeShort(0x0001);
        //Extra length
        dos.writeShort(0x00);
        //Data type
        //dos.writeShort(0x00);
        //Reserved
        dos.writeShort(0x7374);
        //Total body
        dos.writeShort(0x6174);
        //Opaque
        dos.writeShort(0x730d);
        //CAS
        dos.writeShort(0x0a00);

        //Extras, Key, Values - None
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
        } catch (InterruptedIOException ex) {
            System.out.println("MemCachedThread interrupted");
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
    private void queryUDP(InetAddress serverAddress) {
        vulnerability(sendUdpPacket(serverAddress,MEMCACHED_SERVER_PORT, 40), messageUdp.length, serverAddress.toString(), "UDP");
    }
    /**
     * Creates a socket with TCP transport protocol<br/>
     * When connection between client and server is established, it sends a query<br/>
     * If an answer was received it checks its length<br/>
     * When conditions were met, method prints out the message and write to File if toFile flag equals TRUE<br/>
     * @param serverAddress represents an IP address on which method sends a query
     * @see #vulnerability(int, int, String, String)
     * @see IPv4Addresses
     */
    private void queryTCP(InetAddress serverAddress){
        Socket tcpSocket = null;
        BufferedReader reader = null;
        try{
            tcpSocket = new Socket();
            tcpSocket.connect(new InetSocketAddress(serverAddress, MEMCACHED_SERVER_PORT), 500);
            Writer out = new OutputStreamWriter(tcpSocket.getOutputStream(), "ASCII");
            out.write(messageTCP);
            out.flush();
            reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream(), "ASCII"));
            String buffer = null;
            String received = "";
            while ((buffer = reader.readLine()) != null){
                received += buffer;
                if(buffer.equals("END")) break;
            }
            int responseSize = received.getBytes().length;
            reader.close();
            tcpSocket.close();
            vulnerability(responseSize, messageTCPSize, serverAddress.toString(), "TCP");
        }
        catch (Exception ignore) { }
        finally {
            if (tcpSocket != null) {
                try {
                    tcpSocket.close();
                }
                catch (IOException ignore) { }
                catch (NullPointerException ex1) {
                    tcpSocket = null;
                }
            }
        }
    }
    /**
     * Calls methods queryUDP(InetAddress) and queryTCP(InetAddress)<br/>
     * Scans address given as parameter firstly with UDP and later with TCP<br/>
     * @param serverAddress represents an IP address on which method sends a query
     */
    @Override
    public void query(InetAddress serverAddress) {
        queryUDP(serverAddress);
        queryTCP(serverAddress);
    }
}
