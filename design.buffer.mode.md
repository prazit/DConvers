
# Design of Buffer Modes

```properties
[source/target].tablename.buffer=[buffer-mode]
```

| Mode   | Value for [buffer-mode] | Description                                                  |
| ------ | ----------------------- | ------------------------------------------------------------ |
| Memory | Memory                  | In normal (default), data-table load all records into Memory (large physical memory is required) |
| DBMS   | [Data Source Name]      | Alternative way for large number of records or low memory resource, data-table load only the ActiveRows into Memory and unload them when they're going to be InactiveRows. |



## DBMS Mode

**Some objects need to changes**

| Object                 | Changes                                                      |
| ---------------------- | ------------------------------------------------------------ |
| ActiveRows             | ActiveRows has 4 important properties.<br /><br />1.RowCount = select count(*) from (original-sql-select)<br />2.ActiveRowCount<br />3.first-active-row-number ( row-number start at 0 )<br />4.last-active-row-number = RowCount - 1<br /><br />ActiveRows in Memory mode contains first-active-row-number = 0 and last-active-row-number = RowCount - 1. |
| DataTable              | getRowList function must be obsoleted,  lets use getRow instead. |
| DataTable              | getRow function need to check row-number is in ActiveRows first, when not in ActiveRows then set it as new ActiveRows(ActiveRow manager maybe needed) and then load data into ActiveRows. |
| DataTable              | when build target table need to insert into DBMS directly and then use ActiveRows to manage data when want to access them.<br />use the same to build target table for build mapping table. |
| DataSource             | createDataTable function need to load Active-Rows only (the fetch statement maybe use/or limit). |
| ColValue<br />(lookup) | mechanic of lookup are different between Memory-Mode and DBMS-Mode, need new mechanic for DBMS-Mode using SQL-Select. |





----

-- end of document --