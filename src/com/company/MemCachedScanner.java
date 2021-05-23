package com.company;

import java.io.*;
import java.net.*;

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
public class MemCachedScanner extends IPv4Addresses implements Runnable{

    public static final int MEMCACHED_SERVER_PORT = 11211;
    private byte[] memCachedFrame;

    /**
     * Constructor<br/>
     * It removes file MemCached_Vulnerable.txt if exists to avoid appending new output to the old one<br/>
     * Builds memCachedFrame byte array using the buildPacket() method<br/>
     * @param begin used to set BEGIN variable
     * @param end used to set END variable
     * @throws IOException
     */
    public MemCachedScanner(int begin, int end) throws IOException {
        packetType = "MemCached";
        fileName = "MemCached_Vulnerable.txt";
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
            printWriter.println(memCachedFrame.length + " bytes sent\n");
            printWriter.close();
        }
    }
    /**
     * Builds a MemCached packet<br/>
     * build packet is being saved to memCachedFrame, which is a byte array<br/>
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
    public void run(){
        try {
            scan();
        } catch (InterruptedIOException ex){
            System.out.println("MemCachedThread interrupted");
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
    public void query(InetAddress serverAddress) throws IOException, SocketTimeoutException {

        //DatagramSocket socket = null;
        Socket tcpSocket = null;
        BufferedReader reader = null;
        //Writer writer = null;
        try{
        /*
            socket = new DatagramSocket();

            DatagramPacket memCachedReqPacket = new DatagramPacket(memCachedFrame, memCachedFrame.length, serverAddress, MEMCACHED_SERVER_PORT);
            socket.send(memCachedReqPacket);

            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            //Waiting for a response limited by 40 ms
            socket.setSoTimeout(90);
            socket.receive(packet);
            socket.close();
            try {
                if (packet.getLength() >= memCachedReqPacket.getLength() * 1) {
                    if (toFile) {
                        writeToFile(serverAddress, packet);
                    }
                    System.out.println("MemCached IP address " + serverAddress.toString() + "\t" + memCachedReqPacket.getLength() + " bytes sent " + packet.getLength() + " bytes received");
                }
            }catch(NullPointerException e){}
            */


            //////////////////////TCP
            tcpSocket = new Socket(serverAddress, MEMCACHED_SERVER_PORT);
            OutputStream output = tcpSocket.getOutputStream();
            //writer = new OutputStreamWriter(output);
            output.write(memCachedFrame);
            output.flush();
            System.out.println("Wyslano");
            output.close();
            InputStream input = tcpSocket.getInputStream();
            int bytesRead = 0;
            int bytesToRead = 4096;
            byte[] received = new byte[bytesToRead];
            System.out.println("Przed whilem");
            while (bytesRead < bytesToRead) {
                System.out.println(bytesRead);
                int result = input.read(received, bytesRead, bytesToRead - bytesRead);
                if (result == -1) break; // end of stream
                bytesRead += result;
            }
            System.out.println("wyszlo");
            //BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            //String line = reader.readLine();    // reads a line of text
            if (bytesRead>0)
                System.out.println("MemCached IP address " + serverAddress.toString() + "\t" + memCachedFrame.length + " bytes sent " + bytesRead + " bytes received");
        }
        catch(SocketTimeoutException e) {
            System.out.println("TimeoutException");
        }
        catch(SocketException e) {
            //System.out.println("SocketException");
        }
        catch(SecurityException e) {
            System.out.println("SecurityException");
        }
        catch(UnknownHostException e) {
            System.out.println("UnknownHostException");
        }
        catch(Exception e)
        {
            System.out.println("Inny wyjatek");
        }
        finally {
            //System.out.println("Wyszlo z traja");
            if (tcpSocket != null) {
                try {
                    //socket.close();
                    tcpSocket.close();
                } catch (NullPointerException ex1) {
                    //socket = null;
                    tcpSocket = null;
                }
            }
        }
    }
}
