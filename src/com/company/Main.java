package com.company;


import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    /** List of permitted agrs **/
    private static List<ArgAvailable> argList = new ArrayList<>();
    private static ArgAvailable help;

    /**
     * setArgList method fills the argList with allowed arguments by reading the file "args.txt"
     **/
    private static void setArgList() throws FileNotFoundException {
        File file = new File("args.txt");
        Scanner in = new Scanner(file);
        help = new ArgAvailable(in.nextLine(),in.nextLine());
        while(in.hasNext()){
            argList.add(new ArgAvailable(in.nextLine(),in.nextLine()));
        }
    }
    /**
     * verificationArgs method checks all arguments write by user in command line while running the program
     * It throws an Exception/false when user typed:
     * unavailable argument
     * twice or more the same argument
     * --help or -h along with the other arguments
     **/
    private static boolean verificationArgs(String [] args){

        /** checking if --help or -h has been typed **/
        if(help.match(args[0]) && args.length == 1) {
            return true;
        }
        else if (help.match(args[0])) {
            return false;
        }
        /** checking the rest of arguments typed by an user **/
        for(int i = 0; i < args.length; i++) {
            boolean  flag = false; //is argument given by an user available
            for( int j = 0; j < argList.size(); j++){
                if(argList.get(j).match(args[i])){
                    argList.remove(j);
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return false;
            }
        }
        return true;
    }
    public static int main(String[] args) throws IOException {
       setArgList();

       try {
            if(!verificationArgs(args)){
                throw new Exception();
            }
       } catch (Exception e) {
           System.out.println("Wrong argument list. Type --help or -h to check command list");
           return -1;
       }

       IPv4Addresses iPv4Addresses = new IPv4Addresses();

       return 0;
    }
}
