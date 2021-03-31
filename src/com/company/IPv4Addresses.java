package com.company;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class IPv4Addresses {

    private final static List<InetAddress> IPv4List = new ArrayList<>();

    public IPv4Addresses() throws UnknownHostException {
        Integer[] rawIPList = new Integer[] {0, 0, 0, 0};
        /** Generating all IPv4 addresses **/
        for (int j = 3; j >= 0; j--) {
            for (int i = 0; i <= 255; i++) {
                rawIPList[j] = i;
                String address = rawIPList[0].toString()+"."+ rawIPList[1].toString()+ "." + rawIPList[2].toString()+"."+rawIPList[3].toString();
                InetAddress temp = InetAddress.getByName(address);
                System.out.println(temp.toString());
                IPv4List.add(temp);
            }
        }
    }

    public static List<InetAddress> getIPv4List() {
        return IPv4List;
    }
}
