package com.randerson;

import com.jcraft.jsch.*;
import java.util.Vector;

// TODO ctrom recommendation: The name of this class doesn't tell me a lot by itself. Maybe another name would be more suitable?
public class Jsch {


    // TODO ctrom recommendation: managing secrets effectively is challenging. Unless you're prepared to manage something more robust, I suggest you pass in these values at runtime. This dramatically reduces opportunities for Information Leakage (https://en.wikipedia.org/wiki/Information_leakage)
    private String remoteHost = "xtr.southernco.com";
    private String username = "Fireside_NG_Project";
    private String password = "*********";


    private ChannelSftp setupJsch() throws JSchException {
        JSch jsch = new JSch();
        // TODO ctrom recommendation: directly accessing files like this is very error prone. Like the secrets above, I suggest you pass in the file parameter at runtime.
        jsch.setKnownHosts("/Users/nryaand/.ssh/known_hosts");
        Session jschSession = jsch.getSession(username, remoteHost);
        jschSession.setPassword(password);
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    public String getFile() throws JSchException, SftpException {

        // log into SFTP Server
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        // Get path to local directory and store in variable
        String localDir = System.getProperty("user.dir") + "/src/main/resources/";

        // execute ls command on sftp server and store output in vector variable
        // TODO ctrom recommendation: Vector is a generic which should be parameterized - Vector<ChannelSftp.LsEntry> maybe?
        Vector vector = channelSftp.ls("/Output");

        // get the last (most recent) string from the vector
        // TODO ctrom recommendation: typing the vector correctly should allow you to avoid casting this variable. Casting can produce unclear errors and should be done with caution.
        ChannelSftp.LsEntry lsEntry = (ChannelSftp.LsEntry) vector.get(vector.size() -1);

        // convert the object to a string
        String filename = lsEntry.getFilename();

        // add the string '/Output/' to the front of the file name
         String mostRecentFile = "/Output/" + filename;

        // tell the sftp client to get that file and store it locally
         channelSftp.get(mostRecentFile, localDir);

        // exit the sftp client
        channelSftp.exit();

        // return the path to where the file will exist locally
         return localDir + filename;
    }
}
