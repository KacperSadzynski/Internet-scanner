package com.company;


import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main {

    private static List<ArgAvailable> argList = new ArrayList<>();

    private static void setArgList() throws FileNotFoundException {
        File file = new File("args.txt");
        Scanner in = new Scanner(file);
        while(in.hasNext()){
            argList.add(new ArgAvailable(in.nextLine(),in.nextLine()));
        }
    }

    public static void main(String[] args) throws IOException {
       setArgList();
       try {
           for (String arg : args) {
              
           }
           DatagramSocket socket = new DatagramSocket(4445);

       } catch (Exception e) {
           System.out.println("Wrong argument list. Type --help or -h to check command list");
           //e.printStackTrace();
       }
    }

}
