package com.randerson;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayInputStream;
import java.util.Vector;

public class SftpFetcher {

    private final String remoteHost;
    private final String username;
    private final String password;
    public SftpFetcher(String remoteHost, String username, String password) {
        this.remoteHost = remoteHost;
        this.username = username;
        this.password = password;
    }

    public String getMostRecentFile() throws Exception {
        JSch jsch = new JSch();

        // Get path to local directory and store in variable
        String localDir = System.getProperty("java.io.tmpdir") + "/";
        System.out.printf("localDir set to %s%n", localDir);
        Session jschSession = null;
        ChannelSftp sftpChannel = null;
        try {
            String knownHostPublicKey = SecretClient.accessSecretVersion("southernco-host-public-key");
            jsch.setKnownHosts(new ByteArrayInputStream(knownHostPublicKey.getBytes()));
            jschSession = jsch.getSession(username, remoteHost);
            jschSession.setPassword(password);
            jschSession.connect();
            sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
            sftpChannel.connect();

            final String FTP_SOURCE_DIRECTORY = "/Output";
            Vector<ChannelSftp.LsEntry> vector = sftpChannel.ls(FTP_SOURCE_DIRECTORY);
            // get the last (most recent) file entry
            ChannelSftp.LsEntry lsEntry = vector.get(vector.size() -1);
            String filename = lsEntry.getFilename();
            String mostRecentFile = "/Output/" + filename;
            sftpChannel.get(mostRecentFile, localDir);
            // return the local path of the new file
            return localDir + filename;
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
