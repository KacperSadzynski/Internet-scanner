package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.*;

/**
 * IPv4Addresses is the <b/>superclass</b>, any other Scanner classes inherit from this class<br/>
 * this class is only for inheritance purposes<br/>
 * Instance Variables:<br/>
 * static boolean toFile - subclasses use this to decide whether they write to file their output or not, by default set to FALSE<br/>
 * int BEGIN, END - use to limit the IPv4 scan range, used only by scan() method<br/>
 * String packetType - represents type of a packet which will be printed out in a message, used in writeToFile method<br/>
 * String fileName - represents a name of a file in which a message will be printed out<br/>
 * <b/>e.g</b> setting BEGIN to 1 and END to 2 means that method scan() generates addresses from 1.0.0.0 to 1.255.255.255 only<br/>
 */
public class IPv4Addresses {

    protected static boolean toFile;
    static{
    toFile = false;
    }
    protected int BEGIN;
    protected int END;
    protected String packetType, fileName;

    /**
     * Generates IPv4 addresses limited by BEGIN, END variables<br/>
     * For each generated address, query() method is being used<br/>
     * @throws IOException
     */
    protected void scan() throws IOException {
        Integer[] rawIPList = new Integer[] {0, 0, 0, 0};
        try {
            for (int i = BEGIN; i < END; i++) {
                rawIPList[0] = i;
                if (i == 0 || i == 10 || i == 127)
                    continue;
                for (int j = 0; j < 256; j++) {
                    rawIPList[1] = j;
                    //System.out.println(i + "." + j + ".0.0 reached");
                    for (int k = 0; k < 256; k++) {
                        System.out.println(i + "." + j + "." + k + ".0 reached");
                        rawIPList[2] = k;
                        for (int l = 0; l < 256; l++) {
                            rawIPList[3] = l;
                            String address = rawIPList[0].toString() + "." + rawIPList[1].toString() + "." + rawIPList[2].toString() + "." + rawIPList[3].toString();
                            if (address.equals("255.255.255.255"))
                                continue;
                            address = "127.0.0.1";
                            //System.out.println(address);
                            InetAddress current = InetAddress.getByName(address);
                            query(current);
                        }
                    }
                }
            }
        }catch (InterruptedIOException ex){
            System.out.println("DNSThread interrupted");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * An abstract method, subclasses use this method to send different types of queries<br/>
     * @param dest on this address, the query is being sent
     * @throws IOException
     */
    protected void query(InetAddress dest) throws IOException {}

    /**
     * Default constructor
     */
    public IPv4Addresses(){}

    /**
     * Constructor<br/>
     * Used when a user writes -w in args<br/>
     * @param f used to set  toFile variable
     */
    public IPv4Addresses(boolean f){
        toFile = f;
    }

    /**
     * The method that creates the file and appends found results<br/>
     * It is synchronized to avoid sharing the same resources among threats<br/>
     * @param serverAddress used to represent IP address in a string, using toString method
     * @param packet used to write a number of received bytes, using getLength method
     * @throws IOException
     */
    protected synchronized void writeToFile(InetAddress serverAddress, DatagramPacket packet) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName, true); //Set true for append mode
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println(packetType + " IP address " + serverAddress.toString() + " " + packet.getLength() +" bytes received with UDP");
        printWriter.close();
    }

    protected int sendUdpPacket(byte[] message, InetAddress serverAddress, int portNumber, int timeout) {

        DatagramSocket udpSocket = null;
        int bytesRead = 0;
        int control = 0;
        try {
            udpSocket = new DatagramSocket();
            DatagramPacket ntpReqPacket = new DatagramPacket(message, message.length, serverAddress, portNumber);
            udpSocket.send(ntpReqPacket);
            byte[] buf = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            udpSocket.setSoTimeout(timeout);
            while (true) {
                udpSocket.receive(packet);
                bytesRead += packet.getLength();
                control++;
            }
        }
        catch (SocketException e) {
            if(control == 0)
                bytesRead = -1;
        }
        catch (IOException ignored) { }
        finally {
            if (udpSocket != null) {
                try {
                    udpSocket.close();
                } catch (NullPointerException ignored) { }
            }
        }
        return bytesRead;
    }
}
