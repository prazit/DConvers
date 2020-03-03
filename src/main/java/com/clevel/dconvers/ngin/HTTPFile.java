package com.clevel.dconvers.ngin;

import com.clevel.dconvers.Application;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HTTPFile extends AppBase {

    /**
     * @param application
     * @param httpFileName full path to the file.http
     */
    public HTTPFile(Application application, String httpFileName) {
        super(application, httpFileName);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(HTTPFile.class);
    }

    /**
     * Load file from HTTP/HTTPS, save on local outputPath and return local file name
     *
     * @return full path to the loaded file on the local environment.
     */
    public String downloadTo(String outputPath) {
        String httpFileName = name;

        File httpFile = new File(httpFileName);
        if (!httpFile.getName().endsWith(".http")) {
            error("HTP: http file is required, not for '{}'", httpFileName);
            return null;
        }
        if (!httpFile.exists()) {
            error("HTP: file not found '{}'", httpFileName);
            return null;
        }

        String httpFileString = application.currentConverter.valueFromFile(httpFileName);
        log.debug("downloadTo.httpFileString={}", httpFileString);

        HttpRequestString httpRequestString = new HttpRequestString(httpFileString);
        log.trace("downloadTo.httpRequestString is created");

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpRequestBase httpRequest = createHttpRequest(httpRequestString);
        if (httpRequest == null) {
            return null;
        }
        log.debug("downloadTo.httpRequestMethod={}", httpRequest.getMethod());

        String httpContentFileName;
        HttpResponse httpResponse = null;
        byte[] lf = "\n".getBytes();
        try {
            httpResponse = httpClient.execute(httpRequest);
            HttpEntity entity = httpResponse.getEntity();

            // debug only
            for (Header header : httpResponse.getAllHeaders()) {
                log.debug("httpResponse:header:{}={}", header.getName(), header.getValue());
            }
            Header contentType = entity.getContentType();
            log.debug("httpResponse:contentType={}>>{}", contentType.getValue(), contentType);

            // content file name
            String httpResponseFileName;
            if (httpResponse.containsHeader("Content-Disposition")) {
                String disposition = httpResponse.getFirstHeader("Content-Disposition").getValue();
                String fileName = disposition.replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");
                httpContentFileName = outputPath + fileName;
            } else {
                httpContentFileName = outputPath + httpRequest.getURI().getHost();
            }
            httpResponseFileName = httpContentFileName + ".http.response";

            // create response file.
            FileOutputStream responseFileOutputStream = new FileOutputStream(httpResponseFileName);
            responseFileOutputStream.write(httpResponse.getStatusLine().toString().getBytes());
            for (Header header : httpResponse.getAllHeaders()) {
                responseFileOutputStream.write(header.toString().getBytes());
                responseFileOutputStream.write(lf);
            }
            responseFileOutputStream.close();

            // create content file.
            FileOutputStream contentFileOutputStream = new FileOutputStream(httpContentFileName);
            entity.writeTo(contentFileOutputStream);
            contentFileOutputStream.close();

        } catch (HttpHostConnectException exception) {
            error("valueFromHttp is failed! {}", exception.getMessage());
            return null;

        } catch (IOException ex) {
            error("valueFromHttp is failed!", ex);
            return null;

        }

        return httpContentFileName;
    }

    private class HttpRequestString {
        String originalString;
        int originalStringLength;
        int currentOffset;

        public HttpRequestString(String originalString) {
            this.originalString = originalString;
            originalStringLength = originalString.length();
            currentOffset = 0;

            log.debug("HttpRequestString(length:{}, originalString:{})", originalStringLength, originalString);
        }

        /**
         * Skip comment line that startWith '#' symbol.
         *
         * @return return first line after comment block (without line separator), return null at EOF.
         */
        public String nextLine() {
            if (currentOffset > originalStringLength) {
                log.debug("HttpRequestString.nextLine.currentOffset({}) > originalStringLength({})", currentOffset, originalStringLength);
                return null;
            }

            int nextLineIndex = originalString.indexOf("\n", currentOffset);
            if (nextLineIndex < 0) {
                log.debug("HttpRequestString.nextLine.nextLineIndex={}", nextLineIndex);
                return null;
            }

            int nextLineOffset = nextLineIndex + 1;
            if (originalString.substring(currentOffset, currentOffset + 1).equals("#")) {
                currentOffset = nextLineOffset;
                return nextLine();
            }

            String line = originalString.substring(currentOffset, nextLineOffset - 1);
            currentOffset = nextLineOffset;
            if (line.trim().isEmpty()) {
                return nextLine();
            }

            return line;
        }
    }

    private HttpRequestBase createHttpRequest(HttpRequestString httpRequestString) {
        HttpRequestBase httpRequest;
        String urlLine = httpRequestString.nextLine();
        log.debug("createHttpRequest.urlLine={}", urlLine);
        if (urlLine == null) {
            error("createHttpRequest is failed by URL is null");
            return null;
        }

        PropertyValue property = new PropertyValue(urlLine, " ");
        if (property.name.equals("POST")) {
            httpRequest = new HttpPost(property.value);
        } else {
            httpRequest = new HttpGet(property.value);
        }

        String nextLine = httpRequestString.nextLine();
        while (nextLine != null) {
            property = new PropertyValue(nextLine, ":");
            if (property.name != null) {
                httpRequest.addHeader(property.name, property.value);
            }
            // TODO: POST need coding for multipart contents here.
        }

        return httpRequest;
    }

}
