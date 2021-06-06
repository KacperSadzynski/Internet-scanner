package com.company;

/**
 * ArgAvailable class keeps the information about a single permitted argument (full name and shortcut)
 * which user can write in the command line<br/>
 * Instance Variables:<br/>
 * final String fullName - represents full name of an argument<br/>
 * final String shortcut - represents short name of an argument
 */
public class ArgAvailable {

    private final String fullName;
    private final String shortcut;


    /**
     * Constructor<br/>
     * @param fullName used to set fullName variable
     * @param shortcut used to set shortcut variable
     */
    ArgAvailable( String fullName, String shortcut){
        this.fullName = fullName;
        this.shortcut = shortcut;
    }

    /**
     * Compares the passed argument with available one
     * @param arg will be compare with fullName and shortcut
     * @return false if arg is not equal to fullName or shortcut
     *         true  if arg is equal to fullName or shortcut
     */
    public boolean match(String arg){
        return (arg.equals(fullName) || arg.equals(shortcut));
    }

    /**
     * Getter
     * @return string shortcut
     */
    public String getShortcut() {
        return shortcut;
    }
}
