package com.company;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.net.InetAddress;

public class SNMPScanner extends IPv4Addresses {
    public static final int SNMP_SERVER_PORT = 161;

    public void doStuff() throws IOException {
            scan();

    }

    @Override
    public void query(InetAddress serverAddress) throws IOException {

         Address address = GenericAddress.parse("udp:" + serverAddress + "/" + SNMP_SERVER_PORT);
         CommunityTarget target = new CommunityTarget();
         target.setCommunity(new OctetString("public"));
         target.setAddress(address);
         target.setVersion(SnmpConstants.version2c);
         target.setTimeout(20);
         target.setRetries(3);
         Snmp snmp = null;
         try {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,1})));
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            System.out.println(pdu.getBERLength() + " bytes sent."); //na potrzeby testów jak narazie :)
            System.out.println("PeerAddress:" + respEvent.getPeerAddress());
            PDU response = respEvent.getResponse();

            if (response != null) {
                System.out.println(response.getBERLength() + " bytes received");
                for (int i = 0; i < response.size(); i++) {
                    VariableBinding vb = response.get(i);
                    System.out.println(vb.getOid() + " = " + vb.getVariable());
                }

            }
        } catch (Exception e) {}
        finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }
        }

    }
}
