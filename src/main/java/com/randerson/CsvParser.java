package com.randerson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CsvParser {

    private final String path;
    public CsvParser(String path) {
        this.path = path;
    }

    public HashMap<String, ArrayList<String>> getMeterIdAndPulseReadings(){

        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        String line = "";

        try {

            BufferedReader br = new BufferedReader(new FileReader(path));
            int index = 0;
            while ((line = br.readLine()) != null) {
                ArrayList<String> pulseReadings = new ArrayList<String>();
                // if the index variable is odd, go into if statement
                if (index % 2 != 0) {
                    // split the line on commas
                    String[] values = line.split(",");
                    for (int i = 16; i < values.length; i += 3) {
                        // add the string at index 16 of the values string array to the pulseReadings arraylist
                        pulseReadings.add(values[i]);
                    }
                    // values[5] is the MAIS number.
                    map.put(values[5], pulseReadings);
                }
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
