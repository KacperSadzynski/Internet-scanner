package source;/*
 * SNMP Trap Test
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
import java.net.*;

import snmp.*;




public class SNMPTrapReceiveTest extends JFrame
                            implements ActionListener, SNMPv1TrapListener, SNMPv2TrapListener, SNMPv2InformRequestListener, Runnable
{
    
    JButton clearButton;
    JTextArea messagesArea;
    JScrollPane messagesScroll;
    JTextField hostIDField, communityField, OIDField, valueField, enterpriseField, agentField;
    JLabel authorLabel, hostIDLabel, communityLabel, OIDLabel, valueLabel, enterpriseLabel, agentLabel, genericTrapLabel, specificTrapLabel;
    JComboBox valueTypeBox, genericTrapBox, specificTrapBox;
    
    MenuBar theMenubar;
    Menu fileMenu;
    MenuItem aboutItem, quitItem;
    
    
    SNMPTrapReceiverInterface trapReceiverInterface;
    
    PipedReader errorReader;
    PipedWriter errorWriter;
    Thread readerThread;
    
    
    
    
    // WindowCloseAdapter to catch window close-box closings
    private class WindowCloseAdapter extends WindowAdapter
    { 
        public void windowClosing(WindowEvent e)
        {
            readerThread.interrupt();
            System.exit(0);
        }
    };
            
    
    public SNMPTrapReceiveTest() 
    {
        setUpDisplay();
            
        try
        {
            errorReader = new PipedReader();
            errorWriter = new PipedWriter(errorReader);
            
            readerThread = new Thread(this);
            readerThread.start();
            
            trapReceiverInterface = new SNMPTrapReceiverInterface(new PrintWriter(errorWriter));
            trapReceiverInterface.addv1TrapListener(this);
            trapReceiverInterface.addv2TrapListener(this);
            trapReceiverInterface.addv2InformRequestListener(this);
            trapReceiverInterface.startReceiving();
            
        }
        catch(Exception e)
        {
            messagesArea.append("Problem starting Trap Test: " + e.toString() + "\n");
        }
    }
    
    
    
    private void setUpDisplay()
    {
        
        this.setTitle("SNMP Trap Receive Test");
            
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
        
        
        authorLabel = new JLabel(" Version 1.1        J. Sevy, January 2001 ");
        authorLabel.setFont(new Font("SansSerif", Font.ITALIC, 8));
            
        
        clearButton = new JButton("Clear messages");
        clearButton.setActionCommand("clear messages");
        clearButton.addActionListener(this);
        
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
        JLabel messagesLabel = new JLabel("Received traps:");
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
        
              
    }
    
    
    
            
    
    
    public void processv1Trap(SNMPv1TrapPDU pdu, String communityName) 
    {
        messagesArea.append("Got v1 trap:\n");
        
        messagesArea.append("  community name:     " + communityName + "\n");
        messagesArea.append("  enterprise OID:     " + pdu.getEnterpriseOID().toString() + "\n");
        messagesArea.append("  agent address:      " + pdu.getAgentAddress().toString() + "\n");
        messagesArea.append("  generic trap:       " + pdu.getGenericTrap() + "\n");
        messagesArea.append("  specific trap:      " + pdu.getSpecificTrap() + "\n");
        messagesArea.append("  timestamp:          " + pdu.getTimestamp() + "\n");
        messagesArea.append("  supplementary vars: " + pdu.getVarBindList().toString() + "\n");
        
        messagesArea.append("\n");
        
    }
    
    
    
    public void processv2Trap(SNMPv2TrapPDU pdu, String communityName, InetAddress agentIPAddress) 
    {
        messagesArea.append("Got v2 trap:\n");
        
        messagesArea.append("  agent IP address:   " + agentIPAddress.getHostAddress() + "\n");
        messagesArea.append("  community name:     " + communityName + "\n");
        messagesArea.append("  system uptime:      " + pdu.getSysUptime().toString() + "\n");
        messagesArea.append("  trap OID:           " + pdu.getSNMPTrapOID().toString() + "\n");
        messagesArea.append("  var bind list:      " + pdu.getVarBindList().toString() + "\n");
                
        messagesArea.append("\n");
        
    }
    
    
    
    public void processv2InformRequest(SNMPv2InformRequestPDU pdu, String communityName, InetAddress agentIPAddress) 
    {
        messagesArea.append("Got v2 inform request:\n");
        
        messagesArea.append("  sender IP address:  " + agentIPAddress.getHostAddress() + "\n");
        messagesArea.append("  community name:     " + communityName + "\n");
        messagesArea.append("  system uptime:      " + pdu.getSysUptime().toString() + "\n");
        messagesArea.append("  trap OID:           " + pdu.getSNMPTrapOID().toString() + "\n");
        messagesArea.append("  var bind list:      " + pdu.getVarBindList().toString() + "\n");
                
        messagesArea.append("\n");
        
    }
    
    
    
    
    public void run()
    {
        int numChars;
        char[] charArray = new char[256];
        
        try
        {
            while (!readerThread.isInterrupted() && ((numChars = errorReader.read(charArray, 0, charArray.length)) != -1))
            {
                messagesArea.append("Problem receiving trap or inform:\n");
                messagesArea.append(new String(charArray, 0, numChars));
                messagesArea.append("\n\n");
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
            SNMPTrapReceiveTest theApp = new SNMPTrapReceiveTest();
            theApp.pack();
            theApp.setSize(700,500);
            theApp.setVisible(true);
        }
        catch (Exception e)
        {}
    }
    

}