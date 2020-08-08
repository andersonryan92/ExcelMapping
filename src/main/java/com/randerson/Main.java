package com.randerson;

import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;

public class Main implements BackgroundFunction<PubsubMessage> {
//public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.updateXlsxFile(main.getMeterIdAndPulseReadings(args));
        } catch (Exception e) {
            e.printStackTrace();
            //TODO Handle exceptions
        }
    }

    @Override
    public void accept(PubsubMessage message, Context context) {
        if (message != null && message.getData() != null) {
            new String(
                    Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8);
            try {
//                updateXlsxFile(getMeterIdAndPulseReadings());
            } catch (Exception e) {
                e.printStackTrace();
                //TODO Handle exceptions
            }
        }
    }

    private HashMap<String, ArrayList<String>> getMeterIdAndPulseReadings(String[] args) throws InterruptedException {
        System.out.println("Getting Meter ID and Pulse Readings");
        // California Meter Exchange Protocol
        String inputCmepFilePath = "";
        try {
            // fetch meter reading CSV file from FTP server
            String remoteHost = "xtr.southernco.com";
            String username = "Fireside_NG_Project";
            String password = args[0];
            inputCmepFilePath = new SftpFetcher(remoteHost, username, password).getMostRecentFile();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO handle exception
        }
        CsvParser csvParser = new CsvParser(inputCmepFilePath);
        return csvParser.getMeterIdAndPulseReadings();
    }

    private void updateXlsxFile(HashMap<String, ArrayList<String>> meterIdAndPulseReadings) throws Exception { //TODO handle exceptions
        System.out.println("Updating XLSX File");
        ZonedDateTime currentTimestamp = ZonedDateTime.now(ZoneId.of("America/New_York"));
        DateTimeFormatter isoFormat = DateTimeFormatter.ISO_LOCAL_DATE;

        if (currentTimestamp.getHour()==0) { //TODO ask Ryan if this is needed. Is it harmful to run the application at midnight, or just not useful?
            System.out.println("hour is midnight. exiting.....");
            System.exit(0); //TODO handle this case
        }
        String outputExcelPath = System.getProperty("user.dir") + "/src/main/resources/MeterReadDataSampleSpreadsheet.xlsx";
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(outputExcelPath));
        // Get sheet named after yesterday's date
        XSSFSheet yesterdaySheet = workbook.getSheet(isoFormat.format(currentTimestamp.minusDays(1)));
        XSSFSheet todaySheet = workbook.getSheet(isoFormat.format(currentTimestamp));
        // If we can't find a sheet with yesterday's date, use the last sheet in the workbook
        if (yesterdaySheet == null) {
            yesterdaySheet = workbook.getSheetAt(workbook.getNumberOfSheets()-1);
        }
        if (todaySheet == null) {
            final int FIRST_SHEET = 0;
            todaySheet = workbook.cloneSheet(FIRST_SHEET, isoFormat.format(currentTimestamp));
        }

        ExcelWriter writer = new ExcelWriter();
        writer.writeArrayBasedOnMeter(meterIdAndPulseReadings, yesterdaySheet, todaySheet);
        workbook.write(new FileOutputStream(outputExcelPath));
        File resourcesDirectory = new File(System.getProperty("user.dir") + "/src/main/resources");
        File[] files = resourcesDirectory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().contains("GPC") && f.isFile() && f.exists()) {
                    if (!f.delete()) {
                        throw new Exception("File did not delete properly."); //TODO improve exception handling
                    }
                }
            }
        }
        System.out.println("Finished. Done.");
    }

}
