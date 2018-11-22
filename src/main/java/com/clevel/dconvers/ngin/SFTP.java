package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.SFTPConfig;
import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFTP extends AppBase implements UserInfo {

    private SFTPConfig sftpConfig;

    private Session sftpSession;
    private ChannelSftp sftpChannel;


    public SFTP(Application application, String name, SFTPConfig sftpConfig) {
        super(application, name);

        this.sftpConfig = sftpConfig;
        log.debug("SFTP({}) sftpConfig({})", name, sftpConfig);
        valid = open();

        log.trace("sftp({}) is created", name);
    }

    public boolean open() {

        try {
            JSch jsch = new JSch();

            sftpSession = jsch.getSession(sftpConfig.getUser(), sftpConfig.getHost(), sftpConfig.getPort());
            sftpSession.setUserInfo(this);
            sftpSession.connect();

            //log.info("sftp({}) session is connected", name);
        } catch (Exception ex) {
            log.error("sftp({}) session connection is failed! {}", name, ex);
            return false;
        }

        try {

            Channel channel = sftpSession.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            log.info("Connected to sftp({})", name);
        } catch (Exception ex) {
            log.error("sftp({}) sftp channel connection is failed! {}", name, ex);
            return false;
        }

        return true;

    }

    public void close() {
        valid = false;
        sftpChannel.exit();
        log.info("Disconnected from sftp({}).", name);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SFTP.class);
    }

    @Override
    public String getPassphrase() {
        log.trace("SFTP.getPassphrase()");
        return null;
    }

    @Override
    public String getPassword() {
        log.trace("SFTP.getPassword()");
        return sftpConfig.getPassword();
    }

    @Override
    public boolean promptPassword(String message) {
        log.trace("SFTP.promptPassword(message:{})", message);
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        log.trace("SFTP.promptPassphrase(message:{})", message);
        return false;
    }

    @Override
    public boolean promptYesNo(String message) {
        log.trace("SFTP.promptYesNo(message:{})", message);
        return true;
    }

    @Override
    public void showMessage(String message) {
        log.info(message);
    }

    public boolean copyToSFTP(String localFile, String remoteFile) {
        log.trace("SFTP({}).copyToSFTP(localFile:{}, remoteFile:{})", name, localFile, remoteFile);

        if (!isValid()) {
            log.error("The SFTP({}) is invalid! can not copy local-file({}) to remote-file({}).", name, localFile, remoteFile);
            return false;
        }

        try {
            sftpChannel.put(localFile, remoteFile, ChannelSftp.OVERWRITE);
            log.info("SFTP({}) transfer file({}) to sftp({}) is completed", name, localFile, remoteFile);
        } catch (SftpException e) {
            log.error("SFTP({}) transfer file({}) to sftp({}) is failed, {}", name, localFile, remoteFile, e.getMessage());
            return false;
        }

        return true;
    }

}
