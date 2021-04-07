package com.company;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
            args[0] = help.getShortcut();
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
                    args[i] = argList.get(j).getShortcut();
                    argList.remove(j);
                    flag = true;
                    if (args[i].equals("-w")) {
                        IPv4Addresses setFlag = new IPv4Addresses(true);
                    }

                    break;
                }
            }
            if(!flag){
                return false;
            }
        }
        return true;
    }
    public static void main(String[] args) throws IOException {
        IPv4Addresses setFlag = new IPv4Addresses(true);
       setArgList();
        /*
       try {
            if(!verificationArgs(args)){
                throw new Exception();
            }
       } catch (Exception e) {
           System.out.println("Wrong argument list. Type --help or -h to check command list");
           return;
       }
        */
        System.out.println("Running DNS Scanner");
        DNSScanner scanner = new DNSScanner();
        scanner.doStuff();

        for(int i = 0; i < args.length; i++) {
            switch (args[i]){
                case "-h": {
                    File file = new File("help.txt");
                    Scanner in = new Scanner(file);
                    while(in.hasNext()){
                        System.out.println(in.nextLine());
                    }
                    break;
                }
                case "-d": {
                    System.out.println("Running DNS Scanner");
                    //DNSScanner scanner = new DNSScanner();
                    //scanner.scan();
                    break;
                }
                case "-s": {
                    System.out.println("Running SNMP Scanner");
                    break;
                }
                case "-n": {
                    System.out.println("Running NTP Scanner");
                    break;
                }
                case "-mc": {
                    System.out.println("Running MemCached Scanner Scanner");
                    break;
                }
                case "-w": {
                    break;
                }
            }
        }
       return;
    }
}
