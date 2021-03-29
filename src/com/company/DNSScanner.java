package com.company;

import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class DNSScanner {
    //private DatagramSocket datagramSocket = new DatagramSocket(53);
    private Socket socket = new Socket("127.0.0.1", 53);
    private List<InetAddress> ipList = new ArrayList<>();

    private Byte[] rawIPList = new Byte[] {0, 0, 0, 0};

    private void doStuff() throws IOException {


        for (int j = 3; j >= 0; j--)
        {
            for (int i = 0; i < 8; i++)
            {
                rawIPList[j] = (byte)i;
                InetAddress temp = InetAddress.getByAddress(new byte[] {rawIPList[0], rawIPList[1], rawIPList[2], rawIPList[3]});
                ipList.add(temp);
            }
        }

        for (InetAddress addr : ipList)
        {
            //TODO DNS QUERY
            DatagramSocket datagramSocket = new DatagramSocket(53, addr);
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, 53);
            //DatagramPacket
            //https://docs.oracle.com/javase/7/docs/api/java/net/DatagramPacket.html


        }
    }





    public DNSScanner() throws IOException {
        System.out.println(":(");
    }
}
