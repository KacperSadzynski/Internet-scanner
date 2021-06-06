package com.company;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
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
 * static ArgAvailable help - improves data validation for the variables -h, --help<br/>
 * static ArgAvailable demonstration - improves data validation for the variables -dm, --demonstration<br/>
 */
public class Main {
    private static final List<ArgAvailable> argList = new ArrayList<>();
    private static ArgAvailable help;
    private static ArgAvailable demonstration;

    /**
     * setArgList method fills the argList with allowed arguments by reading the file "args.txt"
     */
    private static void setArgList() throws FileNotFoundException {
        String fileName = System.getProperty("user.dir") + "/src/com/company/" + "args.txt";
        File file = new File(fileName);
        Scanner in = new Scanner(file);
        help = new ArgAvailable(in.nextLine(),in.nextLine());
        demonstration = new ArgAvailable(in.nextLine(), in.nextLine());
        while(in.hasNext()){
            argList.add(new ArgAvailable(in.nextLine(),in.nextLine()));
        }
    }

    /**
     * verificationArgs method checks all arguments written by a user in the command line <br/>
     * @return  FALSE if user typed unavailable argument,
     * twice or more the same argument,
     * --help or -h along with the other arguments,
     * TRUE in other cases
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
        /* checking if --demonstration or -dm has been typed */
        if(demonstration.match(args[0]) && args.length == 1) {
            args[0] = demonstration.getShortcut();
            return true;
        }
        else if (demonstration.match(args[0])) {
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
                    //checking if the "-w" has been typed and was the only argument - then return false
                    if(args[i].equals("-w")&&args.length==1){
                        return false;
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
     * Verifies args written by a user<br/>
     * If everything is right, then it runs all wanted functions simultaneously<br/>
     * @param args arguments written by a user in the list of arguments
     */
    public static void main(String[] args) throws IOException {
       setArgList();
       int coreCount = Runtime.getRuntime().availableProcessors(); //number of cores; in order to run optimal number of threads for a concrete computer
       //checking arguments
       try {
            if(!verificationArgs(args)){
                throw new Exception();
            }
       } catch (Exception e) {
           System.out.println("Wrong argument list. Type --help or -h to check command list");
           return;
       }
       //settings (according to arguments)
        boolean writingToFile = false;
        boolean isDemo = false;
        if (args[0].equals("-dm")){
            isDemo = true;
        }
        else{
            for (String arg : args) {
                if (arg.equals("-w")) {
                    writingToFile = true;
                    break;
                }
            }
        }
        //progress bar settings
        IPv4Addresses manager;
        if (!isDemo){
            long statesNumber = args.length;
            if (writingToFile) {
                statesNumber--;
            }
            statesNumber *= 221L*256L*256L*256L;
            manager = new IPv4Addresses(writingToFile, statesNumber);
        }
        //running main part of the program
        for (String arg : args) {
            switch (arg) {
                case "-h": {
                    String fileName = System.getProperty("user.dir") + "/src/com/company/" + "help.txt";
                    File file = new File(fileName);
                    Scanner in = new Scanner(file);
                    while (in.hasNext()) {
                        System.out.println(in.nextLine());
                    }
                    break;
                }
                case "-d": {
                    System.out.println("Running DNS Scanner");
                    ExecutorService serviceDNS = Executors.newFixedThreadPool(coreCount);
                    for (int i = 0; i < 224; i++) {
                        serviceDNS.execute(new DNSScanner(i, i + 1));
                    }
                    serviceDNS.shutdown();
                    break;
                }
                case "-s": {
                    System.out.println("Running SNMP Scanner");
                    coreCount = 1;
                    ExecutorService serviceSNMP = Executors.newFixedThreadPool(coreCount);
                    for (int i = 0; i < 224; i++) {
                        serviceSNMP.execute(new SNMPScanner(i, i + 1));
                    }
                    serviceSNMP.shutdown();
                    break;
                }
                case "-n": {
                    System.out.println("Running NTP Scanner");
                    ExecutorService serviceNTP = Executors.newFixedThreadPool(coreCount);
                    for (int i = 0; i < 224; i++) {
                        serviceNTP.execute(new NTPScanner(i, i + 1));
                    }
                    serviceNTP.shutdown();
                    break;
                }
                case "-mc": {
                    System.out.println("Running MemCached Scanner Scanner");
                    ExecutorService serviceMemCached = Executors.newFixedThreadPool(coreCount);
                    for (int i = 0; i < 224; i++) {
                        serviceMemCached.execute(new MemCachedScanner(i, i + 1));
                    }
                    serviceMemCached.shutdown();
                    break;
                }
                case "-dm": {
                    System.out.println("Running Demonstration Scan");
                    String fileName = System.getProperty("user.dir") + "/src/com/company/" + "demonstration.txt";
                    File file = new File(fileName);
                    Scanner in = new Scanner(file);
                    long howManyLines = Files.lines(Paths.get(fileName)).count();
                    manager = new IPv4Addresses(false, howManyLines);
                    manager.pb.draw();
                    DNSScanner dnsScanner = new DNSScanner(0, 0);
                    SNMPScanner snmpScanner = new SNMPScanner(0, 0);
                    NTPScanner ntpScanner = new NTPScanner(0, 0);
                    MemCachedScanner memCachedScanner = new MemCachedScanner(0, 0);
                    String address;
                    InetAddress current;
                    while (in.hasNext()) {
                        address = in.nextLine();
                        current = InetAddress.getByName(address);
                        dnsScanner.demonstrationScan(current);
                        snmpScanner.demonstrationScan(current);
                        ntpScanner.demonstrationScan(current);
                        memCachedScanner.demonstrationScan(current);
                        manager.pb.update();
                    }
                }
                case "-w": {
                    break;
                }
            }
        }
    }
}
