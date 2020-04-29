package com.clevel.dconvers.input;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.DataSourceConfig;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.Converter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Types;
import java.util.*;

public class LinesDataSource extends DataSource {

    public LinesDataSource(DConvers dconvers, String name, DataSourceConfig dataSourceConfig) {
        super(dconvers, name, dataSourceConfig);
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(LinesDataSource.class);
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
        DataTable dataTable = new DataTable(dconvers, tableName, idColumnName);
        dataTable.setDataSource(name);
        dataTable.setQuery(query);

        String eol = queryParamMap.get(Property.OUTPUT_EOL.key().toUpperCase());
        if (eol == null) {
            eol = "\n";
        }

        List<File> fileList = getFileList(query);
        List<DataRow> dataRowList = new ArrayList<>();

        if (eol.equals("\\n")) {
            for (File file : fileList) {
                dataRowList.addAll(getRowListByLine(file, dataTable));
            }
        } else {
            for (File file : fileList) {
                dataRowList.addAll(getRowListByEOL(file, dataTable, eol));
            }
        }

        dataTable.setRowList(dataRowList);
        return dataTable;
    }

    private List<DataRow> getRowListByLine(File file, DataTable dataTable) {
        Converter converter = dconvers.currentConverter;
        List<DataRow> dataRowList = new ArrayList<>();
        DataRow dataRow;
        BufferedReader br = null;
        int lineNumber = 0;

        try {
            br = new BufferedReader(new FileReader(file));
            for (String line; (line = br.readLine()) != null; lineNumber++) {
                line = converter.compileDynamicValues(line + "\n");
                dataRow = getDataRow(lineNumber, line, dataTable);
                if (dataRow == null) {
                    continue;
                }
                dataRowList.add(dataRow);
            }

        } catch (FileNotFoundException fx) {
            error("file not found: {}", fx.getMessage());
            dataRowList = Collections.emptyList();

        } catch (Exception ex) {
            error("unexpected exception: ", ex);
            dataRowList = Collections.emptyList();

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("close file {} is failed, {}", file.getName(), e);
                }
            }
        }

        return dataRowList;
    }

    private List<DataRow> getRowListByEOL(File file, DataTable dataTable, String eol) {
        Converter converter = dconvers.currentConverter;
        List<DataRow> dataRowList = new ArrayList<>();
        DataRow dataRow;
        BufferedReader br = null;
        int lineNumber = 0;

        try {
            Scanner scanner = new Scanner(file, "UTF-8").useDelimiter(eol);
            String line;
            while (scanner.hasNext()) {
                line = scanner.next();
                if (scanner.hasNext()) {
                    line += eol;
                }
                line = converter.compileDynamicValues(line);
                dataRow = getDataRow(lineNumber, line, dataTable);
                if (dataRow == null) {
                    continue;
                }
                dataRowList.add(dataRow);
            }

        } catch (FileNotFoundException fx) {
            error("file not found: {}", fx.getMessage());
            dataRowList = Collections.emptyList();

        } catch (Exception ex) {
            error("unexpected exception: ", ex);
            dataRowList = Collections.emptyList();

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("close file {} is failed, {}", file.getName(), e);
                }
            }
        }

        return dataRowList;
    }

    protected DataRow getDataRow(int lineNumber, String line, DataTable dataTable) {
        DataRow dataRow = new DataRow(dconvers, dataTable);
        String columnName;

        columnName = "Number";
        dataRow.putColumn(columnName, dconvers.createDataColumn(columnName, Types.INTEGER, String.valueOf(lineNumber)));

        columnName = "Line";
        dataRow.putColumn(columnName, dconvers.createDataColumn(columnName, Types.VARCHAR, line));

        columnName = "Length";
        dataRow.putColumn(columnName, dconvers.createDataColumn(columnName, Types.INTEGER, (line == null) ? "0" : String.valueOf(line.length())));

        return dataRow;
    }

    private List<File> getFileList(String query) {

        // comma separated filename/pattern
        String[] patterns = query.split("[,]");

        List<File> fileList = new ArrayList<>();
        for (String pattern : patterns) {
            fileList.addAll(getFileListFromSinglePath(pattern));
        }

        return fileList;

    }

    private List<File> getFileListFromSinglePath(String pattern) {
        pattern = pattern.replaceAll("[\\\\]", "/");

        String path;
        String filter;
        int index;
        File file;
        File[] files;

        index = pattern.indexOf("*");
        if (index >= 0) {
            index = pattern.lastIndexOf("/");
            if (index >= 0) {
                path = pattern.substring(0, index);
                filter = pattern.substring(index + 1);
            } else {
                path = System.getProperty("user.pattern");
                filter = pattern;
            }
            file = new File(path);
            files = file.listFiles(new WildCardFilenameFilter(dconvers, name, filter));
        } else {
            file = new File(pattern);
            files = new File[]{file};
        }

        if (files == null) {
            return Collections.emptyList();
        }

        List<File> fileList = Arrays.asList(files);
        /*
        fileList.sort((o1, o2) -> {
            int dir1 = o1.isDirectory() ? 0 : 1;
            int dir2 = o2.isDirectory() ? 0 : 1;
            if (dir1 == dir2) {
                return o1.getName().compareTo(o2.getName());
            }
            return (dir1 > dir2) ? 1 : -1;
        });*/

        return fileList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("name", name)
                .toString();
    }
}
