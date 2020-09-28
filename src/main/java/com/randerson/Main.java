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
        DateTimeFormatter yearMonthFormat = DateTimeFormatter.ofPattern("yyyy-MM");

        if (currentTimestamp.getHour()==0) { //TODO ask Ryan if this is needed. Is it harmful to run the application at midnight, or just not useful?
            System.out.println("hour is midnight. exiting.....");
            System.exit(0); //TODO handle this case
        }
        String egnyteRemoteHost = SecretClient.accessSecretVersion("egnyte-remote-host");
        String egnyteUsername = SecretClient.accessSecretVersion("egnyte-username");
        String egnytePassword = SecretClient.accessSecretVersion("egnyte-password");
        EgnyteClient egnyteClient = new EgnyteClient(egnyteRemoteHost, egnyteUsername, egnytePassword);
        ExcelWriter writer = new ExcelWriter();
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(egnyteClient.downloadFile(null)));

        ///// ARCHIVE LOGIC
        XSSFWorkbook archiveWorkbook;
        String archiveWorkbookPath = egnyteClient.downloadFile(yearMonthFormat.format(currentTimestamp));
        if (archiveWorkbookPath == null) { // If we didn't find a workbook for the current month, create one from the template file.
            archiveWorkbook = new XSSFWorkbook(System.getProperty("java.io.tmpdir") + "/Template.xlsx");
        } else {
            archiveWorkbook = new XSSFWorkbook(new FileInputStream(archiveWorkbookPath));
        }
        // WARNING this logic assumes that there were no errors processing the data on the last day of the previous month.
        // Handling this case would require loading up the workbook from last month and processing some of the data there
        // and some of the data in the newly created workbook for the current month.
        XSSFSheet archiveYesterdaySheet = null;
        if (currentTimestamp.getMonth() == currentTimestamp.minusDays(1).getMonth()) {
            archiveYesterdaySheet = archiveWorkbook.getSheet(isoFormat.format(currentTimestamp.minusDays(1)));
        }
        XSSFSheet archiveTodaySheet = archiveWorkbook.getSheet(isoFormat.format(currentTimestamp));
        System.out.println("Does it get here?");
        System.out.println(archiveTodaySheet);
        if (archiveTodaySheet == null) {
            System.out.println("listing of archive sheets");
            for (int i = 0; i < archiveWorkbook.getNumberOfSheets(); i++) {
                System.out.println(archiveWorkbook.getSheetAt(i).getSheetName());
            }
            final int FIRST_SHEET = 0;
            archiveTodaySheet = archiveWorkbook.cloneSheet(FIRST_SHEET, isoFormat.format(currentTimestamp));
        }
        writer.writeArrayBasedOnMeter(meterIdAndPulseReadings, archiveYesterdaySheet, archiveTodaySheet);
        String outputExcelPath = System.getProperty("java.io.tmpdir") + "/" + yearMonthFormat.format(currentTimestamp) + ".xlsx";
        System.out.println("the ARCHIVE output excel path is : " + outputExcelPath);
        archiveWorkbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
        archiveWorkbook.write(new FileOutputStream(outputExcelPath));
        egnyteClient.uploadFile(outputExcelPath, true);
        ///// END OF ARCHIVE LOGIC


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
            workbook.setActiveSheet(workbook.getNumberOfSheets()-1); // Set the last sheet in the workbook as the active one.
            workbook.removeSheetAt(SECOND_SHEET);
        }
        writer.writeArrayBasedOnMeter(meterIdAndPulseReadings, yesterdaySheet, todaySheet);
        outputExcelPath = System.getProperty("java.io.tmpdir") + "/MeterReadSpreadsheet.xlsx";
        System.out.println("the output excel path is : " + outputExcelPath);
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
        workbook.write(new FileOutputStream(outputExcelPath));
        egnyteClient.uploadFile(outputExcelPath, false);
        System.out.println("Finished.");
    }

}
