package com.company;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * IPv4Addresses is the <b/>superclass</b>, any other Scanner classes inherit from this class<br/>
 * This class is for inheritance purposes and to manage writing to file and progress bar drawing (static fields)<br/>
 * All instance variables are initialized in subclasses except toFile and pb<br/>
 * Instance Variables:<br/>
 * static boolean toFile - subclasses use this to decide whether they write to file their output or not, by default set to FALSE<br/>
 * int BEGIN, END - use to limit the IPv4 scan range, used only by scan() method<br/>
 * <i><b/>e.g.</b> setting BEGIN to 1 and END to 2 means that method scan() generates addresses from<br/>
 * 1.0.0.0 to 1.255.255.255 only<i/><br/>
 * String packetType - represents type of a packet, which will be printed out in a message, used in writeToFile method<br/>
 * String fileName - represents a name of a file in which a message will be printed out<br/>
 * byte[] messageUdp - represents a UDP message specified for every protocol<br/>
 * int amplification - it sets how large (at least) the amplification must be to consider a server as a vulnerable one<br/>
 * String messageTCP - message sent with TCP<br/>
 * int messageTCPSize - length of the messageTCP<br/>
 * ProgressBar pb - it allows all subclasses to draw a common progress bar<br/>
 */
public class IPv4Addresses {

    protected static boolean toFile;
    static{
        toFile = false;
    }
    protected int BEGIN;
    protected int END;
    protected String packetType, fileName;
    protected byte[] messageUdp;
    protected int amplification;
    protected String messageTCP;
    protected int messageTCPSize;
    public static ProgressBar pb;

    /**
     * Default constructor
     */
    public IPv4Addresses(){}

    /**
     * Constructor<br/>
     * It is used while creating manager object of this class to control writing to file and progress bar.<br/>
     * @param flag used to set toFile variable
     * @param states used in ProgressBar constructor to set number of steps to do
     */
    public IPv4Addresses(boolean flag, long states){
       toFile = flag;
       pb = new ProgressBar(states);
    }

    /**
     * Generates IPv4 addresses limited by BEGIN, END variables<br/>
     * For each generated address, query() method is used<br/>
     */
    protected void scan() throws IOException {
        Integer[] rawIPList = new Integer[] {0, 0, 0, 0};
        try {
            for (int i = BEGIN; i < END; i++) {
                rawIPList[0] = i;
                if (i == 0 || i == 10 || i == 127){
                    continue;
                }
                for (int j = 0; j < 256; j++) {
                    rawIPList[1] = j;
                    for (int k = 0; k < 256; k++) {
                        rawIPList[2] = k;
                        for (int l = 0; l < 256; l++) {
                            rawIPList[3] = l;
                            String address = rawIPList[0].toString() + "." + rawIPList[1].toString() + "." + rawIPList[2].toString() + "." + rawIPList[3].toString();
                            InetAddress current = InetAddress.getByName(address);
                            query(current);
                            pb.update();
                        }
                    }
                }
            }
        }
        catch (IOException ignore) { }
    }

    /**
     * An abstract method, subclasses use this method to send different types of queries<br/>
     * @param dest on this address, the query is being sent
     */
    protected void query(InetAddress dest) throws IOException {}

    /**
     * Method for demonstration purposes<br/>
     * It scans using all protocols on predefined ip addresses<br/>
     * Ip addresses are in demonstration.txt file<br/>
     * @param dest on this address, the query is being sent
     */
    public void demonstrationScan(InetAddress dest) throws IOException {
        query(dest);
    }

    /**
     * The method that creates the file and appends found results<br/>
     * It is synchronized to avoid sharing the same resources among threats<br/>
     * @param serverAddress used to represent IP address in a string, using toString method
     * @param bytesRead used to write a number of received bytes
     * @param protocol type of query protocol - UDP or TCP
     */
    protected synchronized void writeToFile(String serverAddress, int bytesRead, String protocol) {
        PrintWriter printWriter = null;
        try {
            FileWriter fileWriter = new FileWriter(fileName, true); //Set true for append mode
            printWriter = new PrintWriter(fileWriter);
            printWriter.println("IP address " + serverAddress + "  \t" + bytesRead + " bytes received with " + protocol);
        }
        catch(IOException ignore){ }
        finally {
            if(printWriter != null)
                printWriter.close();
        }
    }
    /**
     * Creates a DatagramSocket with UDP transport protocol<br/>
     * Sends a query to a specific IP address<br/>
     * It reads a message in a infinite while loop for a limited time specified by timeout param<br/>
     * @param serverAddress on this ip a query is sent
     * @param portNumber on this port socket is created
     * @param timeout determines the time to read answers from a server
     * @return numbers of bytes read or -1 if no reply was received
     */
    protected int sendUdpPacket(InetAddress serverAddress, int portNumber, int timeout) {
        DatagramSocket udpSocket = null;
        int bytesRead = 0;
        int control = 0;
        try {
            udpSocket = new DatagramSocket();
            DatagramPacket ReqPacket = new DatagramPacket(messageUdp, messageUdp.length, serverAddress, portNumber);
            udpSocket.send(ReqPacket);
            byte[] buf = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            udpSocket.setSoTimeout(timeout);
            boolean isMC = false;
            if (packetType.equals("MemCached")) isMC = true;
            do{
                udpSocket.receive(packet);
                bytesRead += packet.getLength();
                control++;
            } while (isMC);
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

    /**
     * It checks if the response is large enough to consider the server as a vulnerable one.<br/>
     * If yes, then it prints a message in console (and moves the progress bar into a line below thanks to "\r" and draw method).<br/>
     * When conditions are met, it writes a message into a file.<br/>
     * @param bytesRead length of the response
     * @param messageLength length of the request
     * @param serverAddress the address from which we got the response
     * @param protocol type of query protocol - UDP or TCP
     */
    protected void vulnerability(int bytesRead, int messageLength, String serverAddress, String protocol){
        //checking if the response has enough amplification
        if (bytesRead >= messageLength * amplification) {
            if (toFile) {
                writeToFile(serverAddress, bytesRead, protocol);
            }
            System.out.println("\r" + packetType + " IP address " + serverAddress + "  \t" + messageLength + " bytes sent " + bytesRead + " bytes received with " + protocol);
            pb.draw();
        }
    }

    /**
     * It deletes output file if exists and writes a beginning of a file if the conditions are met.<br/>
     * @param isYourFirstTime the flag that checks if a given object of a class is its first created object
     * @param sizeUdp size of the UDP request that is sent
     * @return false (because the method has already been called)
     */
    protected synchronized boolean fileManager(boolean isYourFirstTime, int sizeUdp){
        PrintWriter printWriter = null;
        try{
            if(isYourFirstTime){
                File file = new File(fileName);
                if(file.exists()){
                    file.delete();
                }
                if(toFile) {
                    FileWriter fileWriter = new FileWriter(fileName, true); //Set true for append mode
                    printWriter = new PrintWriter(fileWriter);
                    if(packetType.equals("MemCached")){
                        printWriter.println(sizeUdp + " bytes sent with UDP\n");
                        printWriter.println(messageTCPSize + " bytes sent with TCP\n");
                    }
                    else {
                        printWriter.println(sizeUdp + " bytes sent\n");
                    }
                }
                isYourFirstTime = false;
            }
        }
        catch(IOException ignored){ }
        finally {
            if(printWriter != null)
                printWriter.close();
        }
        return isYourFirstTime;
    }

    /**
     * ProgressBar is an internal class of IPv4Addresses (and is implemented as a static field in that class) and extends Thread<br/>
     * This class draws a progress bar at the last line in terminal with information how many steps have been done and the number of all steps to do.<br/>
     * Bar contains 50 elements (signs "=" for "done" and " " for "not done yet")/<br/>
     * Instance Variables:<br/>
     * long endState - represents the number of all steps to do<br/>
     * long actualState - represents the number of steps that have been already done<br/>
     * double jump - represents a number of "=" signs in the progress bar for one step.<br/>
     * <i><b/>e.g.</b> If endState is 100, then a jump will be 2, so doing 1 step will cause drawing two "=" signs in the bar.<i/><br/>
     * If the jump is lower than 1, then the "=" sign will be drawn, when actualState*jump will be greater than 1.<br/>
     */
    public class ProgressBar extends Thread{
            private long endState;
            private long actualState;
            private double jump;
            /**
             * Default Constructor<br/>
             * It  sets fields: actualState as 0 (as always), endState as 100, jump as 2 (because 100/50 is 2)<br/>
             */
            public ProgressBar(){
                actualState = 0L;
                endState = 100L;
                jump = 2;
            }

            /**
             * Constructor setting endState<br/>
             * It sets fields: actualState as 0 (as always), endState as given in the parameter, jump as endState/50<br/>
             * @param end to set endState - it is the number of steps to do<br/>
             */
            public ProgressBar(long end){
                actualState = 0L;
                endState = end;
                jump = 50.0/(double)endState;
            }

            /**
             * The most important method in this class.<br/>
             * It draws a progress bar overriding an existing one (thanks to '\r').<br/>
             */
            public void draw(){
                System.out.print("\r Scanning ["); //overriding an existing progress bar and starting drawing
                int howManyJumps = (int)(actualState*jump); //represents a number of "=" signs to draw
                for (int i = 0; i < howManyJumps; i++) //drawing "="
                {
                    System.out.print("=");
                }
                for (int i = howManyJumps; i < 50; i++){ //drawing " "
                    System.out.print(" ");
                }
                System.out.print("]\tScanned " + actualState + "/" + endState); //the end of the bar and information how many steps have been done (actualState) of ('/') full steps' number (endState)
            }
            /**
             * Default update method.<br/>
             * It updates actualState by incrementing it.<br/>
             * Draws an updated progress bar using the draw method.<br/>
             */
            public synchronized void update(){
                actualState++;
                if(actualState%100 == 0 || actualState == endState)
                    draw();
            }
            /**
             * It updates actualState by increasing it of a number given in the parameter.<br/>
             * Draws an updated progress bar using the draw method.<br/>
             * @param n increase of actualState
             */
            public void update(long n){
                actualState += n;
                draw();
            }
    }
}
