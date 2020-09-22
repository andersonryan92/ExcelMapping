package com.randerson;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

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

    public void uploadFile(String filePath) throws Exception {
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

            final String FTP_DIRECTORY = "/Shared/IT Meter Data";
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

    public String downloadFile() throws Exception {
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

            final String FTP_DIRECTORY = "/Shared/IT Meter Data/";
//            Vector<ChannelSftp.LsEntry> vector = sftpChannel.ls(FTP_DIRECTORY);
//            for (ChannelSftp.LsEntry entry : vector) {
//                System.out.println(entry);
//            }
            String outputPath = System.getProperty("java.io.tmpdir") + "/MeterReadSpreadsheet.xlsx";
            sftpChannel.get(FTP_DIRECTORY+"MeterReadSpreadsheet.xlsx", outputPath);
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
