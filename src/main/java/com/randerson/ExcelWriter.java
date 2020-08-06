package com.randerson;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ExcelWriter {

    public void writeArrayBasedOnMeter(HashMap<String, ArrayList<String>> map, XSSFSheet sheet1, XSSFSheet sheet2){

        HashMap<String, ArrayList<Integer>> updatedMap;
        updatedMap = addValuesFromArray(map);
        XSSFSheet sheetOneRef = sheet1;

        for (Map.Entry<String, ArrayList<Integer>> entry : updatedMap.entrySet()){

            int columnNumber = 0;
            String maisNumber = entry.getKey();

            // TODO ctrom recommendation: convert switch statement to map or array that you can pull the correct value from
            switch (maisNumber) {
                case "23800371200":
                    // Baldwin / Cumming
                    columnNumber = 1;
                    break;
                case "23800371900":
                    // Baldwin / Douglasville
                    columnNumber = 2;
                    break;
                case "23800369600":
                    // Baldwin / Marietta
                    columnNumber = 3;
                    break;
                case "23791025011":
                    // Blount / Cumming
                    columnNumber = 4;
                    break;
                case "23791015007":
                    // CWM / Big Creek
                    columnNumber = 5;
                    break;
                case "23469100019":
                    // CWM / Bolton
                    columnNumber = 6;
                    break;
                case "23791010003":
                    // CWM / Cumming
                    columnNumber = 7;
                    break;
                case "23800209200":
                    // CWM / Forest Park
                    columnNumber = 8;
                    break;
                case "23420095001":
                    // CWM / Kennesaw
                    columnNumber = 9;
                    break;
                case "23760108008":
                    // CWM / Norcross
                    columnNumber = 10;
                    break;
                case "23233030026":
                    // CWM / StockBridge
                    columnNumber = 11;
                    break;
                case "23233240015":
                    // CWM / Tyrone
                    columnNumber = 12;
                    break;
                case "23730137511":
                    // ERS / Athens
                    columnNumber = 13;
                    break;
                case "23800378200":
                    // ERS / Friendship
                    columnNumber = 14;
                    break;
                case "23127100027":
                    // ERS / Lithonia
                    columnNumber = 15;
                    break;
                case "23800420400":
                    // ERS / Lithonia #2
                    columnNumber = 16;
                    break;
                case "23760040015":
                    // ERS / Norcross
                    columnNumber = 17;
                    break;
                case "23233192501":
                    // ERS / StockBridge
                    columnNumber = 18;
                    break;
                case "23127060011":
                    // ERS / Terminal
                    columnNumber = 19;
                    break;
                case "23800312100":
                    // ERS / Tyrone
                    columnNumber = 20;
                    break;
                case "23010505004":
                    // Metro / Conley
                    columnNumber = 21;
                    break;
                case "23760080009":
                    // Metro / Doraville
                    columnNumber = 22;
                    break;
                case "23469135001":
                    // TipTop / Marietta
                    columnNumber = 23;
                    break;
            }

            // the number 29 represents the 30th row in the excel file- 1:00AM reading
            int numberOfPulseReadings = entry.getValue().size();

            // if the number of pulse readings is less than 10,
            // then just add that number to 29. Otherwise, just use 39
            // as the stop number
            int goUpto = numberOfPulseReadings < 10 ? 29 + numberOfPulseReadings : 39;

            int j = 0;
            //int total = 0;
            for (int rowNumber = 29; rowNumber < goUpto; rowNumber++) {
                //total += Integer.parseInt(entry.getValue().get(j));
                sheet1.getRow(rowNumber).getCell(columnNumber).setCellValue(entry.getValue().get(j++));
                if (rowNumber == 38) {
                    rowNumber = 14;
                    goUpto = (numberOfPulseReadings - 10) + 15;
                    sheet1 = sheet2;
                }
            }
            
             sheet1 = sheetOneRef;

            //sheet1.getRow(39).getCell(columnNumber).setCellValue(total);
        }
    }

    public HashMap<String, ArrayList<Integer>> addValuesFromArray(HashMap<String, ArrayList<String>> map){

        HashMap<String, ArrayList<Integer>> theNewMap = new HashMap<>();
        ArrayList<String> placeHolderA;
        ArrayList<String> placeHolderB;
        ArrayList<Integer> arrToReturn = new ArrayList<>();
        HashSet<String> removedKeys = new HashSet<>();

        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()){

            // if key is not in hashmap, then proceed
            if (!removedKeys.contains(entry.getKey())){

                // if entry is an A or B entry
                if (entry.getKey().contains("A") || entry.getKey().contains("B")){
                    placeHolderA = entry.getValue();
                    String key = entry.getKey().substring(0, entry.getKey().length()-1);
                    if (entry.getKey().contains("A")){
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
                    }
                    int keySize = entry.getKey().length();

                    theNewMap.put(entry.getKey().substring(0,keySize-1), arrToReturn);
                    removedKeys.add(key);
                }

            else{
                // if entry is not an A or B entry
                // just add it to the new hashmap
                // that we will return
                if (!removedKeys.contains(entry.getKey())){
                    ArrayList<String> numbersAsString = entry.getValue();
                    ArrayList<Integer> integerArrayList = new ArrayList<>();
                    for (String s : numbersAsString) { integerArrayList.add(Integer.parseInt(s)); }
                    theNewMap.put(entry.getKey(), integerArrayList);
                }
            }
            }
        }
        return theNewMap;
    }
}





