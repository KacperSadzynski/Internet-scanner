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
 * SNMPScanner class inherits from the IPv4Addresses class and Runnable interface<br/>
 * By default, this class is being executed by a thread<br/>
 * It scans all public IPv4 addresses limited by BEGIN, END variables<br/>
 * If the amplification of the sent packet is big enough it print out the IP of this server<br/>
 * If the toFile flag is TRUE it writes to SNMP_Vulnerable.txt file output as well<br/>
 * <b/>This class uses snmp4j package</b><br/>
 * Instance Variables:
 * static final int SNMP_SERVER_PORT - represents SNMP server port, set on 161
 * @see IPv4Addresses
 */
public class SNMPScanner extends IPv4Addresses implements Runnable{
    private static final int SNMP_SERVER_PORT = 161;
    private static boolean isYourFirstTime = true;
    private PDU pdu;
    private int pduSize;
    /**
     * Constructor<br/>
     * It removes file SNMP_Vulnerable.txt if exists to avoid appending new output to the old one<br/>
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     */
    public SNMPScanner(int begin, int end){
        amplification = 1;
        packetType = "SNMP";
        fileName = "SNMP_Vulnerable.txt";
        this.BEGIN=begin;
        this.END=end;
        pdu = new PDU();
        pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,1})));
        pduSize = pdu.getBERLength();
        if(isYourFirstTime){
            isYourFirstTime = fileManager(isYourFirstTime, pduSize);
        }
    }

    /**
     * Scans IPv4 addresses pool limited by BEGIN, END variables<br/>
     * This method is being executed by a thread only<br/>
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
     * Creates a socket with UDP transport protocol<br/>
     * Sends a query to a specific IP address, then waits a limited time for an answer<br/>
     * If an answer was received it checks its length<br/>
     * When conditions were met, method prints out the message and write to File if toFile flag equals TRUE<br/>
     * @param serverAddress represents an IP address on which method sends a query
     * @throws IOException
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
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            PDU response = respEvent.getResponse();
            if (response != null) {
                vulnerability(response.getBERLength(), pduSize, serverAddress.toString(), "UDP");
            }
        } catch (Exception ignore) { }
        finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ignore) { }
            }
        }
    }
}
