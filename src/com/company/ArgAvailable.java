package com.company;

import java.util.Objects;

/**
ArgAvailable class keeps the information about a single permitted argument (full name and shortcut)
which you can write in the command line while running the program
**/
public class ArgAvailable {
    private String fullName;
    private String shortcut;



    ArgAvailable( String fullName, String shortcut){
        this.fullName = fullName;
        this.shortcut = shortcut;
    }

   public boolean match(String arg){
        return (arg.equals(fullName) || arg.equals(shortcut));
   }
}
