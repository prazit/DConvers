package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.OutputConfig;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.ngin.SMTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlEmailFormatter extends DataFormatter {

    private OutputConfig outputConfig;
    private boolean isHtml;
    private String smtpName;

    private String eol;
    private String valueSeparator;

    private SMTP smtp;

    public HtmlEmailFormatter(Application application, String name, OutputConfig outputConfig) {
        super(application, name, true);

        valueSeparator = ": ";

        this.outputConfig = outputConfig;
        eol = outputConfig.getEmailOutputEOL();
        smtpName = outputConfig.getEmailSMTP();
        isHtml = outputConfig.isEmailHtml();

        smtp = application.getSMTP(smtpName);
        valid = (smtp != null);

        outputType = "Email";
    }

    @Override
    public String format(DataRow row) {
        log.debug("HtmlEmailFormatter.format(DataRow:{})", row);

        /*all values below are 'Dynamic Value Enabled' that need to load again for every next row*/
        String subject = outputConfig.getEmailSubject();
        String from = outputConfig.getEmailFrom();
        String to = outputConfig.getEmailTo();
        String cc = outputConfig.getEmailCC();
        String bcc = outputConfig.getEmailBCC();
        String content = outputConfig.getEmailContent();

        if (!smtp.sendMessage(subject, from, to, cc, bcc, isHtml, content)) {
            return null;
        }

        String formatted = "";
        formatted += Property.SMTP.name() + valueSeparator + smtpName + "(" + smtp.getConfig() + ")" + eol;
        formatted += Property.SUBJECT.name() + valueSeparator + subject + eol;
        formatted += Property.FROM.name() + valueSeparator + from + eol;
        formatted += Property.TO.name() + valueSeparator + to + eol;
        formatted += Property.CC.name() + valueSeparator + cc + eol;
        formatted += Property.BCC.name() + valueSeparator + bcc + eol;
        formatted += Property.CONTENT.name() + "-" + Property.TYPE.name() + valueSeparator + (isHtml ? "text/html" : "text/plain") + eol;
        formatted += Property.CONTENT.name() + valueSeparator + content + eol;
        return formatted;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(HtmlEmailFormatter.class);
    }

}
