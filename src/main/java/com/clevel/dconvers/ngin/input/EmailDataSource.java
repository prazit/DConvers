package com.clevel.dconvers.ngin.input;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.ngin.data.*;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    /*private Properties getPOP3() {
        String host = "pop.gmail.com";
        String port = "995";

        Properties prop = new Properties();
        prop.put("mail.store.protocol", "pop3s");
        prop.put("mail.pop3.host", host);
        prop.put("mail.pop3.port", port);
        prop.put("mail.pop3.auth", true);
        prop.put("mail.smtp.starttls.enable", true);

        return prop;
    }*/
    private Properties getIMAPSSL() {
        String host = dataSourceConfig.getHost();
        String port = getPort(host, "993");

        Properties prop = new Properties();
        prop.put("mail.store.protocol", "imaps");
        prop.put("mail.imaps.host", removePort(host));
        prop.put("mail.imaps.port", port);
        prop.put("mail.imaps.auth", true);
        prop.put("mail.smtp.starttls.enable", true);

        return prop;
    }

    private Properties getIMAPLocal() {
        String host = dataSourceConfig.getHost();
        String port = getPort(host, "143");

        Properties prop = new Properties();
        prop.put("mail.store.protocol", "imap");
        prop.put("mail.imap.host", removePort(host));
        prop.put("mail.imap.port", port);
        prop.put("mail.imap.auth", true);

        return prop;
    }

    private String removePort(String host) {
        int portIndex = host.lastIndexOf(':');
        if (portIndex >= 0) {
            host = host.substring(0, portIndex);
        }
        return host;
    }

    private String getPort(String host, String defaultPort) {
        int portIndex = host.lastIndexOf(':');
        String port;
        if (portIndex < 0) {
            port = defaultPort;
        } else {
            port = host.substring(portIndex + 1);
            host = host.substring(0, portIndex);
        }
        log.debug("host is {} and port is {}", host, port);
        return port;
    }

    @Override
    public DataTable getDataTable(String tableName, String idColumnName, String query) {
        DataTable dataTable = new DataTable(query, "id");

        // filters
        String tokenInSubject = null;       // meaning of null is not filter
        String tokenInBody = null;          // meaning of null is not filter
        boolean skipMarkAsRead = false;
        boolean attachmentOnly = false;
        int scanLimit = 10;
        int resultLimit = 10;

        // queries
        String queries[] = query.split("[:]");
        if (queries.length > 1) {
            query = queries[0];
            tokenInSubject = queries[1];
            if (queries.length > 2) {
                scanLimit = Integer.parseInt(queries[2]);
            }
            if (queries.length > 3) {
                resultLimit = Integer.parseInt(queries[3]);
            } else {
                resultLimit = 1;
            }
        } else {
            tokenInSubject = null;
        }

        // user password are required
        String user = dataSourceConfig.getUser();
        String password = dataSourceConfig.getPassword();

        // output path
        String sourcePath = application.dataConversionConfigFile.getOutputSourcePath();

        // scan emails
        try {
            Properties props = dataSourceConfig.isSsl() ? getIMAPSSL() : getIMAPLocal();
            String protocol = props.getProperty("mail.store.protocol");
            Session session = Session.getInstance(props, null);
            session.setDebug(true);

            Store store = session.getStore(protocol);
            store.connect(user, password);
            log.info("Email: connected to store({})", protocol);

            // ------------------
            /*Folder[] folders = store.getDefaultFolder().list("*");
            for (javax.mail.Folder folder : folders) {
                log.debug("Available folder: {} ({})", folder.getName(), folder.getFullName());
            }*/
            // ------------------

            Folder folder = store.getFolder(query);
            if (folder == null || !folder.exists()) {
                log.error("Email: Invalid folder({})", query);
                System.exit(1);
            }
            log.info("Email: folder({}) is exists", query);

            folder.open(Folder.READ_ONLY);
            log.info("Email: connected to folder({})", query);

            int messageCount = folder.getMessageCount();
            log.info("Email: folder({}) has {} messages", query, messageCount);

            Date firstDate = DateUtils.addMonths(new Date(), -3);
            MimeMultipart mimeMultipart;
            int partCount;

            for (int msgNumber = messageCount; msgNumber > 0; msgNumber--) {
                Message message = folder.getMessage(msgNumber);

                // filter by resultLimit
                if (resultLimit < 0) {
                    log.debug("Result limit is exceed.");
                    break;
                }

                // filter by scanLimit
                if (scanLimit < 0) {
                    log.debug("Scan limit is exceed.");
                    break;
                }
                scanLimit--;

                // filter by sent date
                Date sentDate = message.getSentDate();
                log.debug("Email: sentDate = {}", sentDate);
                if (sentDate == null || sentDate.before(firstDate)) {
                    log.debug("Send date is exceed.");
                    continue;
                }

                // filter by tokenInSubject in subject
                String subject = message.getSubject();
                if (tokenInSubject != null && subject.indexOf(tokenInSubject) < 0) {
                    log.debug("subject({}) is not contains token({})", subject, tokenInSubject);
                    continue;
                }
                log.debug("found subject({})", subject);

                // filter by resultLimit
                resultLimit--;

                int columnIndex = 0;
                log.info("Email: message({}) = {}", msgNumber, message);

                String contentType = message.getContentType();
                Date receivedDate = message.getReceivedDate();
                Address recipient[] = message.getAllRecipients();
                Address from[] = message.getFrom();
                Address replyTo[] = message.getReplyTo();
                Flags flags = message.getFlags();

                // has some attached files
                if (contentType.startsWith("multipart")) {
                    mimeMultipart = (MimeMultipart) message.getContent();
                    partCount = mimeMultipart.getCount();
                    for (int i = 0; i < partCount; i++) {
                        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                        String disposition = message.getDisposition();
                        if (attachmentOnly && disposition != null && !disposition.trim().equalsIgnoreCase("ATTACHMENT")) {
                            log.debug("skip: part {}", disposition);
                            continue;
                        }
                        addDataRowByPart(dataTable, bodyPart, msgNumber * 10 + i, attachmentOnly, sourcePath);
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

    private void addDataRowByPart(DataTable dataTable, Part bodyPart, int msgNumber, boolean attachmentOnly, String sourcePath) throws MessagingException, IOException {
        String disposition = bodyPart.getDisposition();
        String contentType = bodyPart.getContentType();
        int lineCount = bodyPart.getLineCount();
        int size = bodyPart.getSize();
        String description = bodyPart.getDescription();
        Object contentObject = bodyPart.getContent();
        String filename = bodyPart.getFileName();
        Enumeration headers = bodyPart.getAllHeaders();

        // has some attached files
        if (contentType.startsWith("multipart")) {
            MimeMultipart mimeMultipart = (MimeMultipart) bodyPart.getContent();
            int partCount = mimeMultipart.getCount();
            for (int i = 0; i < partCount; i++) {
                if (attachmentOnly && disposition != null && !disposition.trim().equalsIgnoreCase("ATTACHMENT")) {
                    log.debug("skip: part {}", disposition);
                    continue;
                }
                Part childPart = mimeMultipart.getBodyPart(i);
                addDataRowByPart(dataTable, childPart, msgNumber * 10 + i, attachmentOnly, sourcePath);
            }
            return;
        }

        if (filename != null) {
            saveAttachedFile(sourcePath, "Attached_" + filename, contentObject);
        }
        String content = contentObject.toString();

        int columnIndex = 0;
        DataRow dataRow = new DataRow(dataTable);
        dataRow.putColumn("id", new DataLong(columnIndex++, Types.INTEGER, "id", (long) msgNumber));
        dataRow.putColumn("message_number", new DataLong(columnIndex++, Types.INTEGER, "message_number", (long) msgNumber));
        dataRow.putColumn("size", new DataLong(columnIndex++, Types.INTEGER, "size", (long) size));
        dataRow.putColumn("line_count", new DataLong(columnIndex++, Types.INTEGER, "line_count", (long) lineCount));
        dataRow.putColumn("sent_date", new DataDate(columnIndex++, Types.DATE, "sent_date", (Date) null));
        dataRow.putColumn("received_date", new DataDate(columnIndex++, Types.DATE, "received_date", (Date) null));
        dataRow.putColumn("content_type", new DataString(columnIndex++, Types.VARCHAR, "content_type", contentType));
        dataRow.putColumn("subject", new DataString(columnIndex++, Types.VARCHAR, "subject", null));
        dataRow.putColumn("description", new DataString(columnIndex++, Types.VARCHAR, "description", description));
        dataRow.putColumn("disposition", new DataString(columnIndex++, Types.VARCHAR, "disposition", disposition));
        dataRow.putColumn("file_name", new DataString(columnIndex++, Types.VARCHAR, "file_name", filename));
        dataRow.putColumn("flags", new DataString(columnIndex++, Types.VARCHAR, "flags", null));

        dataRow.putColumn("from", new DataString(columnIndex++, Types.VARCHAR, "from", null));
        dataRow.putColumn("recipient", new DataString(columnIndex++, Types.VARCHAR, "recipient", null));
        dataRow.putColumn("reply_to", new DataString(columnIndex++, Types.VARCHAR, "reply_to", null));

        dataRow.putColumn("headers", new DataString(columnIndex++, Types.VARCHAR, "headers", headerToString(headers)));
        dataRow.putColumn("content", new DataString(columnIndex++, Types.VARCHAR, "content", content));

        dataTable.addRow(dataRow);

    }

    private String md5(String sourceText) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(sourceText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            log.error("md5 encode failed: ", e);
            return sourceText;
        }
        return DatatypeConverter.printHexBinary(md.digest());
    }

    private String getFileExtension(String fileName) {
        int extIndex = fileName.lastIndexOf('.');
        if (extIndex < 0) {
            return "";
        }

        String ext = fileName.substring(extIndex);
        if (ext.length() > 10) {
            return "";
        }

        return ext;
    }

    /**
     * @param filename full path
     * @param content  boday part content object
     */
    private void saveAttachedFile(String path, String filename, Object content) {
        log.debug("saveAttachedFile: filename = {}", filename);

        File file;
        FileOutputStream fileOutputStream;
        try {
            String newName = md5(filename) + getFileExtension(filename);
            log.debug("saveAttachedFile(filename:{}, newname:{})", filename, newName);
            file = new File(path + newName);
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            log.error("saveAttachedFile: {}", e.getMessage());
            return;
        }

        int buffer;
        FilterInputStream filterInputStream = (FilterInputStream) content;
        try {
            while ((buffer = filterInputStream.read()) != -1) {
                fileOutputStream.write(buffer);
            }
        } catch (IOException e) {
            log.error("saveAttachedFile: ", e.getMessage());
            return;
        }

        try {
            fileOutputStream.close();
            filterInputStream.close();
        } catch (IOException e) {
            log.warn("saveAttachedFile: ", e.getMessage());
        }
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
            return string.substring(0, string.length() - 2);
        } else {
            return string;
        }
    }

    @Override
    public void close() {
        // nothing here, close file is in getDataTable function.
    }

}