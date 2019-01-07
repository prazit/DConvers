package com.clevel.dconvers.ngin.input;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.ngin.data.DataDate;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.util.*;

public class DirDataSource extends DataSource {

    public DirDataSource(Application application, String name, DataSourceConfig dataSourceConfig) {
        super(application, name, dataSourceConfig);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(DirDataSource.class);
    }

    @Override
    public boolean open() {
        // nothing here, open directory is in getDataTable function.
        return true;
    }

    @Override
    public void close() {
        // nothing here, close directory is in getDataTable function.
    }

    @Override
    public DataTable getDataTable(String tableName, String idColumnName, String query) {

        DataTable dataTable = new DataTable(application, tableName, idColumnName);
        dataTable.setQuery(query);

        String dir = query.replaceAll("[\\\\]", "/");
        String path;
        String filter;
        int index;
        File directory;
        File[] files;

        index = dir.indexOf("*");
        if (index >= 0) {
            index = dir.lastIndexOf("/");
            if (index >= 0) {
                path = dir.substring(0, index);
                filter = dir.substring(index + 1);
            } else {
                path = System.getProperty("user.dir");
                filter = dir;
            }
            directory = new File(path);
            files = directory.listFiles(new WildCardFilenameFilter(application, name, filter));
        } else {
            directory = new File(dir);
            files = directory.listFiles();
        }

        if (files == null) {
            return dataTable;
        }

        List<File> fileList = Arrays.asList(files);
        fileList.sort((o1, o2) -> {
            int dir1 = o1.isDirectory() ? 0 : 1;
            int dir2 = o2.isDirectory() ? 0 : 1;
            if (dir1 == dir2) {
                return o1.getName().compareTo(o2.getName());
            }
            return (dir1 > dir2) ? 1 : -1;
        });

        String columnName;
        DataRow dataRow;
        for (File file : fileList) {
            dataRow = new DataRow(application, dataTable);

            columnName = "Name";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, file.getName()));

            columnName = "Directory";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, booleanValue(file.isDirectory())));

            columnName = "Hidden";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, booleanValue(file.isHidden())));

            columnName = "Read";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, booleanValue(file.canRead())));

            columnName = "Write";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, booleanValue(file.canWrite())));

            columnName = "Execute";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, booleanValue(file.canExecute())));

            columnName = "LastModified";
            dataRow.putColumn(columnName, new DataDate(application, 0, Types.DATE, columnName, new Date(file.lastModified())));

            columnName = "Length";
            dataRow.putColumn(columnName, new DataDate(application, 0, Types.INTEGER, columnName, String.valueOf(file.length())));

            columnName = "Path";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, file.getParent()));

            columnName = "Absolute";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, booleanValue(file.isAbsolute())));

            columnName = "AbsolutePath";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, file.getAbsolutePath()));

            columnName = "CanonicalPath";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.VARCHAR, getCanonicalPath(file)));

            columnName = "FreeSpace";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.INTEGER, String.valueOf(file.getFreeSpace())));

            columnName = "UsableSpace";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.INTEGER, String.valueOf(file.getUsableSpace())));

            columnName = "TotalSpace";
            dataRow.putColumn(columnName, application.createDataColumn(columnName, Types.INTEGER, String.valueOf(file.getTotalSpace())));

            dataTable.addRow(dataRow);
        }

        return dataTable;
    }

    private String booleanValue(boolean isTrue) {
        if (isTrue) {
            return "True";
        } else {
            return "False";
        }
    }

    private String getCanonicalPath(File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = "";
        }
        return canonicalPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("name", name)
                .toString();
    }
}
