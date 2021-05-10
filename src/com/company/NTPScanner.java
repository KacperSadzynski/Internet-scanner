package com.company;

import java.io.*;
import java.net.*;
/**
 * NTPScanner class inherits from the IPv4Addresses class and Runnable interface<br/>
 * By default, this class is being executed by a thread<br/>
 * It builds a NTP packet that is sent by query(), then a thread execute run() method<br/>
 * It scans all public IPv4 addresses limited by BEGIN, END variables<br/>
 * If the amplification of the sent packet is big enough it print out the IP of this server<br/>
 * If the toFile flag is TRUE it writes to NTP_Vulnerable.txt file output as well<br/>
 * Instance Variables:<br/>
 * static final int NTP_SERVER_PORT - represents DNS server port, set on 53<br/>
 * byte[] ntpFrame - byte array that represents DNS packet<br/>
 * @see IPv4Addresses
 */
public class NTPScanner extends IPv4Addresses implements Runnable{

    public static final int NTP_SERVER_PORT = 123;
    private byte[] ntpFrame;
    
    /**
     * Constructor<br/>
     * It removes file NTP_Vulnerable.txt if exists to avoid appending new output to the old one<br/>
     * Builds ntpFrame byte array using the buildPacket() method<br/>
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     * @throws IOException
     */
    public NTPScanner(int begin, int end) throws IOException {
        packetType = "NTP";
        fileName = "NTP_Vulnerable.txt";
        File file = new File(fileName);
        if(file.exists()){
            file.delete();
        }
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
        if(toFile) {
            FileWriter fileWriter = new FileWriter(fileName, true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(ntpFrame.length + " bytes sent\n");
            printWriter.close();
        }
    }
    
    /**
     * Builds a NTP packet<br/>
     * build packet is being saved to ntpFrame, which is a byte array<br/>
     * @throws IOException
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
        
        ntpFrame = baos.toByteArray();
    }
    /**
     * Scans IPv4 addresses pool limited by BEGIN, END variables<br/>
     * This method is being executed by a thread only<br/>
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
     * @throws IOException
     */
    @Override
    public void query(InetAddress serverAddress) throws IOException {
    DatagramSocket socket = null;
            try{
                socket = new DatagramSocket();

                DatagramPacket ntpReqPacket = new DatagramPacket(ntpFrame, ntpFrame.length, serverAddress, NTP_SERVER_PORT);
                socket.send(ntpReqPacket);

                byte[] buf = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                //Waiting for a response limited by 40 ms
                socket.setSoTimeout(40);
                socket.receive(packet);
                socket.close();
                try {
                    if (packet.getLength() >= ntpFrame.length * 1) {
                        if (toFile) {
                            writeToFile(serverAddress, packet);
                        }
                        System.out.println("NTP IP address " + serverAddress.toString() + "\t" + ntpReqPacket.getLength() + " bytes sent " + packet.getLength() + " bytes received");
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
