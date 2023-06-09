package com.clevel.dconvers.ngin;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.HostConfig;
import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SFTP extends AppBase implements UserInfo {

    private HostConfig sftpConfig;
    private int retry;

    private Session sftpSession;
    private ChannelSftp sftpChannel;

    public SFTP(DConvers dconvers, String name, HostConfig sftpConfig) {
        super(dconvers, name);

        this.sftpConfig = sftpConfig;
        this.retry = sftpConfig.getRetry();

        log.debug("SFTP({}) sftpConfig({})", name, sftpConfig);
        valid = open();

        log.debug("sftp({}) is created", name);
    }

    public boolean open() {
        log.info("Try(remain:{}) to connect to SFTP({}), ", retry, name);
        retry--;

        try {
            JSch jsch = new JSch();

            sftpSession = jsch.getSession(sftpConfig.getUser(), sftpConfig.getHost(), sftpConfig.getPort());
            sftpSession.setUserInfo(this);
            sftpSession.connect();

            //log.info("sftp({}) session is connected", name);
        } catch (Exception ex) {
            if (retry <= 0) {
                error("sftp({}) session connection is failed! {}", name, ex.getMessage());
                return false;
            } else {
                return reopen();
            }
        }

        try {

            Channel channel = sftpSession.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
            valid = true;

            log.info("Connected to sftp({})", name);
        } catch (Exception ex) {
            if (retry <= 0) {
                error("sftp({}) sftp channel connection is failed! {}", name, ex);
                return false;
            } else {
                return reopen();
            }
        }

        return true;

    }

    public boolean reopen() {
        close();
        return open();
    }

    public void close() {
        if (!valid) {
            return;
        }

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
        log.debug("SFTP.getPassphrase()");
        return null;
    }

    @Override
    public String getPassword() {
        log.debug("SFTP.getPassword()");
        return sftpConfig.getPassword();
    }

    public HostConfig getSftpConfig() {
        return sftpConfig;
    }

    @Override
    public boolean promptPassword(String message) {
        log.debug("SFTP.promptPassword(message:{})", message);
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        log.debug("SFTP.promptPassphrase(message:{})", message);
        return false;
    }

    @Override
    public boolean promptYesNo(String message) {
        log.debug("SFTP.promptYesNo(message:{})", message);
        return true;
    }

    @Override
    public void showMessage(String message) {
        log.info(message);
    }

    public boolean copyToLocal(String remoteFile, String localFile) {
        log.debug("SFTP({}).copyToLocal(remoteFile:{},localFile:{})", name, remoteFile, localFile);

        if (!isValid()) {
            error("The SFTP({}) is invalid! can not transfer remote-file({}) to local-file({}).", name, remoteFile, localFile);
            return false;
        }

        try {
            File file = new File(localFile);
            if (file.exists()) {
                if (!file.delete()) {
                    error("Invalid filename of the localFile({}), the file is already exists", localFile);
                    return false;
                }

            } else {
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    if (!parent.mkdirs()) {
                        error("Invalid path to the localFile({})", localFile);
                        return false;
                    }
                }

            }
        } catch (Exception ex) {
            error("Invalid filename of the localFile({})", localFile);
            return false;
        }

        try {
            sftpChannel.get(remoteFile, localFile);
            log.info("SFTP({}) transfer remote-file({}) to local-file({}) is completed", name, remoteFile, localFile);
        } catch (SftpException e) {
            if (retry <= 0) {
                error("SFTP({}) transfer remote-file({}) to local-file({}) is failed! {}", name, remoteFile, localFile, e.getMessage());
                return false;
            } else {
                reopen();
                return copyToLocal(remoteFile, localFile);
            }
        }

        return true;
    }

    public boolean copyToSFTP(String localFile, String remoteFile) {
        log.debug("SFTP({}).copyToSFTP(localFile:{}, remoteFile:{})", name, localFile, remoteFile);

        if (!isValid()) {
            error("The SFTP({}) is invalid! can not transfer local-file({}) to remote-file({}).", name, localFile, remoteFile);
            return false;
        }

        try {
            sftpChannel.put(localFile, remoteFile, ChannelSftp.OVERWRITE);
            log.info("SFTP({}) transfer local-file({}) to remote-file({}) is completed", name, localFile, remoteFile);
        } catch (SftpException e) {
            if (retry <= 0) {
                error("SFTP({}) transfer local-file({}) to remote-file({}) is failed! {}", name, localFile, remoteFile, e.getMessage());
                return false;
            } else {
                reopen();
                return copyToSFTP(localFile, remoteFile);
            }
        }

        return true;
    }

}
