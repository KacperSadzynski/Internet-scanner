package source;/*
 * SNMP Inquisitor
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
import java.net.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import java.awt.event.*;
import java.io.*;
import snmp.*;




public class SNMPInquisitor extends JFrame
                            implements ActionListener, Runnable
{
    
    JButton getDataButton, getTreewalkDataButton, getTableButton, getNextButton, setValueButton;
    JButton clearButton;
    JTextArea messagesArea;
    JScrollPane messagesScroll;
    JTextField hostIDField, communityField, OIDField, valueField;
    JLabel authorLabel, hostIDLabel, communityLabel, OIDLabel, valueLabel;
    JComboBox valueTypeBox;
    
    MenuBar theMenubar;
    Menu fileMenu;
    MenuItem aboutItem, quitItem;
    
    Thread treewalkThread;
    
    SNMPv1CommunicationInterface comInterface;
    String community;
    InetAddress hostAddress;
    int version;
    
    
    // WindowCloseAdapter to catch window close-box closings
    private class WindowCloseAdapter extends WindowAdapter
    { 
        public void windowClosing(WindowEvent e)
        {
            System.exit(0);
        }
    };
            
    
    public SNMPInquisitor() 
    {
        treewalkThread = new Thread(this);
        
        setUpDisplay();
        
    }
    
    
    
    private void setUpDisplay()
    {
        
        
        this.setTitle("SNMP Inquisitor");
            
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
        
        fileMenu.addSeparator();
        
        quitItem = new MenuItem("Quit");
        quitItem.setActionCommand("quit");
        quitItem.addActionListener(this);
        fileMenu.add(quitItem);
        
        theMenubar.add(fileMenu);
        
        
        hostIDLabel = new JLabel("Device address:");
        hostIDField = new JTextField(20);
        hostIDField.setText("10.0.1.1");
        hostIDField.setEditable(true);
        
        OIDLabel = new JLabel("OID:");
        OIDField = new JTextField(20);
        OIDField.setEditable(true);
        
        valueLabel = new JLabel("Value (for Set):");
        valueField = new JTextField(20);
        valueField.setEditable(true);
        
        communityLabel = new JLabel("Community:");
        communityField = new JTextField(20);
        communityField.setText("public");
        communityField.setEditable(true);
        
        authorLabel = new JLabel(" Version 1.1        J. Sevy, January 2001 ");
        authorLabel.setFont(new Font("SansSerif", Font.ITALIC, 8));
            
        
        getDataButton = new JButton("Get OID value");
        getDataButton.setActionCommand("get data");
        getDataButton.addActionListener(this);
        
        setValueButton = new JButton("Set OID value");
        setValueButton.setActionCommand("set value");
        setValueButton.addActionListener(this);
        
        getTableButton = new JButton("Get table");
        getTableButton.setActionCommand("get table");
        getTableButton.addActionListener(this);
        
        getNextButton = new JButton("Get next OID value");
        getNextButton.setActionCommand("get next");
        getNextButton.addActionListener(this);
        
        getTreewalkDataButton = new JButton("Get all OID values");
        getTreewalkDataButton.setActionCommand("get treewalk data");
        getTreewalkDataButton.addActionListener(this);
        
        clearButton = new JButton("Clear responses");
        clearButton.setActionCommand("clear messages");
        clearButton.addActionListener(this);
        
        messagesArea = new JTextArea(10,60);
        messagesScroll = new JScrollPane(messagesArea);
        
        valueTypeBox = new JComboBox();
        valueTypeBox.addItem("SNMPInteger");
        valueTypeBox.addItem("SNMPCounter32");
        valueTypeBox.addItem("SNMPCounter64");
        valueTypeBox.addItem("SNMPGauge32");
        valueTypeBox.addItem("SNMPOctetString");
        valueTypeBox.addItem("SNMPIPAddress");
        valueTypeBox.addItem("SNMPNSAPAddress");
        valueTypeBox.addItem("SNMPObjectIdentifier");
        valueTypeBox.addItem("SNMPTimeTicks");
        valueTypeBox.addItem("SNMPUInteger32");
         

        
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
        
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(theLayout);
        
        c.gridx = 1;
        c.gridy = 1;
        theLayout.setConstraints(getDataButton, c);
        buttonPanel.add(getDataButton);
        
        c.gridx = 2;
        c.gridy = 1;
        theLayout.setConstraints(getNextButton, c);
        buttonPanel.add(getNextButton);
        
        c.gridx = 3;
        c.gridy = 1;
        theLayout.setConstraints(getTableButton, c);
        buttonPanel.add(getTableButton);
        
        c.gridx = 4;
        c.gridy = 1;
        theLayout.setConstraints(getTreewalkDataButton, c);
        buttonPanel.add(getTreewalkDataButton);
        
        c.gridx = 5;
        c.gridy = 1;
        theLayout.setConstraints(setValueButton, c);
        buttonPanel.add(setValueButton);
        
        
        JPanel hostPanel = new JPanel();
        hostPanel.setLayout(theLayout);
        
        c.gridx = 1;
        c.gridy = 1;
        theLayout.setConstraints(hostIDLabel, c);
        hostPanel.add(hostIDLabel);
        
        c.gridx = 2;
        c.gridy = 1;
        theLayout.setConstraints(hostIDField, c);
        hostPanel.add(hostIDField);
        
        c.gridx = 1;
        c.gridy = 2;
        theLayout.setConstraints(communityLabel, c);
        hostPanel.add(communityLabel);
        
        c.gridx = 2;
        c.gridy = 2;
        theLayout.setConstraints(communityField, c);
        hostPanel.add(communityField);
        
        
        
        JPanel oidPanel = new JPanel();
        oidPanel.setLayout(theLayout);
        
        c.gridx = 1;
        c.gridy = 1;
        theLayout.setConstraints(OIDLabel, c);
        oidPanel.add(OIDLabel);
        
        c.gridx = 2;
        c.gridy = 1;
        theLayout.setConstraints(OIDField, c);
        oidPanel.add(OIDField);
        
        c.gridx = 1;
        c.gridy = 2;
        theLayout.setConstraints(valueLabel, c);
        oidPanel.add(valueLabel);
        
        c.gridx = 2;
        c.gridy = 2;
        theLayout.setConstraints(valueField, c);
        oidPanel.add(valueField);
        
        c.gridx = 3;
        c.gridy = 2;
        theLayout.setConstraints(valueTypeBox, c);
        oidPanel.add(valueTypeBox);
        
        
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        
        
        
        JPanel messagesPanel = new JPanel();
        messagesPanel.setLayout(theLayout);
        
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        JLabel messagesLabel = new JLabel("Responses:");
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
        
        
        c.gridx = 1;
        c.gridy = 1;
        theLayout.setConstraints(hostPanel, c);
        this.getContentPane().add(hostPanel);
        
        c.gridx = 1;
        c.gridy = 2;
        theLayout.setConstraints(oidPanel, c);
        this.getContentPane().add(oidPanel);
        
        c.gridx = 1;
        c.gridy = 3;
        theLayout.setConstraints(buttonPanel, c);
        this.getContentPane().add(buttonPanel);
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = .5;
        c.weighty = .5;
        theLayout.setConstraints(messagesPanel, c);
        this.getContentPane().add(messagesPanel);
        
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 5;
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
            System.exit(0);
        }
        
        
        
        if (command == "clear messages")
        {
            messagesArea.setText("");
        }
        
        
        
        if (command == "about")
        {
            AboutDialog aboutDialog = new AboutDialog(this);
        }
        
        
        
        if (command == "get data")
        {
            try
            {
            
                String community = communityField.getText();
                int version = 0;    // SNMPv1
                InetAddress hostAddress = InetAddress.getByName(hostIDField.getText());
                SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);
                
                StringTokenizer st = new StringTokenizer(OIDField.getText(), " ,;");
                
                while (st.hasMoreTokens())
                {
                    String itemID = st.nextToken();    
                    SNMPVarBindList newVars = comInterface.getMIBEntry(itemID);
                    SNMPSequence pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
                    SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
                    SNMPObject snmpValue = pair.getSNMPObjectAt(1);
                    String typeString = snmpValue.getClass().getName();
                    
                    if (typeString.equals("snmp.SNMPOctetString"))
                    {
                        String snmpString = snmpValue.toString();
                        
                        // truncate at first null character
                        int nullLocation = snmpString.indexOf('\0');
                        if (nullLocation >= 0)
                            snmpString = snmpString.substring(0,nullLocation);
                        
                        messagesArea.append("OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpString);
                        messagesArea.append("  (hex: " + ((SNMPOctetString)snmpValue).toHexString() + ")\n");
                    }
                    else
                    {
                        messagesArea.append("OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue);
                        messagesArea.append("\n");
                    }
                }    
            }
            catch(InterruptedIOException e)
            {
                messagesArea.append("Interrupted during retrieval:  " + e + "\n");
            }
            catch(Exception e)
            {
                messagesArea.append("Exception during retrieval:  " + e + "\n");
            }
                    
        }
        
        
        
        if (command == "get next")
        {
            try
            {
            
                String community = communityField.getText();
                int version = 0;    // SNMPv1
                InetAddress hostAddress = InetAddress.getByName(hostIDField.getText());
                SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);
                
                StringTokenizer st = new StringTokenizer(OIDField.getText(), " ,;");
                
                while (st.hasMoreTokens())
                {
                    String itemID = st.nextToken();    
                    SNMPVarBindList newVars = comInterface.getNextMIBEntry(itemID);
                    SNMPSequence pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
                    SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
                    SNMPObject snmpValue = pair.getSNMPObjectAt(1);
                    String typeString = snmpValue.getClass().getName();
                    
                    if (typeString.equals("snmp.SNMPOctetString"))
                    {
                        String snmpString = snmpValue.toString();
                        
                        // truncate at first null character
                        int nullLocation = snmpString.indexOf('\0');
                        if (nullLocation >= 0)
                            snmpString = snmpString.substring(0,nullLocation);
                        
                        messagesArea.append("OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpString);
                        messagesArea.append("  (hex: " + ((SNMPOctetString)snmpValue).toHexString() + ")\n");
                    }
                    else
                    {
                        messagesArea.append("OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue);
                        messagesArea.append("\n");
                    }
                }    
            }
            catch(InterruptedIOException e)
            {
                messagesArea.append("Interrupted during retrieval:  " + e + "\n");
            }
            catch(Exception e)
            {
                messagesArea.append("Exception during retrieval:  " + e + "\n");
            }
                    
        }
        
        
        
        if (command == "get table")
        {
            try
            {
            
                String community = communityField.getText();
                int version = 0;    // SNMPv1
                InetAddress hostAddress = InetAddress.getByName(hostIDField.getText());
                SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);
                
                String itemID = OIDField.getText();    
                
                SNMPVarBindList newVars = comInterface.retrieveMIBTable(itemID);
                
                // print the retrieved stuff
                for (int i = 0; i < newVars.size(); i++)
                {
                    SNMPSequence pair = (SNMPSequence)(newVars.getSNMPObjectAt(i));
                    
                    SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
                    SNMPObject snmpValue = pair.getSNMPObjectAt(1);
                    String typeString = snmpValue.getClass().getName();
                    
                    if (typeString.equals("snmp.SNMPOctetString"))
                    {
                        String snmpString = snmpValue.toString();
                        
                        // truncate at first null character
                        int nullLocation = snmpString.indexOf('\0');
                        if (nullLocation >= 0)
                            snmpString = snmpString.substring(0,nullLocation);
                        
                        messagesArea.append("OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpString);
                        messagesArea.append("  (hex: " + ((SNMPOctetString)snmpValue).toHexString() + ")\n");
                    }
                    else
                    {
                        messagesArea.append("OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue);
                        messagesArea.append("\n");
                    }
                
                }
            }
            catch(InterruptedIOException e)
            {
                messagesArea.append("Interrupted during retrieval:  " + e + "\n");
            }
            catch(Exception e)
            {
                messagesArea.append("Exception during retrieval:  " + e + "\n");
            }
                    
        }
        
        
        
        
        if (command == "set value")
        {
            try
            {
            
                String community = communityField.getText();
                int version = 0;    // SNMPv1
                InetAddress hostAddress = InetAddress.getByName(hostIDField.getText());
                SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);
            
                
                String itemID = OIDField.getText();
                String valueString = valueField.getText();
                String valueTypeString = (String)valueTypeBox.getSelectedItem();
                valueTypeString = "snmp." + valueTypeString;
                
                SNMPObject itemValue;
                Class valueClass = Class.forName(valueTypeString);
                itemValue = (SNMPObject)valueClass.newInstance();
                itemValue.setValue(valueString);
                    
                SNMPVarBindList newVars = comInterface.setMIBEntry(itemID, itemValue);
                
                SNMPSequence pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
            
                SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
                
                SNMPObject snmpValue = pair.getSNMPObjectAt(1);
                
                String typeString = snmpValue.getClass().getName();
                
                messagesArea.append("OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue);
                
                if (typeString.equals("snmp.SNMPOctetString"))
                    messagesArea.append("  (hex: " + ((SNMPOctetString)snmpValue).toHexString() + ")\n");
                else
                    messagesArea.append("\n");
            
            }
            catch(InterruptedIOException e)
            {
                messagesArea.append("Interrupted during retrieval:  " + e + "\n");
            }
            catch(Exception e)
            {
                messagesArea.append("Exception during retrieval:  " + e + "\n");
            }
                    
        }
        
        
        
        
        if (command == "get treewalk data")
        {
            if (!treewalkThread.isAlive())
            {
                treewalkThread = new Thread(this);
                treewalkThread.start();
                getTreewalkDataButton.setText("Stop OID retrieval");
            }
            else
            {
                treewalkThread.interrupt();
            }
        }
        
        
    
        
    }
    
    
    
    
    
    
    public void run() 
    {
        
        try
        {
        
            String community = communityField.getText();
            int version = 0;    // SNMPv1
            InetAddress hostAddress = InetAddress.getByName(hostIDField.getText());
            SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, community);
        
            
            //String itemID = "1.3.6.1.2.1.1.1.0";    // start with device name
            String itemID = "";            
            String retrievedID = "1.3.6.1.2.1";        // start point
                
                
            while (!Thread.interrupted() && !retrievedID.equals(itemID))
            {
                itemID = retrievedID;
                
                SNMPVarBindList newVars = comInterface.getNextMIBEntry(itemID);
                
                SNMPSequence pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
                SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
                SNMPObject snmpValue = pair.getSNMPObjectAt(1);
                retrievedID = snmpOID.toString();
                String typeString = snmpValue.getClass().getName();
                
                if (typeString.equals("snmp.SNMPOctetString"))
                {
                    String snmpString = snmpValue.toString();
                    
                    // truncate at first null character
                    int nullLocation = snmpString.indexOf('\0');
                    if (nullLocation >= 0)
                        snmpString = snmpString.substring(0,nullLocation);
                    
                    messagesArea.append("OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpString);
                    messagesArea.append("  (hex: " + ((SNMPOctetString)snmpValue).toHexString() + ")\n");
                }
                else
                {
                    messagesArea.append("OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue);
                    messagesArea.append("\n");
                }
            }
            
            
        }
        catch(InterruptedIOException e)
        {
            messagesArea.append("Interrupted during retrieval:  " + e + "\n");
        }
        catch(Exception e)
        {
            messagesArea.append("Exception during retrieval:  " + e + "\n");
        }
        catch(Error err)
        {
            messagesArea.append("Error during retrieval:  " + err + "\n");
        }
        
        getTreewalkDataButton.setText("Get all OID values");
        
    }
    
    
    
    
    
    
    
    
    private String hexByte(byte b)
    {
        int pos = b;
        if (pos < 0)
            pos += 256;
        String returnString = new String();
        returnString += Integer.toHexString(pos/16);
        returnString += Integer.toHexString(pos%16);
        return returnString;
    }
    
    
    
    
    
    
    
    
    
    public static void main(String args[]) 
    {
        try
        {
            SNMPInquisitor theApp = new SNMPInquisitor();
            theApp.pack();
            theApp.setSize(700,500);
            theApp.setVisible(true);
        }
        catch (Exception e)
        {
            System.out.println("Exception starting app: " + e.toString());
        }
    }
    

}