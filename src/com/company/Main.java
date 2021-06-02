package com.company;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Main class runs scanners typed by a user <br/>
 * Instance Variables: <br/>
 * List<ArgAvailable> argList - list of available args that user can type <br/>
 * static ArgAvailable help - something truly special
 */
public class Main {
    private static List<ArgAvailable> argList = new ArrayList<>();
    private static ArgAvailable help;

    /**
     * setArgList method fills the argList with allowed arguments by reading the file "args.txt"
     * @throws FileNotFoundException
     */
    private static void setArgList() throws FileNotFoundException {
        String fileName = System.getProperty("user.dir") + "/src/com/company/" + "args.txt";
        File file = new File(fileName);
        Scanner in = new Scanner(file);
        help = new ArgAvailable(in.nextLine(),in.nextLine());
        while(in.hasNext()){
            argList.add(new ArgAvailable(in.nextLine(),in.nextLine()));
        }
    }

    /**
     * verificationArgs method checks all arguments written by a user in the command line <br/>
     * @return boolean FALSE if user typed unavailable argument,
     *                       twice or more the same argument
     *                       --help or -h along with the other arguments
     *                 TRUE in other cases
     */
    private static boolean verificationArgs(String [] args){

        /* checking if --help or -h has been typed */
        if(help.match(args[0]) && args.length == 1) {
            args[0] = help.getShortcut();
            return true;
        }
        else if (help.match(args[0])) {
            return false;
        }
        /* checking the rest of arguments typed by an user */
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

    /**
     * Verify args written by a user<br/>
     * If everything is alright then it runs all wanted functions simultaneously<br/>
     * @param args arguments written by a user in the list of arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
       setArgList();
       int coreCount = Runtime.getRuntime().availableProcessors();
       try {
            if(!verificationArgs(args)){
                throw new Exception();
            }
       } catch (Exception e) {
           System.out.println("Wrong argument list. Type --help or -h to check command list");
           return;
       }
       boolean writingToFile = false;
        for (int i = 0; i < args.length; i++ ){
            if (args[i].equals("-w")) {
                writingToFile = true;
            }
        }
        IPv4Addresses FileWriting = new IPv4Addresses(writingToFile);
        for(int j = 0; j < args.length; j++) {
            switch (args[j]){
                case "-h": {
                    String fileName = System.getProperty("user.dir") + "/src/com/company/" + "help.txt";
                    File file = new File(fileName);
                    Scanner in = new Scanner(file);
                    while(in.hasNext()){
                        System.out.println(in.nextLine());
                    }
                    break;
                }
                case "-d": {
                    System.out.println("Running DNS Scanner");
                    ExecutorService serviceDNS = Executors.newFixedThreadPool(coreCount);
                    for(int i = 0 ; i < 224; i++){
                        serviceDNS.execute(new DNSScanner(i,i+1));
                    }
                    serviceDNS.shutdown();
                    break;
                }
                case "-s": {
                    System.out.println("Running SNMP Scanner");
                    ExecutorService serviceSNMP = Executors.newFixedThreadPool(224);
                    for(int i = 0 ; i < 224; i++){
                         serviceSNMP.execute(new SNMPScanner(i,i+1));
                    }
                    serviceSNMP.shutdown();
                    break;
                }
                case "-n": {
                    System.out.println("Running NTP Scanner");
                    ExecutorService serviceNTP = Executors.newFixedThreadPool(coreCount);
                    for(int i = 0 ; i < 224; i++){
                        serviceNTP.execute(new NTPScanner(i,i+1));
                    }
                    serviceNTP.shutdown();
                    break;
                }
                case "-mc": {
                    System.out.println("Running MemCached Scanner Scanner");
                    ExecutorService serviceMemCached = Executors.newFixedThreadPool(coreCount);
                    for(int i = 0 ; i < 224; i++){
                        serviceMemCached.execute(new MemCachedScanner(i,i+1));
                    }
                    serviceMemCached.shutdown();
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
