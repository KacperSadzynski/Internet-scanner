package com.company;

import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DNSScanner class creates a socket bound to port 53 and a UDP packet for DNS protocol
 * It scans all public IPv4 addresses by sending to all available DNS servers query and waiting for respond
 * If the amplification of the sent packet is big enough it print out the IP of this server
 **/
public class DNSScanner extends IPv4Addresses {
    //private DatagramSocket datagramSocket = new DatagramSocket(53);
    private Socket socket = new Socket("127.0.0.1", 53); //?


    private void doStuff() throws IOException {
        scan();
    }

    @Override
    public void query(InetAddress dest) throws SocketException {
        //TODO DNS QUERY
        DatagramSocket datagramSocket = new DatagramSocket(53, dest);
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length, dest, 53);
        //DatagramPacket
        //https://docs.oracle.com/javase/7/docs/api/java/net/DatagramPacket.html
    }





    public DNSScanner() throws IOException {
        System.out.println("BibleThump");// +1
    }
}
