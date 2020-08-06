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
                // if the index variable is odd, go into if statement // TODO ctrom recommendation: the portion after the comma here should be implied. Better comment might explain why we care if the index variable is odd.
                if (index % 2 != 0) {
                    // split the line on commas
                    String[] values = line.split(",");
                    for (int i = 16; i < values.length; i += 3) { // TODO ctrom recommendation: These seem to be more magic numbers. Why 16? Why 3? Declaring these as variables would at least give you an opportunity to name them or their purpose.
                        // add the string at index 16 of the values string array to the pulseReadings arraylist
                        // TODO ctrom recommendation: "add the string at index 16"? This is a loop, right? So each iteration of the loop should have a different 'i' value? If not, is the loop needed at all?
                        pulseReadings.add(values[i]);
                    }
                    // values[5] is the MAIS number.
                    // map.put(values[5], pulseReadings); // TODO ctrom recommendation: An example of how you might do this without the magic number:
                    final int MAIS_NUMBER = 5; // The 5th X should be the Y thing in the source data.
                    map.put(values[MAIS_NUMBER], pulseReadings);
                }
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
