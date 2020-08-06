package com.randerson;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        // create a date format object
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // set the timezone as eastern time
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        // create new date object
        Date date = new Date();
        // get todays date
        String[] todaysDateArray = dateFormat.format(date).split(" ");
        // get a calendar instance, which defaults to "now"
        Calendar calendar = Calendar.getInstance();
        // subtract one day to the date/calendar to get yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        // now get "yesterday"
        Date yesterday = calendar.getTime();
        // get yesterday's date
        String[] yesterdayDateArray = dateFormat.format(yesterday).split(" ");


        if (todaysDateArray[1].split(":")[0].equals("00")){
            System.out.println("hour is midnight. exiting.....");
            System.exit(0);
        }


        // creating jsch object. This is the class responsible for
        // fetching the CSV file from the SFTP server
        Jsch jsch = new Jsch();

        // Initializing input string variable
        String inputCmepFilePath = "";

        try {
            // getting file from SFTP server by calling getFile method
            // on the jsch object
            inputCmepFilePath = jsch.getFile();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            // TODO ctrom recommendation: should you do anything else here after printing the stack trace? Will the program run if an exception is thrown here? If not, you should exit gracefully.
        }


        // TODO ctrom recommendation: comments that repeat the code are generally not useful. Comments that explain WHY the code does what it does are more valuable.
        // Example, change:
        // pausing 2 seconds for file to set in directory
        // to:
        // sleeping until file fetch complete
        Thread.sleep(2000); // TODO ctrom recommendation: this looks like a magic number (https://en.wikipedia.org/wiki/Magic_number_(programming)). Can you guarantee that 2000ms is enough? What if it isn't enough for some reason? How will you handle that?
        // setting the path to the excel file
        String outputExcelPath = System.getProperty("user.dir") + "/src/main/resources/MeterReadDataSampleSpreadsheet.xlsx";
        // creating excelwriter object // TODO ctrom recommendation: see note above about comments that repeat the code
        ExcelWriter writer = new ExcelWriter(); // TODO ctrom recommendation: this variable isn't used here, so its purpose is unclear. Recommend moving declaration/instantiation closer to initial usage.
        // creating csvParser object. passing csv file path to the constructor // TODO ctrom recommendation: see note above about comments that repeat the code
        CsvParser csvParser = new CsvParser(inputCmepFilePath);
        // getting hashmap of values returned from csvParser method
        HashMap<String, ArrayList<String>> map = csvParser.getMeterIdAndPulseReadings();



        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(outputExcelPath));
        XSSFSheet yesterdaySheet = workbook.getSheet(yesterdayDateArray[0].replace('/','-'));

        XSSFSheet todaySheet = workbook.getSheet(todaysDateArray[0].replace('/','-'));
        if (todaySheet == null){
            todaySheet = workbook.cloneSheet(0, todaysDateArray[0].replace('/','-'));
        }


        writer.writeArrayBasedOnMeter(map,yesterdaySheet, todaySheet);
        workbook.write(new FileOutputStream(outputExcelPath));
        File resourcesDirectory = new File(System.getProperty("user.dir") + "/src/main/resources");
        for (File f : resourcesDirectory.listFiles()) {
            if (f.getName().contains("GPC") && f.isFile() && f.exists()){
                f.delete();
            }
        }
        System.out.println("Finished. Done.");
        System.exit(0);
    }
}
