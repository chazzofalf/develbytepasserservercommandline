
package com.webuildsystems.chazzofalf.bytePasserServer.cmdline;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.ByteTosserServer;

public class CommandLineApp {
    public static void main(String[] args) throws Exception {
        String input = null;
        String output = null;
        boolean inputMode = false;
        boolean outputMode = false;
        int inputPort = -1;
        int outputPort = -1;
        for (String arg : args) {
            if (!inputMode && !outputMode && arg.toLowerCase().equals("-input")) {
                inputMode = true;
                continue;
            }
            if (!inputMode && !outputMode && arg.toLowerCase().equals("-output")) {
                outputMode = true;
                continue;
            }
            if (inputMode && input == null) {
                input = arg;
                inputMode = false;
                continue;
            }
            if (outputMode && output == null) {
                output = arg;
                outputMode = false;
                continue;
            }
            throw new RuntimeException("Invalid Parameters: Only -input and -output are allowed and only one of those are allowed.");
        }
        if (input == null || output == null) {
            System.out.println("Ports must be specified using -input and -output flags.");
        } else {
            try {
                inputPort = Integer.parseInt(input);
                if (inputPort < 1024 || inputPort >= 65536) {
                    throw new Exception();
                }
            }
            catch (Exception e) {
                throw new RuntimeException("Invalid value for -input specified. Only a valid integer between 1025 and 65535 inclusive is allowed.");
            }
            try {
                outputPort = Integer.parseInt(output);
                if (outputPort < 1024 || outputPort >= 65536) {
                    throw new Exception();
                }
            }
            catch (Exception e) {
                throw new RuntimeException("Invalid value for -output specified. Only a valid integer between 1025 and 65535 inclusive is allowed.");
            }
            ByteTosserServer bts = new ByteTosserServer();
            bts.setIncomingDataPort(inputPort);
            bts.setOutgoingDataPort(outputPort);
            bts.start();
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Hit Enter to close: ");
            lineReader.readLine();
        }
    }
}

