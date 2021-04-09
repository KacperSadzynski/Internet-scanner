import snmp.*;
import java.net.*;



public class SNMPMultithreadTest
					implements Runnable
{

    SNMPv1CommunicationInterface comInterface;
    
    public class RetrieveThread extends Thread
    {
        public int i;
        
        public RetrieveThread(Runnable r, int i)
        {
            super(r);
            this.i = i;
        }
        
    }
    
    
    public static void main(String args[]) 
    {
        SNMPMultithreadTest app = new SNMPMultithreadTest();
    }
    
    
    public SNMPMultithreadTest() 
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
            
            comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);
            
            comInterface.setSocketTimeout(500);
            
            for (int i = 0; i < 1000; i++)
            {
                RetrieveThread retrievalThread = new RetrieveThread(this, i);
                retrievalThread.start();
            }
            
            
        }
        catch(Exception e)
        {
            System.out.println("Exception during SNMP operation:  " + e + "\n");
        }
        
    }
    
    
    public void run()
    {
        int threadIndex = ((RetrieveThread)Thread.currentThread()).i;
        
        while (true)
        {
            try
	        {
	
	            // now send an SNMP GET request to retrieve the value of the SNMP variable
	            // corresponding to OID 1.3.6.1.2.1.2.1.0; this is the OID corresponding to
	            // the device identifying string, and the type is thus SNMPOctetString
	            String itemID = "1.3.6.1.2.1.1.1.0";
	            
	            System.out.println("Thread " + threadIndex + ": Retrieving value corresponding to OID " + itemID);
	            
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
	            System.out.println("Thread " + threadIndex + ": Retrieved value: type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
	            
	                
	        }
	        catch(Exception e)
	        {
	            System.out.println("Exception during SNMP operation:  " + e + "\n");
	        }
	        
        }
        
    }

}