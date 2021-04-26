package com.company;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * SNMPScanner class inherits from the IPv4Addresses class and Runnable interface
 * By default, this class is being executed by a thread
 * It scans all public IPv4 addresses limited by BEGIN, END variables
 * If the amplification of the sent packet is big enough it print out the IP of this server
 * If the toFile flag is TRUE it writes to SNMP_Vulnerable.txt file output as well
 * This class uses snmp4j package
 * Instance Variables:
 * static final int SNMP_SERVER_PORT - represents SNMP server port, set on 161
 */
public class SNMPScanner extends IPv4Addresses implements Runnable{
    public static final int SNMP_SERVER_PORT = 161;

    /**
     * Constructor
     * It removes file SNMP_Vulnerable.txt if exists to avoid appending new output to the old one
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     */
    public SNMPScanner(int begin, int end){
        File file = new File("SNMP_Vulnerable.txt");
        if(file.exists()){
            file.delete();
        }
        this.BEGIN=begin;
        this.END=end;
    }

    /**
     * Scans IPv4 addresses pool limited by BEGIN, END variables
     * This method is being executed by a thread only
     */
    public void run() {
        try {
            scan();
        } catch (InterruptedIOException ex){
            System.out.println("SNMPThread interrupted");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     /**
     * The method that creates the file "SNMP_Vulnerable.txt" and appends found results
     * It is synchronized to avoid sharing the same resources among threats
     * @param serverAddress used to represent IP address in a string, using toString method
     * @param response used to write a number of received bytes, using getBERLength method
     * @throws IOException
     * @see IOException
     */
    public synchronized void writeToFile(InetAddress serverAddress, PDU response) throws IOException {
        FileWriter fileWriter = new FileWriter("SNMP_Vulnerable.txt", true); //Set true for append mode
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("SNMP IP address " + serverAddress.toString() + " " + response.getBERLength() +" bytes received");
        printWriter.close();
    }

    /**
     * Creates a socket with UDP transport protocol
     * Sends a query to a specific IP address, then waits a limited time for an answer
     * If an answer was received it checks its length
     * When conditions were met, method prints out the message and write to File if toFile flag equals TRUE
     * @param serverAddress
     * @throws IOException
     * @see IOException
     */
    @Override
    public void query(InetAddress serverAddress) throws IOException {

         Address address = GenericAddress.parse("udp:" + serverAddress + "/" + SNMP_SERVER_PORT);
         CommunityTarget target = new CommunityTarget();
         target.setCommunity(new OctetString("public"));
         target.setAddress(address);
         target.setVersion(SnmpConstants.version2c);
         target.setTimeout(50);
         target.setRetries(0);
         Snmp snmp = null;
         try {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,1})));
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            //System.out.println(pdu.getBERLength() + " bytes sent."); //na potrzeby testów jak narazie :)
            //System.out.println("PeerAddress:" + respEvent.getPeerAddress());
            PDU response = respEvent.getResponse();
            if (response != null) {
                if(pdu.getBERLength()<=response.getBERLength()){
                    System.out.println("SNMP IP address " + serverAddress.toString() + " " + response.getBERLength() + " bytes received");
                    if(toFile) {
                        writeToFile(serverAddress, response);
                    }
                }
            }
        } catch (Exception e) {}
        finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }
        }
    }
}
