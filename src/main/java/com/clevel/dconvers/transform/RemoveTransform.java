package com.clevel.dconvers.transform;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoveTransform extends Transform {

    public RemoveTransform(DConvers dconvers, String name) {
        super(dconvers, name);
    }

    @Override
    public boolean transform(DataTable dataTable) {

        String argument = getArgument(Property.ARGUMENTS.key());
        String[] arguments = argument.split("[,]");

        List<DataRow> newRowList = new ArrayList<>();
        List<DataRow> rowList = dataTable.getRowList();
        List<DataColumn> newColumnList;
        DataRow newRow;

        List<Integer> indexList = createIndexList(rowList.get(0), arguments, 0);
        Collections.sort(indexList, Collections.reverseOrder());
        log.debug("RemoveTransform.reverseOrderIndex => {}", indexList);

        for (DataRow row : rowList) {
            newRow = new DataRow(dconvers, dataTable);
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
