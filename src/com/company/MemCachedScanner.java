package com.company;

import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

/**
 * MemCachedScanner class inherits from the IPv4Addresses class and Runnable interface<br/>
 * By default, this class is being executed by a thread<br/>
 * It builds a MemCached packet that is sent by query(), then a thread execute run() method<br/>
 * It scans all public IPv4 addresses limited by BEGIN, END variables<br/>
 * If the amplification of the sent packet is big enough it print out the IP of this server<br/>
 * If the toFile flag is TRUE it writes to MemCached_Vulnerable.txt file output as well<br/>
 * Instance Variables:<br/>
 * static final int MEMCACHED_SERVER_PORT - represents MemCached server port, set on 11211<br/>
 * byte[] memCachedFrame - byte array that represents MemCached packet<br/>
 * @see IPv4Addresses
 */
public class MemCachedScanner extends IPv4Addresses implements Runnable {

    public static final int MEMCACHED_SERVER_PORT = 11211;
    private byte[] memCachedFrame;

    /**
     * Constructor<br/>
     * It removes file MemCached_Vulnerable.txt if exists to avoid appending new output to the old one<br/>
     * Builds memCachedFrame byte array using the buildPacket() method<br/>
     *
     * @param begin used to set BEGIN variable
     * @param end   used to set END variable
     * @throws IOException
     */
    public MemCachedScanner(int begin, int end) throws IOException {
        packetType = "MemCached";
        fileName = "MemCached_Vulnerable.txt";
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
        if (toFile) {
            FileWriter fileWriter = new FileWriter(fileName, true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(memCachedFrame.length + " bytes sent\n");
            printWriter.close();
        }
    }

    /**
     * Builds a MemCached packet<br/>
     * build packet is being saved to memCachedFrame, which is a byte array<br/>
     *
     * @throws IOException
     */
    protected void buildPacket() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        //Magic
        dos.writeShort(0x80);
        //Opcode
        dos.writeShort(0x10);
        //Key length
        dos.writeShort(0x0000);
        //Extra length
        dos.writeShort(0x00);
        //Data type
        dos.writeShort(0x00);
        //Reserved
        dos.writeShort(0x0000);
        //Total body
        dos.writeShort(0x00000000);
        //Opaque
        dos.writeShort(0x00000000);
        //CAS
        dos.writeShort(0x0000000000000000);

        //Extras, Key, Values - None
        memCachedFrame = baos.toByteArray();
    }

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

    private void queryUDP(InetAddress serverAddress){
        DatagramSocket udpSocket = null;
        try {
            udpSocket = new DatagramSocket();

            DatagramPacket memCachedReqPacket = new DatagramPacket(memCachedFrame, memCachedFrame.length, serverAddress, MEMCACHED_SERVER_PORT);
            udpSocket.send(memCachedReqPacket);

            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            //Waiting for a response limited by 40 ms
            udpSocket.setSoTimeout(40);
            udpSocket.receive(packet);
            udpSocket.close();
            try {
                if (packet.getLength() >= memCachedFrame.length * 1) {
                    if (toFile) {
                        writeToFile(serverAddress, packet);
                    }
                        System.out.println("MemCached IP address " + serverAddress.toString() + "\t" + memCachedReqPacket.getLength() + " bytes sent " + packet.getLength() + " bytes received");
                    }
            }catch(NullPointerException e){}
        } catch (SocketTimeoutException e) {
            //System.out.println("IP address " + serverAddress.toString() + " SocketTimeoutException " + e.getMessage());
            //System.err.println(e.getMessage());
        } catch (UnknownHostException e) {
            //System.out.println("IP address " + serverAddress.toString() + " UnknownHostException " + e.getMessage());
            //System.err.println(e.getMessage());
        } catch (IOException e) {
            //System.out.println("IP address " + serverAddress.toString() + " IOException " + e.getMessage());
            //System.err.println();
        } catch (Exception e) {
            //System.out.println("IP address " + serverAddress.toString() + " Inny wyjatek " + e.getMessage());
            //System.err.println(e.getMessage());
        } finally {
            if (udpSocket != null) {
                try {
                    udpSocket.close();
                } catch (NullPointerException ex1) {
                    //ignore
                }
            }
        }
    }
    private void queryTCP(InetAddress serverAddress){
        Socket tcpSocket = null;
        BufferedReader reader = null;
        try{
            String message = "stats\r\n";
            int querySize = message.getBytes().length;
            tcpSocket = new Socket(serverAddress, MEMCACHED_SERVER_PORT);
            Writer out = new OutputStreamWriter(tcpSocket.getOutputStream(), "ASCII");
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream(), "ASCII"));
            out.write(message);
            out.flush();
            reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            String buffer = null;
            String received = null;
            //reading message by lines while line is not empty
            while ((buffer = reader.readLine()) != null){
                received += buffer;
            }
            //System.out.println(received);
            int responseSize = received.getBytes().length;
            //System.out.println(responseSize);
            reader.close();
            tcpSocket.close();
            try {
                if (responseSize >= querySize * 1) {
                    if (toFile) {
                        //writeToFile(serverAddress, packet);
                    }
                        System.out.println("MemCached IP address " + serverAddress.toString() + "\t" + querySize + " bytes sent " + responseSize + " bytes received");
                }
            } catch(NullPointerException e){}
        }catch (BindException e) {
            //System.out.println("IP address " + serverAddress.toString() + " BindException " + e.getMessage());
            //System.err.println(e.getMessage());
        } catch (ConnectException e) {
            //System.out.println("IP address " + serverAddress.toString() + " ConnectException " + e.getMessage());
            //System.err.println(e.getMessage());
        } catch (NoRouteToHostException e) {
            //System.out.println("IP address " + serverAddress.toString() + " NoRouteToHostException " + e.getMessage());
            //System.err.println(e.getMessage());
        } catch (SocketTimeoutException e) {
            //System.out.println("IP address " + serverAddress.toString() + " SocketTimeoutException " + e.getMessage());
            //System.err.println(e.getMessage());
        } catch (ProtocolException e) {
            //System.out.println("IP address " + serverAddress.toString() + " ProtocolException " + e.getMessage());
            //System.err.println(e.getMessage());
        } catch (SecurityException e) {
            //System.out.println("IP address " + serverAddress.toString() + " SecurityException " + e.getMessage());
            //System.err.println(e.getMessage());
        } catch (UnknownHostException e) {
            //System.out.println("IP address " + serverAddress.toString() + " UnknownHostException " + e.getMessage());
             //System.err.println(e.getMessage());
        } catch (IOException e) {
            //System.out.println("IP address " + serverAddress.toString() + " IOException " + e.getMessage());
            //System.err.println();
        } catch (Exception e) {
            //System.out.println("IP address " + serverAddress.toString() + " Inny wyjatek " + e.getMessage());
            //System.err.println(e.getMessage());
        } finally {
            if (tcpSocket != null) {
                try {
                    tcpSocket.close();
                } catch (IOException e) {
                    //ignore
                } catch (NullPointerException ex1) {
                    tcpSocket = null;
                }
            }
        }
    }
    /**
     * Calls methods quetyUDP(InetAddress) and quetyTCP(InetAddress)<br/>
     * Scans address given as parameter firstly with UDP and later with TCP<br/>
     * @param serverAddress represents an IP address on which method sends a query
     */
    @Override
    public void query(InetAddress serverAddress) {
        queryUDP(serverAddress);
        queryTCP(serverAddress);
    }
}
