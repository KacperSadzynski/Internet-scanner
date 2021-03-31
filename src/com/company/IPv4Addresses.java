package com.company;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class IPv4Addresses {

    public void scan() throws UnknownHostException, SocketException {
        Integer[] rawIPList = new Integer[] {0, 0, 0, 0};


        /** Generating all IPv4 addresses **/
        for(int i=0; i<255;i++){
            rawIPList[0]=i;
            for(int j=0; j<255;j++){
                rawIPList[1]=j;
                for(int k=0; k<255;k++){
                    rawIPList[2]=k;
                    for(int l=0; l<255;l++){
                        rawIPList[3]=l;
                        String address = rawIPList[0].toString()+"."+rawIPList[1].toString()+"."+rawIPList[2].toString()+"."+rawIPList[3].toString();
                        //System.out.println(address);
                        InetAddress current = InetAddress.getByName(address);
                        query(current);
                    }
                }
            }
        }
    }

    public void query(InetAddress dest) throws SocketException {

    }
}
