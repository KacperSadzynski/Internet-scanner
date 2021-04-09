
import snmp.*;
import java.math.*;
import java.net.*;



public class SNMPSample
{

    public static void main(String args[]) 
    {

        try
        {

            // create a communications interface to a remote SNMP-capable device;
            // need to provide the remote host's InetAddress and the community
            // name for the device; in addition, need to  supply the version number
            // for the SNMP messages to be sent (the value 0 corresponding to SNMP
            // version 1)
            InetAddress hostAddress = InetAddress.getByName("10.0.1.1");
            String community = "public";
            int version = 0;    // SNMPv1
            
            SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);
            
            
            
            // now send an SNMP GET request to retrieve the value of the SNMP variable
            // corresponding to OID 1.3.6.1.2.1.2.1.0; this is the OID corresponding to
            // the device identifying string, and the type is thus SNMPOctetString
            String itemID = "1.3.6.1.2.1.1.1.0";
            
            System.out.println("Retrieving value corresponding to OID " + itemID);
            
            // the getMIBEntry method of the communications interface returns an SNMPVarBindList
            // object; this is essentially a Vector of SNMP (OID,value) pairs. In this case, the
            // returned Vector has just one pair inside it.
            SNMPVarBindList newVars = comInterface.getMIBEntry(itemID);
            
            // extract the (OID,value) pair from the SNMPVarBindList; the pair is just a two-element
            // SNMPSequence
            SNMPSequence pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
            
            // extract the object identifier from the pair; it's the first element in the sequence
            SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
            
            // extract the corresponding value from the pair; it's the second element in the sequence
            SNMPObject snmpValue = pair.getSNMPObjectAt(1);
            
            // print out the String representation of the retrieved value
            System.out.println("Retrieved value: type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
            
            // the retrieved value can be obtained from the SNMPObject using the getValue method;
            // the return type of the method is the generic base class Object, and must be cast to 
            // the appropriate actual Java type; in this case, for an SNMPOctetString, the underlying
            // Java type is a byte array[]
            byte[] javaByteArrayValue = (byte[])snmpValue.getValue();
            
            
            
            // now send an SNMP GET request to retrieve the value of the SNMP variable
            // corresponding to OID 1.3.6.1.2.1.1.3.0; this is the OID corresponding to
            // the uptime of the device, and the return type is thus SNMPTimeTicks
            itemID = "1.3.6.1.2.1.1.3.0";
            
            System.out.println("Retrieving value corresponding to OID " + itemID);
            
            // the getMIBEntry method of the communications interface returns an SNMPVarBindList
            // object; this is essentially a Vector of SNMP (OID,value) pairs. In this case, the
            // returned Vector has just one pair inside it.
            newVars = comInterface.getMIBEntry(itemID);
            
            // extract the (OID,value) pair from the SNMPVarBindList; the pair is just a two-element
            // SNMPSequence
            pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
            
            // extract the object identifier from the pair; it's the first element in the sequence
            snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
            
            // extract the corresponding value from the pair; it's the second element in the sequence
            snmpValue = pair.getSNMPObjectAt(1);
            
            // print out the String representation of the retrieved value
            System.out.println("Retrieved value: type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
            
            // the retrieved value can be obtained from the SNMPObject using the getValue method;
            // the return type of the method is the generic base class Object, and must be cast to 
            // the appropriate actual Java type; in this case, for SNMPTimeTicks, which is a subclass
            // of SNMPInteger, the actual type is BigInteger (which permits arbitrarily large values to 
            // be represented).
            BigInteger javaIntegerValue = (BigInteger)snmpValue.getValue();
            
            
            
            // now send an SNMP GET request to simultaneously retrieve the value of the SNMP variables
            // corresponding to OIDs 1.3.6.1.2.1.1.1.0 to 1.3.6.1.2.1.1.5.0
            String[] itemIDs = {"1.3.6.1.2.1.1.1.0", "1.3.6.1.2.1.1.2.0", "1.3.6.1.2.1.1.3.0", "1.3.6.1.2.1.1.4.0", "1.3.6.1.2.1.1.5.0"};
            
            System.out.println("Retrieving value corresponding to OIDs: ");
            for (int i = 0; i < itemIDs.length; i++)
            {
                System.out.println("  " + itemIDs[i]);
            }
            
            // the getMIBEntry method of the communications interface returns an SNMPVarBindList
            // object; this is essentially a Vector of SNMP (OID,value) pairs. In this case, the
            // returned Vector has several pairs inside it.
            newVars = comInterface.getMIBEntry(itemIDs);
            
            // extract the (OID,value) pairs from the SNMPVarBindList; each pair is just a two-element
            // SNMPSequence
            for (int i = 0; i < newVars.size(); i++)
            {
                pair = (SNMPSequence)(newVars.getSNMPObjectAt(i));
                
                // extract the object identifier from the pair; it's the first element in the sequence
                snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
                
                // extract the corresponding value from the pair; it's the second element in the sequence
                snmpValue = pair.getSNMPObjectAt(1);
                
                // print out the String representation of the retrieved value
                System.out.println("Retrieved value: type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
            }
            
            // now do get-next for the OIDS above; this will return the values for the OIDs following
            // each of the supplied OIDs
            System.out.println("Retrieving values _following_ OIDs: ");
            for (int i = 0; i < itemIDs.length; i++)
            {
                System.out.println("  " + itemIDs[i]);
            }
            
            newVars = comInterface.getNextMIBEntry(itemIDs);
            
            // extract the (OID,value) pairs from the SNMPVarBindList; each pair is just a two-element
            // SNMPSequence
            for (int i = 0; i < newVars.size(); i++)
            {
                pair = (SNMPSequence)(newVars.getSNMPObjectAt(i));
                
                // extract the object identifier from the pair; it's the first element in the sequence
                snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
                
                // extract the corresponding value from the pair; it's the second element in the sequence
                snmpValue = pair.getSNMPObjectAt(1);
                
                // print out the String representation of the retrieved value
                System.out.println("Retrieved value: type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
            }
            
            
            
            // next, retrieve an entire table, and print out the results
            // This uses the simple getMIBTable method, in which a single base OID is supplied,
            // and all of the values of OIDs which start with that base are retrieved, one element
            // at a time - this in effect retrieves the elements column-wise: all the elements in
            // the first column are retrieved first, then all the elements in the second column, etc.
            // This is an artifact of the way the OIDs of the elements in a table are formed in SNMP.
            String baseID = "1.3.6.1.2.1.2.2.1";
            
            System.out.println("Retrieving table corresponding to base OID " + baseID);
            
            SNMPVarBindList tableVars = comInterface.retrieveMIBTable(baseID);
            
            System.out.println("Number of table entries: " + tableVars.size());
            
            // extract the (OID,value) pairs from the SNMPVarBindList; each pair is just a two-element
            // SNMPSequence
            for (int i = 0; i < tableVars.size(); i++)
            {
                pair = (SNMPSequence)(tableVars.getSNMPObjectAt(i));
                
                // extract the object identifier from the pair; it's the first element in the sequence
                snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
                
                // extract the corresponding value from the pair; it's the second element in the sequence
                snmpValue = pair.getSNMPObjectAt(1);
                
                // print out the String representation of the retrieved value
                System.out.println("Retrieved OID: " + snmpOID + ", type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
            }
            
            
            
            // Next, retrieve an entire table, but row-by-row rather than entry-by-entry
            // This uses the multiple getMIBTable method, in which an array of base OIDs is supplied,
            // and sequences of the values of OIDs which start with that base are retrieved. This returns
            // the elements row-wise: all the elements corresponding to the first row, followed by the 
            // elements in the second row, etc. Note also that only elements from the columns supplied 
            // are retrieved; this differs from the previous retrieval, in which all of the table's
            // elements, from all columns, are retrieved. This method is thus more selective in the
            // information retrieved. It is also more efficient, in that all of the elements in a single 
            // row are retrieved with a single get-request - multiple OIDs are requested in each message.
            String[] baseIDs = {"1.3.6.1.2.1.2.2.1.1", "1.3.6.1.2.1.2.2.1.2", "1.3.6.1.2.1.2.2.1.3", "1.3.6.1.2.1.2.2.1.4"};
            
            System.out.println("Retrieving table columns corresponding to base OIDs ");
            for (int i = 0; i < baseIDs.length; i++)
            {
                System.out.println("  " + baseIDs[i]);
            }
            
            tableVars = comInterface.retrieveMIBTable(baseIDs);
            
            System.out.println("Number of table entries: " + tableVars.size());
            
            // extract the (OID,value) pairs from the SNMPVarBindList; each pair is just a two-element
            // SNMPSequence
            for (int i = 0; i < tableVars.size(); i++)
            {
                pair = (SNMPSequence)(tableVars.getSNMPObjectAt(i));
                
                // extract the object identifier from the pair; it's the first element in the sequence
                snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
                
                // extract the corresponding value from the pair; it's the second element in the sequence
                snmpValue = pair.getSNMPObjectAt(1);
                
                // print out the String representation of the retrieved value
                System.out.println("Retrieved OID: " + snmpOID + ", type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
            }
            
            
            
            // now send an SNMP SET request to set the value of the SNMP variable
            // corresponding to OID 1.3.6.1.2.1.1.4.0; this is the OID corresponding to
            // the device contact person, and the type is thus SNMPOctetString;
            // to set a new value, a string is supplied
            itemID = "1.3.6.1.2.1.1.4.0";    
            
            SNMPOctetString newValue = new SNMPOctetString("Jon S");
            
            System.out.println("Setting value corresponding to OID " + itemID);
            System.out.println("New value: " + newValue.toString());
            
            // the setMIBEntry method of the communications interface returns the SNMPVarBindList
            // corresponding to the supplied OID and value
            // This call will probably cause an SNMPSetException to be thrown, since the
            // community name "public" is probably not the read/write password of the device 
            newVars = comInterface.setMIBEntry(itemID, newValue);
            
            
            
            // now send an SNMP SET request to set the values of the SNMP variables
            // corresponding to OID 1.3.6.1.2.1.1.4.0 and 1.3.6.1.2.1.1.5.0; the latter
            // is the OID corresponding to the device name, and the type is thus SNMPOctetString;
            // to set a new value, a string is supplied
            String[] setItemIDs = {"1.3.6.1.2.1.1.4.0", "1.3.6.1.2.1.1.5.0"};    
            
            SNMPOctetString[] newValues = {new SNMPOctetString("Jon"), new SNMPOctetString("Jon's device")};
            
            System.out.println("Setting value corresponding to OIDs " + itemID);
            for (int i = 0; i < setItemIDs.length; i++)
            {
                System.out.println("  " + setItemIDs[i] + ", new values " + newValues[i]);
            }
            
            // the setMIBEntry method of the communications interface returns the SNMPVarBindList
            // corresponding to the supplied OID and value
            // This call will probably cause an SNMPSetException to be thrown, since the
            // community name "public" is probably not the read/write password of the device 
            newVars = comInterface.setMIBEntry(setItemIDs, newValues);
            
            
                
        }
        catch(Exception e)
        {
            System.out.println("Exception during SNMP operation:  " + e + "\n");
        }
        
    }

}