package com.randerson;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.text.DecimalFormat;
import java.util.*;

public class ExcelWriter {

    private static class MaisAssociation {
        private final String locationName;
        private final int columnNumber;

        public MaisAssociation(String locationName, int columnNumber) {
            this.locationName = locationName;
            this.columnNumber = columnNumber;
        }

        public String getLocationName() {
            return locationName;
        }

        public int getColumnNumber() {
            return columnNumber;
        }
    }

    Map<String, MaisAssociation> maisColumns = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("23800371200", new MaisAssociation("Baldwin / Cumming", 1)),
            new AbstractMap.SimpleEntry<>("23800371900", new MaisAssociation("Baldwin / Douglasville", 2)),
            new AbstractMap.SimpleEntry<>("23800369600", new MaisAssociation("Baldwin / Marietta", 3)),
            new AbstractMap.SimpleEntry<>("23791025011", new MaisAssociation("Blount / Cumming", 4)),
            new AbstractMap.SimpleEntry<>("23791015007", new MaisAssociation("CWM / Big Creek", 5)),
            new AbstractMap.SimpleEntry<>("23469100019", new MaisAssociation("CWM / Bolton", 6)),
            new AbstractMap.SimpleEntry<>("23791010003", new MaisAssociation("CWM / Cumming", 7)),
            new AbstractMap.SimpleEntry<>("23800209200", new MaisAssociation("CWM / Forest Park", 8)),
            new AbstractMap.SimpleEntry<>("23420095001", new MaisAssociation("CWM / Kennesaw", 9)),
            new AbstractMap.SimpleEntry<>("23760108008", new MaisAssociation("CWM / Norcross", 10)),
            new AbstractMap.SimpleEntry<>("23233030026", new MaisAssociation("CWM / StockBridge", 11)),
            new AbstractMap.SimpleEntry<>("23233240015", new MaisAssociation("CWM / Tyrone", 12)),
            new AbstractMap.SimpleEntry<>("23730137511", new MaisAssociation("ERS / Athens", 13)),
            new AbstractMap.SimpleEntry<>("23800378200", new MaisAssociation("ERS / Friendship", 14)),
            new AbstractMap.SimpleEntry<>("23127100027", new MaisAssociation("ERS / Lithonia", 15)),
            new AbstractMap.SimpleEntry<>("23800420400", new MaisAssociation("ERS / Lithonia #2", 16)),
            new AbstractMap.SimpleEntry<>("23760040015", new MaisAssociation("ERS / Norcross", 17)),
            new AbstractMap.SimpleEntry<>("23233192501", new MaisAssociation("ERS / StockBridge", 18)),
            new AbstractMap.SimpleEntry<>("23127060011", new MaisAssociation("ERS / Terminal", 19)),
            new AbstractMap.SimpleEntry<>("23800312100", new MaisAssociation("ERS / Tyrone", 20)),
            new AbstractMap.SimpleEntry<>("23010505004", new MaisAssociation("Metro / Conley", 21)),
            new AbstractMap.SimpleEntry<>("23760080009", new MaisAssociation("Metro / Doraville", 22)),
            new AbstractMap.SimpleEntry<>("23469135001", new MaisAssociation("TipTop / Marietta", 23))
    );

    public void writeArrayBasedOnMeter(HashMap<String, ArrayList<String>> map, XSSFSheet sheet1, XSSFSheet sheet2) {

        HashMap<String, ArrayList<Integer>> updatedMap;
        updatedMap = addValuesFromArray(map);
        XSSFSheet workingSheet = sheet1;

        for (Map.Entry<String, ArrayList<Integer>> entry : updatedMap.entrySet()) {
            // the number 29 represents the 30th row in the excel file- 1:00AM reading
            int numberOfPulseReadings = entry.getValue().size();

            // if the number of pulse readings is less than 10,
            // then just add that number to 29. Otherwise, just use 39
            // as the stop number
            int goUpto = numberOfPulseReadings < 10 ? 29 + numberOfPulseReadings : 39;

            int j = 0;
            //int total = 0;
            for (int rowNumber = 29; rowNumber < goUpto; rowNumber++) {
                if (workingSheet != null) {
                    String maisNumber = entry.getKey();
                    int number = entry.getValue().get(j);
                    try {
                        workingSheet.getRow(rowNumber).getCell(this.maisColumns.get(maisNumber).getColumnNumber()).setCellFormula("B2*" + number);
                    } catch (NullPointerException npe) {
                        System.err.println("Failed to retrieve a cell for MAIS " + maisNumber);
                        throw npe;
                    }
                }
                j++;
                if (rowNumber == 38) {
                    rowNumber = 14;
                    goUpto = (numberOfPulseReadings - 10) + 15;
                    workingSheet = sheet2;
                }
            }
            workingSheet = sheet1;
        }
    }

    public HashMap<String, ArrayList<Integer>> addValuesFromArray(HashMap<String, ArrayList<String>> map) {

        System.out.println("the hashmap values from the addValuesFromArray function in excelwriter class");

        map.entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " " + entry.getValue());
        });

        HashMap<String, ArrayList<Integer>> theNewMap = new HashMap<>();
        ArrayList<String> placeHolderA;
        ArrayList<String> placeHolderB;
        // ArrayList<Integer> arrToReturn = new ArrayList<>();
        HashSet<String> removedKeys = new HashSet<>();

        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {

            ArrayList<Integer> arrToReturn = new ArrayList<>();

            // if key is not in hashmap, then proceed
            if (!removedKeys.contains(entry.getKey())) {

                // if entry is an A or B entry
                if (entry.getKey().contains("A") || entry.getKey().contains("B")) {
                    placeHolderA = entry.getValue();
                    String key = entry.getKey().substring(0, entry.getKey().length() - 1);
                    if (entry.getKey().contains("A")) {
                        key = key + "B";
                    } else {
                        key = key + "A";
                    }
                    placeHolderB = map.get(key);
                    int maxLength = Math.max(placeHolderA.size(), placeHolderB.size());
                    for (int i = 0; i < maxLength; i++) {
                        // Integer v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;

                        Integer versionA = i < placeHolderA.size() ? Integer.parseInt(placeHolderA.get(i)) : 0;
                        Integer versionB = i < placeHolderB.size() ? Integer.parseInt(placeHolderB.get(i)) : 0;
                        int meterReadSum = versionA + versionB;
                        arrToReturn.add(meterReadSum);
                        System.out.println("the current entry is: ");
                        System.out.println(entry.getKey());
                        System.out.println(Arrays.asList(arrToReturn));
                    }
                    int keySize = entry.getKey().length();

                    theNewMap.put(entry.getKey().substring(0, keySize - 1), arrToReturn);
                    removedKeys.add(key);
                } else {
                    // if entry is not an A or B entry
                    // just add it to the new hashmap
                    // that we will return
                    if (!removedKeys.contains(entry.getKey())) {
                        ArrayList<String> numbersAsString = entry.getValue();
                        ArrayList<Integer> integerArrayList = new ArrayList<>();
                        for (String s : numbersAsString) {
                            integerArrayList.add(Integer.parseInt(s));
                        }
                        theNewMap.put(entry.getKey(), integerArrayList);
                    }
                }
            }
        }
        System.out.println("the hashmap values returned from the addValuesFromArray method");
        theNewMap.entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " " + entry.getValue());
        });
        return theNewMap;
    }
}
