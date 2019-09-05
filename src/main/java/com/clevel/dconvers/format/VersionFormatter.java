package com.clevel.dconvers.format;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.VersionConfigFile;
import com.clevel.dconvers.ngin.AppBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionFormatter extends AppBase {

    public VersionFormatter(Application application) {
        super(application, "VersionFormatter");
        valid = true;
    }

    public String format(VersionConfigFile versionConfigFile) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(versionConfigFile.getProjectName());

        String versionName = versionConfigFile.getVersionName();
        if (versionName != null && !versionName.isEmpty()) {
            stringBuilder.append(" ").append(versionConfigFile.getVersionName());
        } else {
            stringBuilder.append(" v.");
        }

        stringBuilder.append(" ").append(versionConfigFile.getVersionNumber())
                .append(".").append(versionConfigFile.getRevisionNumber())
                .append(".").append(versionConfigFile.getBuildNumber());

        String buildDate = versionConfigFile.getBuildDate();
        if (buildDate != null && !buildDate.isEmpty()) {
            stringBuilder.append(" build ").append(buildDate);
        }

        return stringBuilder.toString();
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(VersionFormatter.class);
    }
}
