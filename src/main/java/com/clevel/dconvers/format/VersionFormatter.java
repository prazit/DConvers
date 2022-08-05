package com.clevel.dconvers.format;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.VersionConfigFile;
import com.clevel.dconvers.ngin.AppBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionFormatter extends AppBase {

    public VersionFormatter(DConvers dconvers) {
        super(dconvers, "VersionFormatter");
        valid = true;
    }

    public String versionNumber(VersionConfigFile versionConfigFile) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(versionConfigFile.getVersionNumber())
                .append(".").append(versionConfigFile.getRevisionNumber())
                .append(".").append(versionConfigFile.getBuildNumber());

        return stringBuilder.toString();
    }

    public String versionString(VersionConfigFile versionConfigFile) {
        if(!versionConfigFile.isValid()) return "DEMO";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(versionConfigFile.getProjectName());

        String versionName = versionConfigFile.getVersionName();
        if (versionName != null && !versionName.isEmpty()) {
            stringBuilder.append(" ").append(versionConfigFile.getVersionName());
        } else {
            stringBuilder.append(" v.");
        }

        stringBuilder.append(" ").append(versionNumber(versionConfigFile));
        String buildDate = versionConfigFile.getBuildDate();
        if (buildDate != null && !buildDate.isEmpty()) {
            stringBuilder.append(" build ").append(buildDate);
        }

        return stringBuilder.toString();
    }

    public VersionConfigFile versionConfigFile(String versionFileName) {
        return new VersionConfigFile(dconvers, versionFileName);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(VersionFormatter.class);
    }
}
