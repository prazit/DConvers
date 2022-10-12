package com.clevel.dconvers.input;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataDate;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.util.*;

public class DirDataSource extends DataSource {

    private Properties moreProperties;
    private boolean isIncludeSub;
    private boolean isIncludeDir;

    public DirDataSource(DConvers dconvers, String name, DataSourceConfig dataSourceConfig) {
        super(dconvers, name, dataSourceConfig);
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
    public DataTable getDataTable(String tableName, String idColumnName, String query, HashMap<String, String> queryParamMap) {

        isIncludeDir = Boolean.parseBoolean(queryParamMap.get(Property.DIR.key().toUpperCase()));
        isIncludeSub = Boolean.parseBoolean(queryParamMap.get(Property.SUB.key().toUpperCase()));
        log.debug("DirDataSource: isIncludeDir({}) isIncludeSub({})", isIncludeDir, isIncludeSub);

        DataTable dataTable = new DataTable(dconvers, tableName, idColumnName);
        dataTable.setDataSource(name);
        dataTable.setQuery(query);

        String[] dirs = query.split("[,]");

        List<File> files;
        List<File> fileList = new ArrayList<>();
        for (String dir : dirs) {
            log.debug("dir = {}", dir);
            files = getFiles(dir);
            fileList.addAll(files);
        }

        if (fileList.size() == 0) {
            return dataTable;
        }

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
            if (file.isDirectory() && !isIncludeDir) {
                continue;
            }

            dataRow = new DataRow(dconvers, dataTable);

            columnName = "Name";
            dataRow.putColumn(columnName, dconvers.createDataColumn(1, columnName, Types.VARCHAR, file.getName()));

            columnName = "Directory";
            dataRow.putColumn(columnName, dconvers.createDataColumn(2, columnName, Types.VARCHAR, booleanValue(file.isDirectory())));

            columnName = "Hidden";
            dataRow.putColumn(columnName, dconvers.createDataColumn(3, columnName, Types.VARCHAR, booleanValue(file.isHidden())));

            columnName = "Read";
            dataRow.putColumn(columnName, dconvers.createDataColumn(4, columnName, Types.VARCHAR, booleanValue(file.canRead())));

            columnName = "Write";
            dataRow.putColumn(columnName, dconvers.createDataColumn(5, columnName, Types.VARCHAR, booleanValue(file.canWrite())));

            columnName = "Execute";
            dataRow.putColumn(columnName, dconvers.createDataColumn(6, columnName, Types.VARCHAR, booleanValue(file.canExecute())));

            columnName = "Absolute";
            dataRow.putColumn(columnName, dconvers.createDataColumn(7, columnName, Types.VARCHAR, booleanValue(file.isAbsolute())));

            columnName = "LastModified";
            dataRow.putColumn(columnName, new DataDate(dconvers, 8, Types.DATE, columnName, new Date(file.lastModified())));

            columnName = "Length";
            dataRow.putColumn(columnName, dconvers.createDataColumn(9, columnName, Types.INTEGER, String.valueOf(file.length())));

            columnName = "Path";
            dataRow.putColumn(columnName, dconvers.createDataColumn(10, columnName, Types.VARCHAR, file.getParent()));

            columnName = "AbsolutePath";
            dataRow.putColumn(columnName, dconvers.createDataColumn(11, columnName, Types.VARCHAR, file.getAbsolutePath()));

            columnName = "CanonicalPath";
            dataRow.putColumn(columnName, dconvers.createDataColumn(12, columnName, Types.VARCHAR, getCanonicalPath(file)));

            dataTable.addRow(dataRow);
        }

        return dataTable;
    }

    private List<File> getFiles(String dir) {
        List<File> fileList = new ArrayList<>();
        File[] files;
        dir = dir.replaceAll("[\\\\]", "/");

        File directory;
        int index = dir.indexOf("*");
        if (index >= 0) {
            String path;
            String filter;
            index = dir.lastIndexOf("/");
            if (index >= 0) {
                path = dir.substring(0, index);
                filter = dir.substring(index + 1);
            } else {
                path = System.getProperty("user.dir");
                filter = dir;
            }
            directory = new File(path);
            files = directory.listFiles(new WildCardFilenameFilter(dconvers, name, filter));
            if (files == null) {
                log.debug("files(0) with filter({}", filter);
                return fileList;
            }
            log.debug("files({}) with filter({})", files.length, filter);
        } else {
            directory = new File(dir);
            files = directory.listFiles();
            if (files == null) {
                log.debug("files(0) without filter");
                return fileList;
            }
            log.debug("files({}) without filter", files.length);
        }
        fileList.addAll(Arrays.asList(files));

        if (isIncludeSub) {
            List<File> subFolders = new ArrayList<>();
            for (File file : fileList) {
                if (file.isDirectory()) {
                    subFolders.addAll(getFiles(file.getAbsolutePath()));
                }
            }
            fileList.addAll(subFolders);
        }

        return fileList;
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
