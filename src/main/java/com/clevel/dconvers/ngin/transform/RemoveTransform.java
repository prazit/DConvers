package com.clevel.dconvers.ngin.transform;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.ngin.data.DataColumn;
import com.clevel.dconvers.ngin.data.DataRow;
import com.clevel.dconvers.ngin.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoveTransform extends Transform {

    public RemoveTransform(Application application, String name) {
        super(application, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        String argument = getArgument(Property.ARGUMENTS.key());
        String[] arguments = argument.split("[,]");

        List<DataRow> newRowList = new ArrayList<>();
        List<DataRow> rowList = dataTable.getAllRow();
        List<DataColumn> newColumnList;
        DataRow newRow;

        List<Integer> indexList = createIndexList(arguments, 0, 0, rowList.get(0).getColumnList().size() - 1);
        Collections.sort(indexList, Collections.reverseOrder());
        log.debug("RemoveTransform.reverseOrderIndex => {}", indexList);

        for (DataRow row : rowList) {
            newRow = new DataRow(application, dataTable);
            newColumnList = newRow.getColumnList();

            newColumnList.addAll(row.getColumnList());
            for (Integer index : indexList) {
                if (newColumnList.remove(index.intValue()) == null) {
                    error("RemoveTransform: Can't remove column(columnIndex:{}), columnlist-size = {}", index, newColumnList.size());
                    return false;
                }
            }

            newRow.updateColumnMap();
            newRowList.add(newRow);
        }

        rowList.clear();
        rowList.addAll(newRowList);

        return true;

    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(RemoveTransform.class);
    }

}
