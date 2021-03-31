package com.company;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class IPv4Addresses {

    private final static List<InetAddress> IPv4List = new ArrayList<>();

    public IPv4Addresses() throws UnknownHostException {
        Byte[] rawIPList = new Byte[] {0, 0, 0, 0};
        /** Generating all IPv4 addresses **/
        for (int j = 3; j >= 0; j--) {
            for (int i = 0; i < 8; i++) {
                rawIPList[j] = (byte)i;
                InetAddress temp = InetAddress.getByAddress(new byte[] {rawIPList[0], rawIPList[1], rawIPList[2], rawIPList[3]});
                System.out.println(temp.toString());
                IPv4List.add(temp);
            }
        }
        
    }

    public static List<InetAddress> getIPv4List() {
        return IPv4List;
    }
}
