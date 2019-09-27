package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionConfigFile extends ConfigFile {

    private String projectName;
    private String versionName;
    private String buildDate;

    /* major changes */
    private int versionNumber;

    /* minor changes */
    private int revisionNumber;

    /* fix bugs */
    private int buildNumber;

    public VersionConfigFile(Application application, String name) {
        super(application, name);

        log.debug("VersionConfigFile({}) = {}", name, this);
        log.trace("VersionConfigFile({}) is created.", name);
    }

    @Override
    protected boolean loadProperties() {
        log.trace("VersionConfigFile.loadProperties.");

        projectName = getPropertyString(properties, Property.PROJECT_NAME.key());
        versionName = getPropertyString(properties, Property.VERSION_NAME.key());
        buildDate =  getPropertyString(properties, Property.BUILD_DATE.key());

        versionNumber = properties.getInt(Property.VERSION_NUMBER.key());
        revisionNumber = properties.getInt(Property.REVISION_NUMBER.key());
        buildNumber = properties.getInt(Property.BUILD_NUMBER.key());

        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(VersionConfigFile.class);
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("projectName", projectName)
                .append("versionName", versionName)
                .append("buildDate", buildDate)
                .append("versionNumber", versionNumber)
                .append("revisionNumber", revisionNumber)
                .append("buildNumber", buildNumber)
                .append("name", name)
                .append("valid", valid)
                .toString()
                .replace('=', ':');
    }
}
