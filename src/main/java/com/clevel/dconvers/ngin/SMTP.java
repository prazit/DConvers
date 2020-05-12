package com.clevel.dconvers.ngin;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.HostConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SMTP extends AppBase {

    private HostConfig smtpConfig;
    private int retry;

    private Session session;

    public SMTP(DConvers dconvers, String name, HostConfig smtpConfig) {
        super(dconvers, name);

        this.smtpConfig = smtpConfig;
        this.retry = smtpConfig.getRetry();

        log.debug("SMTP({}) smtpConfig({})", name, smtpConfig);
        valid = open();

        log.trace("smtp({}) is created", name);
    }

    public boolean open() {
        log.info("Try(remain:{}) to connect to SMTP({}), ", retry, name);
        retry--;

        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", smtpConfig.getHost());
            properties.put("mail.smtp.port", smtpConfig.getPort());
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");

            Authenticator auth = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpConfig.getUser(), smtpConfig.getPassword());
                }
            };

            session = Session.getInstance(properties, auth);
            for (Provider provider : session.getProviders()) {
                log.debug("SMTP.open.provider={}", provider);
            }

            if (dconvers.switches.isTest()) {
                String hardCodedEmail = "prazit@the-c-level.com";
                String hardCodedHtml = "<html><head><title>SMTP Test</title></head><body><h1>SMTP Test</h1><br/>" + smtpConfig + "</body></html>";
                if (!sendMessage("Test", hardCodedEmail, hardCodedEmail, null, null, true, hardCodedHtml)) {
                    return false;
                }
            }

            valid = true;
            log.info("Connected to smtp({})", name);
        } catch (Exception ex) {
            if (retry <= 0) {
                error("smtp({}) session connection is failed! {}", name, ex.getMessage());
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
        //smtpChannel.exit();
        log.info("Disconnected from smtp({}).", name);
    }

    public boolean sendMessage(String subject, String from, String to, String cc, String bcc, boolean isHtmlContent, String content) {
        if (!valid) {
            return false;
        }
        log.trace("sendMessage started.");

        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setSubject(subject.trim());
            mimeMessage.setFrom(new InternetAddress(removeQuotes(from), false));

            InternetAddress[] sendTo = InternetAddress.parse(removeQuotes(to), false);
            mimeMessage.setRecipients(Message.RecipientType.TO, sendTo);

            if (cc != null && !cc.isEmpty()) {
                InternetAddress[] sendToCC = InternetAddress.parse(removeQuotes(cc), false);
                mimeMessage.setRecipients(Message.RecipientType.CC, sendToCC);
            }

            if (bcc != null && !bcc.isEmpty()) {
                InternetAddress[] sendToBCC = InternetAddress.parse(removeQuotes(bcc), false);
                mimeMessage.setRecipients(Message.RecipientType.BCC, sendToBCC);
            }

            if (isHtmlContent) {
                mimeMessage.setContent(content, "text/html");
            } else {
                mimeMessage.setText(content);
            }

            log.debug("localAddress={}", InternetAddress.getLocalAddress(session));
        } catch (MessagingException ex) {
            error("SMTP({}).createMimeMessage is failed! {}", name, ex.getMessage());
            error("", ex);
            return false;
        }

        try {
            Transport.send(mimeMessage);

            /*DEBUG*/
            for (Address address : mimeMessage.getAllRecipients()) {
                log.debug("Recipient=(Type:{},Address:{})", address.getType(), address.toString());
            }
        } catch (Exception ex) {
            error("SMTP({}).sendMessage is failed! {}", name, ex.getMessage());
            error("", ex);
            return false;
        }

        log.trace("sendMessage successful.");
        return true;
    }

    public String removeQuotes(String to) {
        return to.replaceAll("[\"]", "");
    }

    public HostConfig getConfig() {
        return smtpConfig;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SMTP.class);
    }
}
