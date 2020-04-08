package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
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

    public SMTP(Application application, String name, HostConfig smtpConfig) {
        super(application, name);

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

    public boolean sendMessage(String subject, String to, String cc, String bcc, boolean isHtmlContent, String content) {
        if (!valid) {
            return false;
        }

        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setSubject(subject.trim());
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            if (cc != null && !cc.isEmpty()) {
                mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            }
            if (bcc != null && !bcc.isEmpty()) {
                mimeMessage.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
            }

            if (isHtmlContent) {
                mimeMessage.setContent(content, "text/html");
            } else {
                mimeMessage.setText(content);
            }
        } catch (MessagingException ex) {
            error("SMTP({}).sendMessage.createMimeMessage is failed! {}", name, ex.getMessage());
            return false;
        }

        try {
            Transport.send(mimeMessage);
        } catch (Exception ex) {
            error("SMTP({}).sendMessage is failed! {}", name, ex.getMessage());
            return false;
        }

        return true;
    }

    public HostConfig getConfig() {
        return smtpConfig;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(SMTP.class);
    }
}
