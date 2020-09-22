package com.randerson;

import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main implements BackgroundFunction<PubsubMessage> {

    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.updateXlsxFile(main.getMeterIdAndPulseReadings());
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
                updateXlsxFile(getMeterIdAndPulseReadings());
            } catch (Exception e) {
                e.printStackTrace();
                //TODO Handle exceptions
            }
        }
    }

    private HashMap<String, ArrayList<String>> getMeterIdAndPulseReadings() throws InterruptedException {
        System.out.println("Getting Meter ID and Pulse Readings");
        // California Meter Exchange Protocol
        String inputCmepFilePath = "";
        try {
            // fetch meter reading CSV file from FTP server
            String southencoRemoteHost = SecretClient.accessSecretVersion("southernco-remote-host");
            String southerncoUsername = SecretClient.accessSecretVersion("southernco-username");
            String southerncoPassword = SecretClient.accessSecretVersion("southernco-password");
            inputCmepFilePath = new SftpFetcher(southencoRemoteHost, southerncoUsername, southerncoPassword).getMostRecentFile();
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
        String egnyteRemoteHost = SecretClient.accessSecretVersion("egnyte-remote-host");
        String egnyteUsername = SecretClient.accessSecretVersion("egnyte-username");
        String egnytePassword = SecretClient.accessSecretVersion("egnyte-password");
        EgnyteClient egnyteClient = new EgnyteClient(egnyteRemoteHost, egnyteUsername, egnytePassword);
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(egnyteClient.downloadFile()));
        // Get sheet named after yesterday's date
        XSSFSheet yesterdaySheet = workbook.getSheet(isoFormat.format(currentTimestamp.minusDays(1)));
        XSSFSheet todaySheet = workbook.getSheet(isoFormat.format(currentTimestamp));
        // If we can't find a sheet with yesterday's date, use the last sheet in the workbook
        if (yesterdaySheet == null) {
            yesterdaySheet = workbook.getSheetAt(workbook.getNumberOfSheets()-1);
        }
        if (todaySheet == null) {
            final int FIRST_SHEET = 0;
            final int SECOND_SHEET = 1;
            todaySheet = workbook.cloneSheet(FIRST_SHEET, isoFormat.format(currentTimestamp));
            workbook.removeSheetAt(SECOND_SHEET);
        }

        ExcelWriter writer = new ExcelWriter();
        writer.writeArrayBasedOnMeter(meterIdAndPulseReadings, yesterdaySheet, todaySheet);
        String outputExcelPath = System.getProperty("java.io.tmpdir") + "/MeterReadSpreadsheet.xlsx";
        System.out.println("the output excel path is : " + outputExcelPath);
        workbook.write(new FileOutputStream(outputExcelPath));
        egnyteClient.uploadFile(outputExcelPath);
//        File resourcesDirectory = new File(System.getProperty("user.dir") + "/src/main/resources");
//        File[] files = resourcesDirectory.listFiles();
//        if (files != null) {
//            for (File f : files) {
//                if (f.getName().contains("GPC") && f.isFile() && f.exists()) {
//                    if (!f.delete()) {
//                        throw new Exception("File did not delete properly."); //TODO improve exception handling
//                    }
//                }
//            }
//        }
        System.out.println("Finished.");
    }

}
