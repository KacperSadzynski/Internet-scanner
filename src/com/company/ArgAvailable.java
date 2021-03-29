package com.company;

import java.util.Objects;

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
