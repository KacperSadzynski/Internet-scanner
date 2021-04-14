package com.company;

import java.io.IOException;
import java.net.InetAddress;


public class IPv4Addresses {

    protected static boolean toFile;
    static{
    toFile = false;
    }

    protected void scan() throws IOException {
        Integer[] rawIPList = new Integer[] {0, 0, 0, 0};

        /** Generating all IPv4 addresses **/
        for(int i=1; i<256;i++){
            rawIPList[0]=i;
            for(int j=0; j<256;j++){
                rawIPList[1]=j;
                for(int k=0; k<256;k++){
                    rawIPList[2]=k;
                    for(int l=0; l<256;l++){
                        rawIPList[3]=l;
                        String address = rawIPList[0].toString()+"."+rawIPList[1].toString()+"."+rawIPList[2].toString()+"."+rawIPList[3].toString();
                        if (address.equals("0.0.0.0")) {
                            continue;
                        }
                        System.out.println(address);
                        //address = "8.8.8.8";
                        InetAddress current = InetAddress.getByName(address);
                        query(current);
                    }
                }
            }
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
