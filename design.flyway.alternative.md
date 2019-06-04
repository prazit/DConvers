# Flyway Alternative

(design of plugin for DConvers)

----



## Data Source

```properties
datasource=your_datasource_name
datasource.your_datasource_name.url=
```



## Source DataTable

| Datasource Name | Query           | Description                                                  |
| --------------- | --------------- | ------------------------------------------------------------ |
| Lines           | CSV of filename | Read all lines from a file or multiple files as rows of a source datatable. |

```properties
source=your_source_name
source.your_source_name.datasource=Lines
source.your_source_name.query=../sql/*.sql
```



# Output

| Output Name | Remark                                     |
| ----------- | ------------------------------------------ |
| DBExecute   | Execute sql statements using a datasource. |

```properties
source.your_source_name.dbexecute=true
source.your_source_name.dbexecute.datasource=your_datasource_name
source.your_source_name.dbexecute.column=Line
source.your_source_name.dbexecute.output=dbexecute_history_file.csv
```





----

-- end of document --