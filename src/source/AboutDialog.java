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



import java.awt.*;
import javax.swing.*;

public class AboutDialog extends JDialog
                                implements Runnable
{

    private JLabel aboutLabel1 = new JLabel("SNMP Inquisitor");
    private JLabel aboutLabel2 = new JLabel("J. Sevy");
    private JLabel aboutLabel3 = new JLabel("November, 2000");
    private JLabel aboutLabel4 = new JLabel("");
    private JLabel aboutLabel5 = new JLabel("");
    
    private String inquisitionString = "\"NO one expects the SNMP Inquisition...\" ";
    private String mpString = "   (- shamelessly adapted from Monty Python's Flying Circus)";
    
    Thread displayThread;
    
    
    public AboutDialog(JFrame parent)
    {
        super(parent, "About SNMP Inquisitor", true /*modal*/);
        
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        setUpDisplay();
        
        this.setLocation(Math.round((parent.getSize().width - this.getSize().width)/2), Math.round((parent.getSize().height - this.getSize().height)/2));
        
        // create and start display thread
        displayThread = new Thread(this);
        displayThread.start();
        
        this.show();
    
    }
    
    
    
    
    public void hide()
    {
        super.hide();
        
        // interrupt thread so it can exit..
        displayThread.interrupt();
    }
    
    
        
        
    private void setUpDisplay()
    {
        
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
        
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(theLayout);
        
        c.gridx = 1;
        c.gridy = 1;
        theLayout.setConstraints(aboutLabel1, c);
        aboutPanel.add(aboutLabel1);
        
        c.gridx = 1;
        c.gridy = 2;
        theLayout.setConstraints(aboutLabel2, c);
        aboutPanel.add(aboutLabel2);
        
        c.gridx = 1;
        c.gridy = 3;
        theLayout.setConstraints(aboutLabel3, c);
        aboutPanel.add(aboutLabel3);
        
        c.gridx = 1;
        c.gridy = 4;
        theLayout.setConstraints(aboutLabel4, c);
        aboutPanel.add(aboutLabel4);
        
        c.gridx = 1;
        c.gridy = 5;
        theLayout.setConstraints(aboutLabel5, c);
        aboutPanel.add(aboutLabel5);
        
        
        this.getContentPane().add(aboutPanel);
        this.pack();
        
        this.setSize(300, 150);
        
    }
    
    
    
    public void run()
    {
        
        
        try
        {
            
            
            // play sound clip from jar file, IF java version high enough...
            
            String version = System.getProperty("java.version");
            
            //System.out.println(version);
            //System.out.println(version.compareTo("1.3"));
            
            if (version.compareTo("1.3") >= 0)
            {
                AudioFilePlayer audioPlayer = new AudioFilePlayer("SNMPInquisitor.jar", "inquisition.wav");
                audioPlayer.playFromJarFile();
            }
            
            // simultaneously, write message out a character at a time...
            int numChars = inquisitionString.length();
            
            for (int i = 0; i < numChars; i++)
            {
                aboutLabel4.setText(inquisitionString.substring(0,i));
                Thread.sleep(60);
            }
            
            aboutLabel5.setText(mpString);
            
    
        }
        catch(Exception e)
        {
            // don't bother informing of exception; just exit...
            //System.out.println(e);
        }
            
        
        // later!
    }
            
            
            
}