package com.company;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;


public class IPv4Addresses {

    protected static boolean toFile;
    static{
    toFile = false;
    }
    protected int BEGIN;
    protected int END;

    protected void scan() throws IOException {
        Integer[] rawIPList = new Integer[] {0, 0, 0, 0};

        /** Generating all IPv4 addresses **/
        try {
            for (int i = BEGIN; i < END; i++) {
                rawIPList[0] = i;
                if (i == 0 || i == 10 || i == 127)
                    continue;
                for (int j = 0; j < 256; j++) {
                    rawIPList[1] = j;
                    //System.out.println(i + "." + j + ".0.0 reached");
                    for (int k = 0; k < 256; k++) {
                        System.out.println(i + "." + j + "." + k + ".0 reached");
                        rawIPList[2] = k;
                        for (int l = 0; l < 256; l++) {
                            rawIPList[3] = l;
                            String address = rawIPList[0].toString() + "." + rawIPList[1].toString() + "." + rawIPList[2].toString() + "." + rawIPList[3].toString();
                            if (address.equals("255.255.255.255"))
                                continue;
                            //address = "8.8.8.8";
                            InetAddress current = InetAddress.getByName(address);
                            query(current);
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

    protected void query(InetAddress dest) throws IOException {
    }

    public IPv4Addresses(){
    }

    public IPv4Addresses(boolean f){
        toFile = f;
    }
}
