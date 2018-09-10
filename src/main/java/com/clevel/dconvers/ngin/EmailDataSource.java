package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.ngin.data.*;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.sql.Types;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

public class EmailDataSource extends DataSource {

    public EmailDataSource(Application application, String name, DataSourceConfig dataSourceConfig) {
        super(application, name, dataSourceConfig);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(EmailDataSource.class);
    }

    @Override
    public boolean open() {
        // nothing here, open file is in getDataTable function.
        return true;
    }

    /**
     * Please take care, POP3 will delete email from server.
     * Use IMAP protocol to avoid the lost of email.
     */
    private Properties getPOP3() {
        String host = "pop.gmail.com";
        String port = "995";

        Properties prop = new Properties();
        prop.put("mail.store.protocol", "pop3s");
        prop.put("mail.pop3.host", host);
        prop.put("mail.pop3.port", port);
        prop.put("mail.pop3.auth", true);
        prop.put("mail.smtp.starttls.enable", true);

        return prop;
    }

    private Properties getIMAP() {
        String host = "imap.gmail.com";
        String port = "993";

        Properties prop = new Properties();
        prop.put("mail.store.protocol", "imaps");
        prop.put("mail.imaps.host", host);
        prop.put("mail.imaps.port", port);
        prop.put("mail.imaps.auth", true);
        prop.put("mail.smtp.starttls.enable", true);

        return prop;
    }

    @Override
    public DataTable getDataTable(String tableName, String idColumnName, String mbox) {
        DataTable dataTable = new DataTable(mbox, "id");

        // filters
        String tokenInSubject = "0123456789ABCDEF".toUpperCase();   // null is no filter
        String tokenInBody = null;                                  // null is no filter
        boolean skipMarkAsRead = true;
        boolean attachmentOnly = true;
        int scanLimit = 10;
        int resultLimit = 1;

        // user password are required
        String user = "prazit@the-c-level.com";
        String password = "ritr6mT=boik=";

        // scan emails
        try {
            Properties props = getIMAP();
            String protocol = props.getProperty("mail.store.protocol");
            Session session = Session.getInstance(props, null);
            session.setDebug(true);

            Store store = session.getStore(protocol);
            store.connect(user, password);
            log.info("Email: connected to store({})", protocol);

            // ------------------
            Folder[] folders = store.getDefaultFolder().list("*");
            for (javax.mail.Folder folder : folders) {
                log.debug("Available folder: {} ({})", folder.getName(), folder.getFullName());
            }
            // ------------------

            Folder folder = store.getFolder(mbox);
            if (folder == null || !folder.exists()) {
                log.error("Email: Invalid folder({})", mbox);
                System.exit(1);
            }
            log.info("Email: folder({}) is exists", mbox);

            folder.open(Folder.READ_ONLY);
            log.info("Email: connected to folder({})", mbox);

            int messageCount = folder.getMessageCount();
            log.info("Email: folder({}) has {} messages", mbox, messageCount);

            Date firstDate = DateUtils.addMonths(new Date(), -3);
            MimeMultipart mimeMultipart;
            int partCount;

            for (int msgNumber = messageCount; msgNumber > 0; msgNumber--) {
                Message message = folder.getMessage(msgNumber);

                // filter by scanLimit
                if (scanLimit < 0) {
                    log.debug("Scan limit is exceed.");
                    break;
                }
                scanLimit--;

                // filter by sent date
                Date sentDate = message.getSentDate();
                log.debug("Email: sentDate = {}", sentDate);
                if (sentDate.before(firstDate)) {
                    log.debug("Send date is exceed.");
                    continue;
                }

                // filter by tokenInSubject in subject
                String subject = message.getSubject();
                if (tokenInSubject != null && subject.toUpperCase().indexOf(tokenInSubject) < 0) {
                    log.debug("subject({}) is not contains token({})", subject, tokenInSubject);
                    continue;
                }

                // filter by resultLimit
                if (resultLimit < 0) {
                    log.debug("Result limit is exceed.");
                    break;
                }
                resultLimit--;

                int columnIndex = 0;
                log.info("Email: message({}) = {}", msgNumber, message);

                String contentType = message.getContentType();
                Date receivedDate = message.getReceivedDate();
                Address recipient[] = message.getAllRecipients();
                Address from[] = message.getFrom();
                Address replyTo[] = message.getReplyTo();
                Flags flags = message.getFlags();

                if (contentType.startsWith("multipart")) {
                    mimeMultipart = (MimeMultipart) message.getContent();
                    partCount = mimeMultipart.getCount();
                    for (int i = 0; i < partCount; i++) {
                        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                        String disposition = bodyPart.getDisposition();
                        if (attachmentOnly && disposition != null && !disposition.trim().equalsIgnoreCase("ATTACHMENT")) {
                            log.debug("skip: part {}", disposition);
                            continue;
                        }

                        //Date receivedDate = bodyPart.getReceivedDate();
                        //String subject = bodyPart.getSubject();
                        //Address recipient[] = bodyPart.getAllRecipients();
                        //Address from[] = bodyPart.getFrom();
                        //Address replyTo[] = bodyPart.getReplyTo();
                        //Flags flags = bodyPart.getFlags();

                        int lineCount = bodyPart.getLineCount();
                        int size = bodyPart.getSize();
                        String description = bodyPart.getDescription();
                        String content = bodyPart.getContent().toString();
                        String filename = bodyPart.getFileName();
                        Enumeration headers = bodyPart.getAllHeaders();

                        DataRow dataRow = new DataRow(dataTable);
                        dataRow.putColumn("id", new DataLong(columnIndex++, Types.INTEGER, "id", (long) msgNumber * 10 + i + 1));
                        dataRow.putColumn("message_number", new DataLong(columnIndex++, Types.INTEGER, "message_number", (long) msgNumber));
                        dataRow.putColumn("size", new DataLong(columnIndex++, Types.INTEGER, "size", (long) size));
                        dataRow.putColumn("line_count", new DataLong(columnIndex++, Types.INTEGER, "line_count", (long) lineCount));
                        dataRow.putColumn("sent_date", new DataDate(columnIndex++, Types.DATE, "sent_date", sentDate));
                        dataRow.putColumn("received_date", new DataDate(columnIndex++, Types.DATE, "received_date", receivedDate));
                        dataRow.putColumn("content_type", new DataString(columnIndex++, Types.VARCHAR, "content_type", contentType));
                        dataRow.putColumn("subject", new DataString(columnIndex++, Types.VARCHAR, "subject", subject));
                        dataRow.putColumn("description", new DataString(columnIndex++, Types.VARCHAR, "description", description));
                        dataRow.putColumn("disposition", new DataString(columnIndex++, Types.VARCHAR, "disposition", disposition));
                        dataRow.putColumn("file_name", new DataString(columnIndex++, Types.VARCHAR, "file_name", filename));
                        dataRow.putColumn("flags", new DataString(columnIndex++, Types.VARCHAR, "flags", flagsToString(flags)));

                        dataRow.putColumn("from", new DataString(columnIndex++, Types.VARCHAR, "from", addressToString(from)));
                        dataRow.putColumn("recipient", new DataString(columnIndex++, Types.VARCHAR, "recipient", addressToString(recipient)));
                        dataRow.putColumn("reply_to", new DataString(columnIndex++, Types.VARCHAR, "reply_to", addressToString(replyTo)));

                        dataRow.putColumn("headers", new DataString(columnIndex++, Types.VARCHAR, "headers", headerToString(headers)));
                        dataRow.putColumn("content", new DataString(columnIndex++, Types.VARCHAR, "content", content));

                        dataTable.addRow(dataRow);
                    }
                    continue;
                }

                // below for non multipart message

                // filter by tokenInSubject in body message
                String content = message.getContent().toString();
                if (tokenInBody != null && content.toUpperCase().indexOf(tokenInBody) < 0) {
                    log.debug("body-message({}) is not contains token({})", content, tokenInBody);
                    continue;
                }

                // filter by SEEN flag (mark as READ)
                if (skipMarkAsRead && flags.contains(Flags.Flag.SEEN)) {
                    log.debug("Message({}) is mark as READ.", subject);
                    continue;
                }

                int lineCount = message.getLineCount();
                int size = message.getSize();
                String description = message.getDescription();
                String disposition = message.getDisposition();
                String filename = message.getFileName();
                Enumeration headers = message.getAllHeaders();

                DataRow dataRow = new DataRow(dataTable);
                dataRow.putColumn("id", new DataLong(columnIndex++, Types.INTEGER, "message_number", (long) msgNumber * 10));
                dataRow.putColumn("message_number", new DataLong(columnIndex++, Types.INTEGER, "message_number", (long) msgNumber));
                dataRow.putColumn("size", new DataLong(columnIndex++, Types.INTEGER, "size", (long) size));
                dataRow.putColumn("line_count", new DataLong(columnIndex++, Types.INTEGER, "line_count", (long) lineCount));
                dataRow.putColumn("sent_date", new DataDate(columnIndex++, Types.DATE, "sent_date", sentDate));
                dataRow.putColumn("received_date", new DataDate(columnIndex++, Types.DATE, "received_date", receivedDate));
                dataRow.putColumn("content_type", new DataString(columnIndex++, Types.VARCHAR, "content_type", contentType));
                dataRow.putColumn("subject", new DataString(columnIndex++, Types.VARCHAR, "subject", subject));
                dataRow.putColumn("description", new DataString(columnIndex++, Types.VARCHAR, "description", description));
                dataRow.putColumn("disposition", new DataString(columnIndex++, Types.VARCHAR, "disposition", disposition));
                dataRow.putColumn("file_name", new DataString(columnIndex++, Types.VARCHAR, "file_name", filename));
                dataRow.putColumn("flags", new DataString(columnIndex++, Types.VARCHAR, "flags", flagsToString(flags)));

                dataRow.putColumn("from", new DataString(columnIndex++, Types.VARCHAR, "from", addressToString(from)));
                dataRow.putColumn("recipient", new DataString(columnIndex++, Types.VARCHAR, "recipient", addressToString(recipient)));
                dataRow.putColumn("reply_to", new DataString(columnIndex++, Types.VARCHAR, "reply_to", addressToString(replyTo)));

                dataRow.putColumn("headers", new DataString(columnIndex++, Types.VARCHAR, "headers", headerToString(headers)));
                dataRow.putColumn("content", new DataString(columnIndex++, Types.VARCHAR, "content", content));

                dataTable.addRow(dataRow);
            }

        } catch (Exception ex) {
            log.error("EmailDataSource: Unexpected error has occurred", ex);
        }
        return dataTable;
    }

    private String headerToString(Enumeration headers) {
        StringBuilder stringBuilder = new StringBuilder();

        while (headers.hasMoreElements()) {
            Header header = (Header) headers.nextElement();
            stringBuilder.append(header.getName()).append("=").append(header.getValue()).append(",\n");
        }

        String string = stringBuilder.toString();
        if (string.length() > 2) {
            return string.substring(0, string.length() - 3);
        } else {
            return string;
        }
    }

    private String flagsToString(Flags flags) {
        StringBuilder stringBuilder = new StringBuilder("");

        /*Flags.Flag.DELETED;
        Flags.Flag.SEEN;
        Flags.Flag.RECENT;
        Flags.Flag.ANSWERED;
        Flags.Flag.FLAGGED;
        Flags.Flag.DRAFT;
        Flags.Flag.USER;*/

        if (flags.contains(Flags.Flag.DELETED)) stringBuilder.append("DELETED,\n");
        if (flags.contains(Flags.Flag.SEEN)) stringBuilder.append("SEEN,\n");
        if (flags.contains(Flags.Flag.RECENT)) stringBuilder.append("RECENT,\n");
        if (flags.contains(Flags.Flag.ANSWERED)) stringBuilder.append("ANSWERED,\n");
        if (flags.contains(Flags.Flag.FLAGGED)) stringBuilder.append("FLAGGED,\n");
        if (flags.contains(Flags.Flag.DRAFT)) stringBuilder.append("DRAFT,\n");
        if (flags.contains(Flags.Flag.USER)) stringBuilder.append("USER,\n");

        String string = stringBuilder.toString();
        if (string.length() > 2) {
            return string.substring(0, string.length() - 2);
        } else {
            return string;
        }
    }

    private String addressToString(Address addresses[]) {
        StringBuilder stringBuilder = new StringBuilder("");

        for (Address address : addresses) {
            stringBuilder.append(address.toString()).append(",\n");
        }

        String string = stringBuilder.toString();
        if (string.length() > 2) {
            return string.substring(0, string.length() - 3);
        } else {
            return string;
        }
    }

    @Override
    public void close() {
        // nothing here, close file is in getDataTable function.
    }

}