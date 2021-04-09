package source;/*
 * SNMP Agent Test
 *
 * Copyright (C) 2004, Jonathan Sevy <jsevy@mcs.drexel.edu>
 *
 * This is free software. Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation 
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products 
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */



import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import java.awt.event.*;
import java.io.*;
import snmp.*;




public class SNMPAgentTest extends JFrame
                    implements ActionListener, SNMPRequestListener, Runnable
{
    
    JButton clearButton;
    JTextArea messagesArea;
    JScrollPane messagesScroll;
    JLabel authorLabel;
    
    MenuBar theMenubar;
    Menu fileMenu;
    MenuItem aboutItem, quitItem, setReportFileItem;
    
    SNMPv1AgentInterface agentInterface;
    String communityName = "public";
    
    SNMPOctetString storedSNMPValue;
    
    PipedReader errorReader;
    PipedWriter errorWriter;
    Thread readerThread;
    
    boolean haveReportFile = false;
    FileWriter reportFileWriter;
    
    
    
    
    private class WindowCloseAdapter extends WindowAdapter
    { 
        public void windowClosing(WindowEvent e)
        {
            readerThread.interrupt();
            System.exit(0);
        }
    }
    
            
    
    public SNMPAgentTest() 
    {
        setUpDisplay();
        
        storedSNMPValue = new SNMPOctetString("Original value");
        
        try
        {
            errorReader = new PipedReader();
            errorWriter = new PipedWriter(errorReader);
            
            readerThread = new Thread(this);
            readerThread.start();
            
            int version = 0;    // SNMPv1
            
            agentInterface = new SNMPv1AgentInterface(version, new PrintWriter(errorWriter));
            agentInterface.addRequestListener(this);
            agentInterface.setReceiveBufferSize(5120);
            agentInterface.startReceiving();
            
        }
        catch(Exception e)
        {
            messagesArea.append("Problem starting Agent Test: " + e.toString() + "\n");
        }
    }
    
    
    
    private void setUpDisplay()
    {
        
        this.setTitle("SNMP Agent Test");
        
        this.getRootPane().setBorder(new BevelBorder(BevelBorder.RAISED));
        
        // set fonts to smaller-than-normal size, for compaction!
        /*
        FontUIResource appFont = new FontUIResource("SansSerif", Font.PLAIN, 10);
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        Enumeration keys = defaults.keys();
        
        while (keys.hasMoreElements())
        {
            String nextKey = (String)(keys.nextElement());
            if ((nextKey.indexOf("font") > -1) || (nextKey.indexOf("Font") > -1))
            {
                UIManager.put(nextKey, appFont);
            }
        }
        */
        
        // add WindowCloseAdapter to catch window close-box closings
        addWindowListener(new WindowCloseAdapter());

        
        theMenubar = new MenuBar();
        this.setMenuBar(theMenubar);
        fileMenu = new Menu("File");
        
        aboutItem = new MenuItem("About...");
        aboutItem.setActionCommand("about");
        aboutItem.addActionListener(this);
        fileMenu.add(aboutItem);
        
        setReportFileItem = new MenuItem("Set report file...");
        setReportFileItem.setActionCommand("set report file");
        setReportFileItem.addActionListener(this);
        fileMenu.add(setReportFileItem);
        
        fileMenu.addSeparator();
        
        quitItem = new MenuItem("Quit");
        quitItem.setActionCommand("quit");
        quitItem.addActionListener(this);
        fileMenu.add(quitItem);
        
        theMenubar.add(fileMenu);
        
        clearButton = new JButton("Clear messages");
        clearButton.setActionCommand("clear messages");
        clearButton.addActionListener(this);
        
        
        authorLabel = new JLabel(" Version 1.0        J. Sevy, August 2003 ");
        authorLabel.setFont(new Font("SansSerif", Font.ITALIC, 8));
            
        
        messagesArea = new JTextArea(10,60);
        messagesScroll = new JScrollPane(messagesArea);
        
        
        // now set up display
        
        
        // set params for layout manager
        GridBagLayout  theLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.ipadx = 0;
        c.ipady = 0;
        c.insets = new Insets(2,2,2,2);
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0;
        c.weighty = 0;
        
        
        JPanel messagesPanel = new JPanel();
        messagesPanel.setLayout(theLayout);
        
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        JLabel messagesLabel = new JLabel("Received requests:");
        theLayout.setConstraints(messagesLabel, c);
        messagesPanel.add(messagesLabel);
        
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        theLayout.setConstraints(clearButton, c);
        messagesPanel.add(clearButton);
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weightx = .5;
        c.weighty = .5;
        c.anchor = GridBagConstraints.CENTER;
        theLayout.setConstraints(messagesScroll, c);
        messagesPanel.add(messagesScroll);
        
        
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        
        
        this.getContentPane().setLayout(theLayout);
        
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = .5;
        c.weighty = .5;
        theLayout.setConstraints(messagesPanel, c);
        this.getContentPane().add(messagesPanel);
        
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0;
        theLayout.setConstraints(authorLabel, c);
        this.getContentPane().add(authorLabel);
        
        
    }
    
    
    
    
    
    public void actionPerformed(ActionEvent theEvent)
    // respond to button pushes, menu selections
    {
        String command = theEvent.getActionCommand();
        
    
        if (command == "quit")
        {
            readerThread.interrupt();
            System.exit(0);
        }
        
        
        if (command == "clear messages")
        {
            messagesArea.setText("");
        }
        
        
        if (command == "about")
        {
            //AboutDialog aboutDialog = new AboutDialog(this);
        }
        
        
        if (command == "set report file")
        {
            try
            {
                FileDialog fd = new FileDialog(this, "Select report file...", FileDialog.LOAD);
                fd.show();
                
                if (fd.getFile() != null)
                {
                    File newFile = new File(fd.getDirectory(), fd.getFile());
                    
                    reportFileWriter = new FileWriter(newFile);
                    
                    try
                    {
                        reportFileWriter.write("SNMP Agent Report File\n");
                        reportFileWriter.write("Date: " + (new Date()).toString() + "\n\n");
                        reportFileWriter.flush();
                    }
                    catch (IOException e)
                    {
                        messagesArea.append("Unable to write message to report file\n\n");
                    }
                    
                    haveReportFile = true;
                }
                
            }
            catch (Exception e)
            {
                messagesArea.append("Error opening report file: " + e.getMessage() + "\n");
            }
        }
        
    }
    
    
    // Tried making it synchronized so error and "normal" messages won't be interleaved,
    // but this led to hangs during testing; so guess I'll live with possible message interleaving.
    private void writeMessage(String message)
    {
        messagesArea.append(message);
        
        // also write to report file, if any
        if (haveReportFile)
        {
            try
            {
                reportFileWriter.write(message);
                reportFileWriter.flush();
            }
            catch (IOException e)
            {
                messagesArea.append("Unable to write message to report file\n\n");
            }
        }
    }
            
    
    
    public SNMPSequence processRequest(SNMPPDU pdu, String communityName) 
        throws SNMPGetException, SNMPSetException
    {
        writeMessage("Got pdu:\n");
        
        writeMessage("  community name:     " + communityName + "\n");
        writeMessage("  request ID:         " + pdu.getRequestID() + "\n");
        writeMessage("  pdu type:           ");
        byte pduType = pdu.getPDUType();
        switch (pduType)
        {
            case SNMPBERCodec.SNMPGETREQUEST:
            {
                writeMessage("SNMPGETREQUEST\n");
                break;
            }
            
            case SNMPBERCodec.SNMPGETNEXTREQUEST:
            {
                writeMessage("SNMPGETNEXTREQUEST\n");
                break;
            }
            
            case SNMPBERCodec.SNMPSETREQUEST:
            {
                writeMessage("SNMPSETREQUEST\n");
                break;
            }
            
            case SNMPBERCodec.SNMPGETRESPONSE:
            {
                writeMessage("SNMPGETRESPONSE\n");
                break;
            }
            
            case SNMPBERCodec.SNMPTRAP:
            {
                writeMessage("SNMPTRAP\n");
                break;
            }
            
            default:
            {
                writeMessage("unknown\n");
                break;
            }
            
            
        }
        
        
        
        SNMPSequence varBindList = pdu.getVarBindList();
        SNMPSequence responseList = new SNMPSequence();
        
        for (int i = 0; i < varBindList.size(); i++)
        {
            SNMPSequence variablePair = (SNMPSequence)varBindList.getSNMPObjectAt(i);
            SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier)variablePair.getSNMPObjectAt(0);
            SNMPObject snmpValue = (SNMPObject)variablePair.getSNMPObjectAt(1);
            
            writeMessage("       OID:           " + snmpOID + "\n");
            writeMessage("       value:         " + snmpValue + "\n");
            
            
            // check to see if supplied community name is ours; if not, we'll just silently
            // ignore the request by not returning anything
            if (!communityName.equals(this.communityName))
            {
                continue;
            }
            
            // we'll only respond to requests for OIDs 1.3.6.1.2.1.99.0 and 1.3.6.1.2.1.100.0 
            
            // OID 1.3.6.1.2.1.99.0: it's read-only
            if (snmpOID.toString().equals("1.3.6.1.2.1.99.0"))
            {
                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                {
                    // got a set-request for our variable; throw an exception to indicate the 
                    // value is read-only - the SNMPv1AgentInterface will create the appropriate
                    // error message using our supplied error index and status
                    // note that error index starts at 1, not 0, so it's i+1
                    int errorIndex = i+1;
                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                }
                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                {
                    // got a get-request for our variable; send back a value - just a string
                    try
                    {
                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPOctetString("Boo"));
                        //SNMPVariablePair newPair = new SNMPVariablePair(snmpOID, new SNMPOctetString("Boo"));
                        responseList.addSNMPObject(newPair);
                    }
                    catch (SNMPBadValueException e)
                    {
                        // won't happen...
                    }
                } 
                
            }
            
            if (snmpOID.toString().equals("1.3.6.1.2.1.100.0"))
            {
                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                {
                    // got a set-request for our variable; supplied value must be a string
                    if (snmpValue instanceof SNMPOctetString)
                    {
                        // assign new value
                        storedSNMPValue = (SNMPOctetString)snmpValue;
                        
                        // return SNMPVariablePair to indicate we've handled this OID
                        try
                        {
                            SNMPVariablePair newPair = new SNMPVariablePair(snmpOID, storedSNMPValue);
                            responseList.addSNMPObject(newPair);
                        }
                        catch (SNMPBadValueException e)
                        {
                            // won't happen...
                        }
                    
                    }
                    else
                    {
                        int errorIndex = i+1;
                        int errorStatus = SNMPRequestException.BAD_VALUE;
                        throw new SNMPSetException("Supplied value must be SNMPOctetString", errorIndex, errorStatus);
                    }
                    
                }
                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                {
                    // got a get-request for our variable; send back a value - just a string
                    try
                    {
                        SNMPVariablePair newPair = new SNMPVariablePair(snmpOID, storedSNMPValue);
                        responseList.addSNMPObject(newPair);
                    }
                    catch (SNMPBadValueException e)
                    {
                        // won't happen...
                    }
                } 
                
            }
            
        }
        
        writeMessage("\n");
        
        
        // return the created list of variable pairs
        return responseList;
        
    }
    
    
    
    
    public SNMPSequence processGetNextRequest(SNMPPDU pdu, String communityName)
        throws SNMPGetException
    {
        writeMessage("Got pdu:\n");
        
        writeMessage("  community name:     " + communityName + "\n");
        writeMessage("  request ID:         " + pdu.getRequestID() + "\n");
        writeMessage("  pdu type:           ");
        byte pduType = pdu.getPDUType();
        
        switch (pduType)
        {
            case SNMPBERCodec.SNMPGETREQUEST:
            {
                writeMessage("SNMPGETREQUEST\n");
                break;
            }
            
            case SNMPBERCodec.SNMPGETNEXTREQUEST:
            {
                writeMessage("SNMPGETNEXTREQUEST\n");
                break;
            }
            
            case SNMPBERCodec.SNMPSETREQUEST:
            {
                writeMessage("SNMPSETREQUEST\n");
                break;
            }
            
            case SNMPBERCodec.SNMPGETRESPONSE:
            {
                writeMessage("SNMPGETRESPONSE\n");
                break;
            }
            
            case SNMPBERCodec.SNMPTRAP:
            {
                writeMessage("SNMPTRAP\n");
                break;
            }
            
            default:
            {
                writeMessage("unknown\n");
                break;
            }
            
            
        }
        
        
        
        SNMPSequence varBindList = pdu.getVarBindList();
        SNMPSequence responseList = new SNMPSequence();
        
        for (int i = 0; i < varBindList.size(); i++)
        {
            SNMPSequence variablePair = (SNMPSequence)varBindList.getSNMPObjectAt(i);
            SNMPObjectIdentifier suppliedOID = (SNMPObjectIdentifier)variablePair.getSNMPObjectAt(0);
            SNMPObject suppliedObject = (SNMPObject)variablePair.getSNMPObjectAt(1);
            
            writeMessage("       OID:           " + suppliedOID + "\n");
            writeMessage("       value:         " + suppliedObject + "\n");
            
            
            // check to see if supplied community name is ours; if not, we'll just silently
            // ignore the request by not returning anything
            if (!communityName.equals(this.communityName))
            {
                continue;
            }
            
            // we'll only respond to requests for OID 1.3.6.1.2.1.99.0, and it's read-only;
            // for get-next request, we'll return the value for 1.3.6.1.2.1.100.0
            if (suppliedOID.toString().equals("1.3.6.1.2.1.99.0"))
            {
                if (pduType == SNMPBERCodec.SNMPGETNEXTREQUEST)
                {
                    // got a get-next-request for our variable; send back a value for OID 1.3.6.1.2.1.100.0
                    try
                    {
                        // create SNMPVariablePair for the next OID and its value 
                        SNMPObjectIdentifier nextOID = new SNMPObjectIdentifier("1.3.6.1.2.1.100.0");
                        SNMPVariablePair innerPair = new SNMPVariablePair(nextOID, storedSNMPValue);
                        
                        // now create a pair containing the supplied OID and the variable pair containing the following
                        // OID and its value; this allows the ANMPv1AgentInterface to know which of the supplied OIDs
                        // the new OID corresponds to (follows).
                        SNMPVariablePair outerPair = new SNMPVariablePair(suppliedOID, innerPair);
                        
                        // add the "compound" SNMPVariablePair to the response list
                        responseList.addSNMPObject(outerPair);
                    }
                    catch (SNMPBadValueException e)
                    {
                        // won't happen...
                    }
                }
                
            }
            
        }
        
        writeMessage("\n");
        
        
        // return the created list of variable pairs
        return responseList;
        
    }
    
    
    
    
    public void run()
    {
        int numChars;
        char[] charArray = new char[256];
        
        try
        {
            while (!readerThread.isInterrupted() && ((numChars = errorReader.read(charArray, 0, charArray.length)) != -1))
            {
                StringBuffer errorMessage = new StringBuffer();
                errorMessage.append("Problem receiving request:\n");
                errorMessage.append(new String(charArray, 0, numChars));
                errorMessage.append("\n");
                writeMessage(errorMessage.toString());
            }
        }
        catch(IOException e)
        {
            messagesArea.append("Problem receiving errors; error reporter exiting!");
        }
    }
    
    
    
    
    public static void main(String args[]) 
    {
        try
        {
            SNMPAgentTest theApp = new SNMPAgentTest();
            theApp.pack();
            theApp.setSize(700,500);
            theApp.setVisible(true);
        }
        catch (Exception e)
        {}
    }
    

}