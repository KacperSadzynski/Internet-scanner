package com.company;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;

public class NTPScanner extends IPv4Addresses implements Runnable{

    public static final int NTP_SERVER_PORT = 123;
    private byte[] ntpFrame;

    public NTPScanner(int begin, int end) throws IOException {
        this.BEGIN = begin;
        this.END = end;
        buildPacket();
    }
    protected void buildPacket() throws IOException { // 0 2 6 0 0 0 2
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        //LI-VN-Mode-R-E-M-Opcode-Sequence 00 010 110 0 0 0 00010 0000000000000000
        dos.writeShort(0x16020000);
        dos.writeShort(0x00000000);
        dos.writeShort(0x00000000);
        dos.writeShort(0x00000000);
        dos.writeShort(0x00000000);
        dos.writeShort(0x00000000);
        dos.writeShort(0x00000000);
        dos.writeShort(0x00000000);
        ntpFrame = baos.toByteArray();
    }

    @Override
    public void run(){
        try {
            scan();
        } catch (InterruptedIOException ex){
            System.out.println("NTPThread interrupted");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void query(InetAddress serverAddress) throws IOException {
    DatagramSocket socket = null;
            try{
                socket = new DatagramSocket();

                DatagramPacket dnsReqPacket = new DatagramPacket(ntpFrame, ntpFrame.length, serverAddress, NTP_SERVER_PORT);
                socket.send(dnsReqPacket);

                byte[] buf = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                //Waiting for a response limited by 40 ms
                socket.setSoTimeout(60);
                socket.receive(packet);
                socket.close();
                try {
                    if (packet.getLength() >= ntpFrame.length * 1) {
                        if (toFile) {
                            //writeToFile(serverAddress, packet);
                        }
                        System.out.println("NTP IP address " + serverAddress.toString() + " " + packet.getLength() + " bytes received");
                    }
                }catch(NullPointerException e){}
            }
            catch(SocketTimeoutException e) {
                System.out.println("TimeoutException");
            }
            catch(SocketException e) {
                System.out.println("SocketException");
            }
            finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (NullPointerException ex1) {
                        socket = null;
                    }
                }
            }
    }

}
