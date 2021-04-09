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

import java.io.*;
import javax.sound.sampled.*;
import java.util.jar.*;


/**
*    Utility class giving a convenient interface for playing sound from a file. Note
*    that this requires the javax.sound.sampled.* classes that are part of jdk1.3 and up;
*    as such, you should test the version of Java to make sure it's high enough to support
*    this - e.g.,
*
*    String version = System.getProperty("java.version");
*            
*    if (version.compareTo("1.3") >= 0)
*    {
*        AudioFilePlayer audioPlayer = new AudioFilePlayer("thisJarFile.jar", "mySound.wav");
*        audioPlayer.playFromJarFile();
*    }
*
**/

public class AudioFilePlayer
{
    
    private String soundFileName, jarFileName;
    
    
    /**
    *    Create a player for the audio file whose pathname is supplied.
    *    Note: used with standard file-system file, not jar file.
    **/
    
    public AudioFilePlayer(String soundFileName)
    {
        this.soundFileName = soundFileName;
    }
    
    
    
    /**
    *    Create a player for the specified audio file contained in the specified jar file. 
    *    Note that the jar file may be the one containing the application code.
    **/
    
    public AudioFilePlayer(String jarFileName, String soundFileName)
    {
        this.jarFileName = jarFileName;
        this.soundFileName = soundFileName;
    }
    
    
    
    /**
    *    Play the associated audio file contained in the associated jar file.
    **/
    
    public void playFromJarFile()
    {
    
        try
        {
            JarFile thisJarFile = new JarFile(jarFileName);
            
            JarEntry audioEntry = thisJarFile.getJarEntry(soundFileName);
            
            // need an input stream supporting mark() and reset(); used BufferedInputStream        
            BufferedInputStream jarFileInputStream = new BufferedInputStream(thisJarFile.getInputStream(audioEntry));
            
            // get the audio file format; think this is broken...
            AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(jarFileInputStream);
            
            // ..so the following doesn't work
            //AudioFormat audioFormat = audioFileFormat.getFormat();
            
            // so get the audio format hard-coded; wish the preceding commented-out line worked...
            AudioFormat audioFormat = new AudioFormat(11025, 16, 1, true, false);
            
            DataLine.Info dataLineInfo = new DataLine.Info(Clip.class, audioFormat);
            
            Clip audioClip = (Clip)AudioSystem.getLine(dataLineInfo);
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(jarFileInputStream);
            
            audioClip.open(audioInputStream);
            
            audioClip.start();
        }
        catch (Exception e)
        {
            // do nothing...
            System.out.println(e);
        }
    
    }
    
    
    
    /**
    *    Play the associated audio file.
    **/
    
    public void playFromFile()
    {
    
        try
        {
            File audioFile = new File(soundFileName);
            
            AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(audioFile);
            
            AudioFormat audioFormat = audioFileFormat.getFormat();
                    
            DataLine.Info dataLineInfo = new DataLine.Info(Clip.class, audioFormat);
            
            Clip audioClip = (Clip)AudioSystem.getLine(dataLineInfo);
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            
            audioClip.open(audioInputStream);
            
            audioClip.start();
        }
        catch (Exception e)
        {
            // do nothing...
            System.out.println(e);
        }
    
    }

}