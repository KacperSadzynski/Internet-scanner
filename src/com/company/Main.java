package com.company;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
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
        File file = new File("args.txt");
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
    * UNUSED<br/>
    * For test purposes
    * @throws IOException
    */
    public static void searchingDNSAmplification() throws IOException{
            List<String> domains = new ArrayList<>();
            List<String> typeID = new ArrayList<>();
            File file3 = new File("DNS_Vulnerable.txt");
            if(file3.exists()){
                file3.delete();
            }
            File file = new File("domains.txt");
            Scanner in = new Scanner(file);
            while(in.hasNext()){
                domains.add(in.nextLine());
            }
            in.close();
            File file2 = new File("typeID.txt");
            Scanner in2 = new Scanner(file2);
            while(in2.hasNext()) {
                typeID.add(in2.nextLine());
            }
            int max = 0;
            for(String a : domains){
                for(String b : typeID){
                    int pom = 0;
                    DNSScanner scanner = new DNSScanner(0,256);
                    pom = scanner.testScan(a, b,"8.8.8.8");
                    if(pom>max){
                        max = pom;
                    }
                }
            }
            in2.close();
            System.out.println("Max length found: " + max);
    }

    /**
     * Verify args written by a user<br/>
     * If everything is alright then it runs all wanted functions simultaneously<br/>
     * @param args arguments written by a user in the list of arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
       IPv4Addresses setFlag = new IPv4Addresses(true);
       setArgList();
       int coreCount = Runtime.getRuntime().availableProcessors();
       System.out.println(coreCount);
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
/*
        ExecutorService serviceDNS = Executors.newFixedThreadPool(coreCount);
        for(int i = 0 ; i < 224; i++){
            serviceDNS.execute(new DNSScanner(i,i+1));
        }
        serviceDNS.shutdown();
*/

        ExecutorService serviceSNMP = Executors.newFixedThreadPool(1);
        for(int i = 0 ; i < 1; i++){
            serviceSNMP.execute(new SNMPScanner(192,193));
        }
        serviceSNMP.shutdown();


/*
        ExecutorService serviceNTP = Executors.newFixedThreadPool(coreCount);
        for(int i = 0 ; i < 224; i++){
            serviceNTP.execute(new NTPScanner(i,i+1));
        }
        serviceNTP.shutdown();

*/
/*
        ExecutorService serviceMemCached = Executors.newFixedThreadPool(coreCount);
        for(int i = 0 ; i < 224; i++){
            serviceMemCached.execute(new MemCachedScanner(i,i+1));
        }
        serviceMemCached.shutdown();


 */



        for(int i = 0; i < args.length; i++) {
            switch (args[i]){
                case "-h": {
                    /*
                    File file = new File("help.txt");
                    Scanner in = new Scanner(file);
                    while(in.hasNext()){
                        System.out.println(in.nextLine());
                    }

                     */
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
                    //SNMPScanner scanner = new SNMPScanner();
                    //scanner.scan();
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
