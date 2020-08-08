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
                // We only care to read every other line of the source CSV
                if (index % 2 != 0) {
                    String[] values = line.split(",");
                    // For every third column in the CSV starting at column 16
                    for (int i = 16; i < values.length; i += 3) {
                        // add the string at index i to the pulseReadings arrayList
                        pulseReadings.add(values[i]);
                    }
                    final int MAIS_COLUMN_INDEX = 5; // The 5th column in the CSV should be the MAIS ID
                    map.put(values[MAIS_COLUMN_INDEX], pulseReadings);
                }
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
