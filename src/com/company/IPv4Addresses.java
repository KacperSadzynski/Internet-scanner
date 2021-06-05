package com.company;

import java.io.*;
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
    protected byte[] messageUdp;
    protected int amplification;
    protected String messageTCP;
    protected int messageTCPSize;
    public static ProgressBar pb;
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
                if (i == 0 || i == 10 || i == 127){
                    continue;
                }
                for (int j = 0; j < 256; j++) {
                    rawIPList[1] = j;
                    //System.out.println(i + "." + j + ".0.0 reached");
                    for (int k = 0; k < 256; k++) {
                        //System.out.println(i + "." + j + "." + k + ".0 reached");
                        rawIPList[2] = k;
                        for (int l = 0; l < 256; l++) {
                            rawIPList[3] = l;
                            String address = rawIPList[0].toString() + "." + rawIPList[1].toString() + "." + rawIPList[2].toString() + "." + rawIPList[3].toString();
                            if (address.equals("255.255.255.255")){
                                pb.update();
                                continue;
                            }
                            //address = "103.150.0.32";
                            //System.out.println(address);
                            InetAddress current = InetAddress.getByName(address);
                            query(current);
                            pb.update();
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
    public void demonstrationScan(InetAddress dest) throws IOException {
        query(dest);
    }
    /**
     * Default constructor
     */
    public IPv4Addresses(){}

    /**
     * Constructor<br/>
     * Used when a user writes -w in args<br/>
     * @param f used to set  toFile variable
     */
    public IPv4Addresses(boolean f, long states){
        toFile = f;
        pb = new ProgressBar(states);
    }

    /**
     * The method that creates the file and appends found results<br/>
     * It is synchronized to avoid sharing the same resources among threats<br/>
     * @param serverAddress used to represent IP address in a string, using toString method
     * @param bytesRead used to write a number of received bytes
     * @throws IOException
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
    protected void vulnerability(int bytesRead, int messageLength, String serverAddress, String protocol){
        if (bytesRead >= messageLength * amplification) {
            if (toFile) {
                writeToFile(serverAddress, bytesRead, protocol);
            }
            System.out.println("\r" + packetType + " IP address " + serverAddress + "  \t" + messageLength + " bytes sent " + bytesRead + " bytes received with " + protocol);
            pb.draw();
        }
    }
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

    public class ProgressBar extends Thread{
            private long endState;
            private long actualState;
            private double jump;
            public ProgressBar(){
                actualState = 0L;
                endState = 100L;
                jump = 2;
            }
            public ProgressBar(long end){
                actualState = 0L;
                endState = end;
                jump = 50.0/(double)endState;
            }
            public void draw(){
                System.out.print("\r Scanning [");
                int howManyJumps = (int)(actualState*jump);
                for (int i = 0; i < howManyJumps; i++)
                {
                    System.out.print("=");
                }
                for (int i = howManyJumps; i < 50; i++){
                    System.out.print(" ");
                }
                System.out.print("]");
            }
            public synchronized void update(){
                actualState++;
                draw();
            }
            public void update(long n){
                actualState += n;
                draw();
            }
            public void updateTo(long n){
                //TODO kontrola poprawności
                actualState = n;
                draw();
            }
        }
}
