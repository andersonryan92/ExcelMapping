package com.randerson;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Vector;

public class EgnyteClient {

    private final String remoteHost;
    private final String username;
    private final String password;
    public EgnyteClient(String remoteHost, String username, String password) {
        this.remoteHost = remoteHost;
        this.username = username;
        this.password = password;
    }

    public void uploadFile(String filePath, boolean archive) throws Exception {
        JSch jsch = new JSch();

        // Get path to local directory and store in variable
        String localDir = System.getProperty("java.io.tmpdir") + "/";
        System.out.printf("localDir set to %s%n", localDir);
        System.out.println();
        System.out.println("user.dir is: " + System.getProperty("user.dir"));
        System.out.println("user.home is: " + System.getProperty("user.home"));
        System.out.println("os.name is: " + System.getProperty("os.name"));
        System.out.println("os.version is: " + System.getProperty("os.version"));
        System.out.println("user.name is: " + System.getProperty("user.name"));


        Path path = FileSystems.getDefault().getPath(".").toAbsolutePath();
        System.out.println("path is: " + path.toString());
        Session jschSession = null;
        ChannelSftp sftpChannel = null;
        try {
            String knownHostPublicKey = SecretClient.accessSecretVersion("egnyte-host-public-key");
            jsch.setKnownHosts(new ByteArrayInputStream(knownHostPublicKey.getBytes()));
            jschSession = jsch.getSession(username, remoteHost);
            jschSession.setPassword(password);
            jschSession.connect();
            sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
            sftpChannel.connect();

            String FTP_DIRECTORY;
            if (archive) {
                FTP_DIRECTORY = "/Shared/IT Meter Data/Archive";
            } else {
                FTP_DIRECTORY = "/Shared/IT Meter Data";
            }
            sftpChannel.put(filePath, FTP_DIRECTORY);
//            Vector<ChannelSftp.LsEntry> vector = sftpChannel.ls(FTP_DIRECTORY);
//            for (ChannelSftp.LsEntry entry : vector) {
//                System.out.println(entry);
//            }
            // return the local path of the new file
        } catch (Exception e){
            e.printStackTrace(); //TODO handle exception
            throw new Exception("File fetch failed.");
        } finally {
            // Close connections
            if (sftpChannel != null) {
                sftpChannel.exit();
            }
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }
    }

    public String downloadFile(String monthYear) throws Exception {
        JSch jsch = new JSch();

        // Get path to local directory and store in variable
        String localDir = System.getProperty("java.io.tmpdir") + "/";
        System.out.printf("localDir set to %s%n", localDir);
        Session jschSession = null;
        ChannelSftp sftpChannel = null;
        try {
            String knownHostPublicKey = SecretClient.accessSecretVersion("egnyte-host-public-key");
            jsch.setKnownHosts(new ByteArrayInputStream(knownHostPublicKey.getBytes()));
            jschSession = jsch.getSession(username, remoteHost);
            jschSession.setPassword(password);
            jschSession.connect();
            sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
            sftpChannel.connect();

            final String FTP_DIRECTORY;
            String outputPath;
            if (monthYear == null) { // If a month and year weren't specified, get the master (current) file
                FTP_DIRECTORY = "/Shared/IT Meter Data/";
                outputPath = System.getProperty("java.io.tmpdir") + "/MeterReadSpreadsheet.xlsx";
                sftpChannel.get(FTP_DIRECTORY+"MeterReadSpreadsheet.xlsx", outputPath);
            } else { // If a month and year were specified, fetch the archive file accordingly
                FTP_DIRECTORY = "/Shared/IT Meter Data/Archive/";
                outputPath = System.getProperty("java.io.tmpdir") + "/" + monthYear + ".xlsx";
                try {
                    sftpChannel.get(FTP_DIRECTORY+monthYear, outputPath);
                } catch (SftpException se) {
                    if (se.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        sftpChannel.get(FTP_DIRECTORY+"Template.xlsx", System.getProperty("java.io.tmpdir") + "/Template.xlsx");
                        // File does not exist on the server
                        return null;
                    } else {
                        System.out.println("Error occurred");
                        se.printStackTrace();
                        throw se;
                    }
                }
            }
            return outputPath;
        } catch (Exception e){
            e.printStackTrace(); //TODO handle exception
            throw new Exception("File fetch failed.");
        } finally {
            // Close connections
            if (sftpChannel != null) {
                sftpChannel.exit();
            }
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }
    }
}



// 1. Download CSV data
// 2. Transform CSV data
// 3. Download Master spread sheet
// 3.1. Download archive spread sheet
// 4. Write data to Master spread sheet
// 4.1. Write data to archive spread sheet
// 5. Upload Master spread sheet
// 5.1. Upload archive spreadsheet

//      /Shared/IT Meter Data/MeterReadSpreadsheet.xlsx
//      /Shared/IT Meter Data/Archive/2020-01.xlsx
//      /Shared/IT Meter Data/Archive/Template.xlsx