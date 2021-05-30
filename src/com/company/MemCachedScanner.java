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

    private static final int MEMCACHED_SERVER_PORT = 11211;
    private static boolean isYourFirstTime = true;
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
     * Builds a MemCached packet<br/>
     * build packet is being saved to memCachedFrame, which is a byte array<br/>
     *
     * @throws IOException
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
        vulnerability(sendUdpPacket(serverAddress,MEMCACHED_SERVER_PORT, 40), messageUdp.length, serverAddress.toString(), "UDP");
    }
    private void queryTCP(InetAddress serverAddress){
        Socket tcpSocket = null;
        BufferedReader reader = null;
        try{
            tcpSocket = new Socket(serverAddress, MEMCACHED_SERVER_PORT);
            Writer out = new OutputStreamWriter(tcpSocket.getOutputStream(), "ASCII");
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream(), "ASCII"));
            out.write(messageTCP);
            out.flush();
            reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            String buffer = null;
            String received = "";
            int i=0;
            //reading message by lines while line is not empty
            while ((buffer = reader.readLine()) != null){
                received += buffer;
                //System.out.println(buffer);
                if(buffer.equals("END")) break;
            }
            //System.out.println(received);
            int responseSize = received.getBytes().length;
            //System.out.println(responseSize);
            reader.close();
            tcpSocket.close();
            
            vulnerability(responseSize, messageTCPSize, serverAddress.toString(), "TCP");
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
