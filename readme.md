
# DConvers<br/><sup><sup><sup>(Data Conversion)</sup></sup></sup>

DConvers is a small program with the basic concept to convert data from source table to target table. 
But, now has many features that help to transform data in a target table before send to output formatter.


### Supported Inputs and Outputs

| Data Provider                      | Input / Data Source Name | Output / Property Name       |
| ---------------------------------- | ------------------------ | ---------------------------- |
| Database                           | User-Defined             | DBInsert, DBUpdate,DBExecute |
| ResultSetMetaData                  | ResultSetMetaData        | -                            |
| SQL(Create) File                   | -                        | SQL                          |
| SQL(Insert) File                   | SQL                      | SQL                          |
| SQL(Update) File                   | -                        | SQL                          |
| Markdown Table                     | MARKDOWN                 | MARKDOWN                     |
| Email                              | EMAIL                    | -                            |
| PDF                                | -                        | PDF                          |
| Fixed Length                       | -                        | TXT                          |
| CSV                                | CSV(future)              | CSV                          |
| Lines                              | Lines                    | Lines(future)                |
| Configuration Generator for source | -                        | SRC                          |
| Configuration Generator for target | -                        | TAR                          |
| Directory List                     | DIR                      |                              |


### Prerequisites

DConvers is a command line utility built on JDK 1.8 and you need a command line terminal like Windows CMD. 
To run this program as well, you always need to write some configuration (datasources,sftps,converters,sources,targets).
Define list of datasource and list of converter file in a [conversion file](#ConversionFile).
Define list of source and list of target in one or more [converter file](#ConverterFile).  

> Library of database driver are required for user's defined datasource.   

In a terminal, type this command to run sample-conversion.conf 

```batch
"%JAVA_HOME%\bin\java.exe" -Xms64m -Xmx256m -Dfile.encoding=UTF-8 -classpath "bin\dconvers.jar" com.clevel.dconvers.Main --source=sample-conversion.conf
```

And type this command to show help.

```batch
"%JAVA_HOME%\bin\java.exe" -Xms64m -Xmx256m -Dfile.encoding=UTF-8 -classpath "bin\dconvers.jar" com.clevel.dconvers.Main --source=sample-conversion.conf --help
```


### Configuring

Explain how to write the configuration files before run the DConvers application.  
All configuration files in DConvers project are in standard of properties file format.  
The possible values for any property is depends on the DataType of the property as described below

| Data Type | Possible Values     | Remark                                                                                                          |
|-----------|---------------------|-----------------------------------------------------------------------------------------------------------------|
| bool      | true, false         | boolean                                                                                                         |
| int       | 0,1,2,3,...         | long number                                                                                                     |
| dec       | 0.00,...            | big decimal number                                                                                              |
| string    | string              | character array as string                                                                                       |
| date      | yyyy/MM/dd HH:mm:ss | date time string in the default pattern or custom pattern in a FORMAT calculator and some output configuration. |

----

## Conversion File

> All directory path must use '/' instead of '\\' on all operating systems.

Conversion file is a properties file which contains 5 groups of property as follow
1. Conversion Properties
2. List of DataSource Properties 
3. List of SFTP Properties 
4. List of Converter Files
5. List of Variables

#### 1. Conversion Properties

| Property          | Data Type | Default Value | Description                        |
|-------------------|-----------|---------------|------------------------------------|
| exit.on.error     | boolean   | true          | true or false                      |
| exit.code.success | int       | 0             | customizable exit code for success |
| exit.code.error   | int       | 1             | customizable exit code for error   |
| exit.code.warning | int       | 2             | customizable exit code for warning |


#### 2. DataSource Properties (Database Connection)

| Property                | Data Type                 | Default Value | Description                                             |
|-------------------------|---------------------------|---------------|---------------------------------------------------------|
| datasource.url          | string                    | null          | jdbc connection string                                  |
| datasource.driver       | string                    | null          | driver class name with full package location            |
| datasource.user         | string                    | null          | user name to connect to DBMS                            |
| datasource.user.encrypted | boolean                    | false          | the user name is encrypted                            |
| datasource.password     | string                    | null          | password to connect to DBMS                             |
| datasource.password.encrypted | boolean                    | false          | the password is encrypted                         |
| datasource.retry        | integer                   | 1             | number of retry when connection is failed               |
| datasource.quotes.name  | string                    | empty         | one character for quotes of string-value and date-value |
| datasource.quotes.value | string                    | "             | one character for quotes of string-value and date-value |
| datasource.prop.*       | pair<property_name,value> | empty         | list of property sent to DBMS when make a connection.   |


#### 3. SFTP Properties (SFTP Connection) 

| Property      | Data Type | Default Value | Description                                  |
|---------------|-----------|---------------|----------------------------------------------|
| sftp.host     | string    | null          | ip address or domain name of the SFTP server |
| sftp.port     | string    | 22            | port use to connect to SFTP server           |
| sftp.user     | string    | null          | user name to connect to SFTP server          |
| sftp.password | string    | null          | password to connect to SFTP server           |
| sftp.retry    | integer   | 1             | number of retry when connection lost         |


#### 4. Converter File Property 

| Property  | Data Type | Default Value | Description                 |
|-----------|-----------|---------------|-----------------------------|
| converter | string    | null          | list of the converter files |
| converter.source.output | string | "" | root path for output of source table |
| converter.target.output | string | "" | root path for output of target table |
| converter.mapping.output | string | "" | root path for output of mapping table |
| converter.source.output.filenumber | int | 1 | root path for output of source table |
| converter.target.output.filenumber | int | 1 | root path for output of target table |
| converter.mapping.output.filenumber | int | 1 | root path for output of mapping table |


#### 5. Variable Property

```properties
variable.first=<value>
variable.second=<value>
```

Define user variables here and then use them in DynamicValue expression like this 'VAR:MY_VARIABLE_NAME'.  

> Please take care, don't use system variable name as user variable name because of the value will be replaced by the system.

----

## Converter File

Converter file is a properties file which contains 3 groups of property as follow
1 Converter Properties
2 List of Source Properties
3 List of Target Properties

#### Converter Properties

You can see full example in 'sample-converter.conf' file.

| Property        | Description                                                                                   |
|-----------------|-----------------------------------------------------------------------------------------------|
| converter.index | Some project has many converters, this property make all converters are sorted by this index. |

#### Source Properties

Before define the source properties, you need to enable the source property group first.

```properties
source=source_name
source.source_name.property=value
```

You can see full example in 'sample-converter.conf' file. However, the possible properties of the source are completely described in this table. 

| Property                            | Data Type | Default Value | Description                                                  |
| ----------------------------------- | --------- | ------------- | ------------------------------------------------------------ |
| source.index                        | int       | 0             | Some converters contain a lot of sources, this property make all sources are sorted by this index. |
| source.datasource                   | string    | empty string  | Name of data provider for this source, for possible values please see Datasource and Query. (Dynamic Value Enabled) |
| source.query                        | string    | empty string  | A query string is used to retrieve data from a datasource. (Dynamic Value Enabled) |
| source.id                           | string    | id            | Name of column that contains a key value for a data table of this source. |
| source.output                       | List      |               | see [Output Properties](#Output_Properties)                  |
| source.target                       | bool      | true          | when false this source will be destroyed immediately after all outputs are printed (free up memory) |
| (deprecated) source.requires.target | string    | empty string  | Empty mean no targets are required before build data-table of this source, otherwise this is name of a target that is required to build data-table of this source. *(**Deprecated**: see "source.requires.target.md/Alternative Way" for detailed)* |


#### Datasource and query

| Data Provider        | datasource        | query (Dynamic Value Enabled)                        | query parameters (Dynamic Value Enabled) | Data Type | Default Value | Description                                             |
| -------------------- | ----------------- | ---------------------------------------------------- | ---------------------------------------- | --------- | ------------- | ------------------------------------------------------- |
| Database             | User Defined Name | SQL String                                           | split                                    | integer   | 0             | 0 mean not split query, otherwise is split query every number of records         |
| ResultSet MetaData   | ResultSetMetaData | table name like SRC:name                             |                                          |           |               |                                                         |
| SQL(Insert) File     | SQL               | file-name                                            | quotes.name                              | string    | empty         | one character for quotes of string-value and date-value |
|                      |                   |                                                      | quotes.value                             | string    | "             | one character for quotes of string-value and date-value |
| Markdown(Table) File | MARKDOWN          | file-name                                            |                                          |           |               |                                                         |
| Email                | EMAIL             | Search String                                        |                                          |           |               |                                                         |
| Fixed Length File    | TXT               | file-name                                            |                                          |           |               |                                                         |
| CSV File             | CSV               | file-name                                            |                                          |           |               |                                                         |
| Line Based File      | Lines             | comma separated file-name, the file-name can be the system wildcard pattern | eol                                      | string    | \n            | line terminator symbols                                 |
| DConvers             | SYSTEM            | see 'System Query' for detailed                      |                                          |           |               |                                                         |

**Example: Query Parameter**

```properties
source.name.datasource=datasource_name
source.name.query=$[TXT:query.sql]
source.name.query.split=100
```

#### System Query

| query          | description                                                     |
|----------------|-----------------------------------------------------------------|
| ARG            | arguments from app-switch --arg                                 |
| VARIABLE       | system variable table contains all variable for value-type(VAR) |
| ENVIRONMENT    | application environment properties sorted by PROPERTY           |
| MEMORY         | show current memory information                                 |
| TABLE_SUMMARY  | the summary table contains all tables from all converters, this table name is SRC:table_summary and can access at any converter.   |
| OUTPUT_SUMMARY | the summary table contains all outputs from all converters, this table name is SRC:output_summary and can access at any converter. |


#### Target Properties

Before define the target properties, you need to enable the target property group first.

```properties
target=target_name
```

You can see full example in 'sample-converter.conf' file. However, the possible properties of the target are described in this table only. 

rownumber=CAL:ROWCOUNT(cash_deposit_withdraw)+1

| Property              | Data Type | Default Value | Description                                                                                                              |
|-----------------------|-----------|---------------|--------------------------------------------------------------------------------------------------------------------------|
| target.index          | int       | 0             | Some converters contain a lot of sources, this property make all sources are sorted by this index.                       |
| target.source         | string    | empty string  | Name of source or comma separated names, you can put prefix tag 'TAR:' and 'MAP:' to use target and mapping as a source. |
| target.mapping        | string    | target name   | Name of mapping table, mapping table store ID of source and target                                                       |
| target.mapping.output | List      |               | see [Output Properties](#Output_Properties)                                                                              |
| target.id             | string    | id            | name of primary key column, this is used by mapping table and used as default value of Output.DBUpdate.ID.               |
| target.rownumber      | int       | 1             | start number of VAR:ROW_NUMBER for this target (Dynamic Value Enabled)                                                   |
| target.output         | List      |               | see [Output Properties](#Output_Properties)                                                                              |
| target.transform      | List      |               | see [Transform Properties](#Transform_Properties)                                                                        |


#### Output Types

The DConvers program has 7 optional output types with different set of property, they are listed below

- SQL File Output (create,insert,update)
- Markdown File Output (markdown table)
- PDF File Output (using jasper report library)
- TXT File Output (fixed length format)
- CSV File Output (comma separated values)
- DBInsert Output (generate and execute sql insert)
- DBUpdate Output (generate and execute sql update)
- DBExecute Output (execute sql statement)
- Configuration File Output (create converter configuration that can use to backup data from database)


##### SQL Output Properties

| Property         | Data Type      | Default Value  | Description                                                                                                |
|------------------|----------------|----------------|------------------------------------------------------------------------------------------------------------|
| sql              | bool           | false          | create sql file or not                                                                                     |
| sql.sftp         | string         | null           | name of sftp.                                                                                              |
| sql.sftp.output  | string         | null           | custom output file name to put on the sftp server. (Dynamic Value Enabled)                                 |
| sql.output       | string         | table-name.sql | custom file name. (Dynamic Value Enabled)                                                                  |
| sql.create.dir   | bool           | true           | auto create directory for non-existing path.                                                               |
| sql.append       | bool           | false          | append or always replace                                                                                   |
| sql.charset      | string         | UTF-8          | name of character set                                                                                      |
| sql.eol          | string         | \n             | characters put at the end of line. (Dynamic Value Enabled)                                                 |
| sql.eof          | string         | \n             | characters put at the end of file, this characters will appear after the last eol. (Dynamic Value Enabled) |
| sql.quotes.name  | string         | empty          | one character for quotes of table-name and column-name                                                     |
| sql.quotes.value | string         | "              | one character for quotes of string-value and date-value                                                    |
| sql.dbms         | string         | dbms name      | name of DBMS to generate sql for                                                                           |
| sql.table        | string         | target name    | name of table to generate sql for                                                                          |
| sql.column       | list of string | empty          | apply for custom order of column name or use as column filter, empty then use original column list         |
| sql.create       | bool           | false          | generate sql create statement or not                                                                       |
| sql.insert       | bool           | false          | generate sql insert statement or not                                                                       |
| sql.update       | bool           | false          | generate sql update statement or not                                                                       |
| sql.pre          | string         | null           | your sql statements to put at the beginning of file                                                        |
| sql.post         | string         | null           | your sql statements to put at the end of file                                                              |

> Remark: SQL Output for MySQL may be need property sql.pre=SET FOREIGN_KEY_CHECKS = 0;


##### Markdown Output Properties

| Property                    | Data Type | Default Value | Description                                                  |
| --------------------------- | --------- | ------------- | ------------------------------------------------------------ |
| markdown                    | bool      | false         | create markdown file or not                                  |
| markdown.sftp               | string    | null          | name of sftp.                                                |
| markdown.sftp.output        | string    | null          | custom output file name to put on the sftp server. (Dynamic Value Enabled) |
| markdown.output             | string    | table-name.md | custom file name. (Dynamic Value Enabled)                    |
| markdown.create.dir         | bool      | true          | auto create directory for non-existing path.                 |
| markdown.append             | bool      | false         | append or always replace                                     |
| markdown.charset            | string    | UTF-8         | name of character set                                        |
| markdown.eol                | string    | \n            | characters put at the end of line. (Dynamic Value Enabled)   |
| markdown.eof                | string    | \n            | characters put at the end of file, this characters will appear after the last eol. (Dynamic Value Enabled) |
| markdown.comment            | bool      | true          | print comment as first block of content                      |
| markdown.comment.datasource | bool      | true          | print datasource information in a comment block              |
| markdown.comment.query      | bool      | true          | print query string in a comment block                        |
| markdown.mermaid            | bool      | true          | true = generate mermaid graph TD and put at the end of markdown file, false = no mermaid in the markdown file. |
| markdown.mermaid.full       | bool      | false         | true = generate mermaid graph TD with full stack option, false = normal. |


##### PDF Output Properties

| Property        | Data Type | Default Value | Description                                                                |
|-----------------|-----------|---------------|----------------------------------------------------------------------------|
| pdf             | bool      | false         | create markdown file or not                                                |
| pdf.sftp        | string    | null          | name of sftp.                                                              |
| pdf.sftp.output | string    | null          | custom output file name to put on the sftp server. (Dynamic Value Enabled) |
| pdf.output      | string    | table-name.md | custom file name. (Dynamic Value Enabled)                                  |
| pdf.create.dir  | bool      | true          | auto create directory for non-existing path.                               |
| pdf.jrxml       | string    | empty         | custom jrxml file for the layout of PDF.                                   |


##### TXT Output (Fixed Length) Properties

| Property            | Data Type | Default Value  | Description                                                                                             |
|---------------------|-----------|----------------|---------------------------------------------------------------------------------------------------------|
| txt                 | bool      | false          | create text file or not                                                                                 |
| txt.sftp            | string    | null           | name of sftp.                                                                                           |
| txt.sftp.output     | string    | null           | custom output file name to put on the sftp server. (Dynamic Value Enabled)                              |
| txt.output          | string    | table-name.txt | custom file name. (Dynamic Value Enabled)                                                               |
| txt.create.dir      | bool      | true           | auto create directory for non-existing path.                                                            |
| txt.append          | bool      | false          | append or always replace                                                                                |
| txt.charset         | string    | UTF-8          | name of character set                                                                                   |
| txt.eol             | string    | \n             | characters put at the end of line. (Dynamic Value Enabled)                                              |
| txt.eof             | string    | \n             | characters put at the end of file, this is replacement of the eol of last line. (Dynamic Value Enabled) |
| txt.separator       | string    | empty          | separator character or words use to separate values                                                     |
| txt.length.mode     | string    | CHAR           | BYTE or CHAR                                                                                            |
| txt.format          | string    | STR:1024       | see Fixed Length Format                                                                                 |
| txt.format.date     | string    | yyyyMMdd       | date format (pattern)                                                                                   |
| txt.format.datetime | string    | yyyyMMddHHmmss | datetime format (pattern)                                                                               |
| txt.fill.string     | char(1)   | blank:right    | the character to fill the string column                                                                 |
| txt.fill.number     | char(1)   | 0:left         | the character to fill the number column                                                                 |
| txt.fill.date       | char(1)   | blank:right    | the character to fill the date column                                                                   |


##### CSV Output Properties

| Property            | Data Type | Default Value  | Description                                                                                             |
|---------------------|-----------|----------------|---------------------------------------------------------------------------------------------------------|
| csv                 | bool      | false          | create csv file or not                                                                                  |
| csv.sftp            | string    | null           | name of sftp.                                                                                           |
| csv.sftp.output     | string    | null           | custom output file name to put on the sftp server. (Dynamic Value Enabled)                              |
| csv.output          | string    | table-name.csv | custom file name. (Dynamic Value Enabled)                                                               |
| csv.create.dir      | bool      | true           | auto create directory for non-existing path.                                                            |
| csv.append          | bool      | false          | append or always replace                                                                                |
| csv.charset         | string    | UTF-8          | name of character set                                                                                   |
| csv.bof             | string    | (none)         | characters put at the beginning of file. (Dynamic Value Enabled) |
| csv.eol             | string    | \n             | characters put at the end of line. (Dynamic Value Enabled)                                              |
| csv.eof             | string    | \n             | characters put at the end of file, this is replacement of the eol of last line. (Dynamic Value Enabled) |
| csv.header          | bool      | true           | first line is header                                                                                    |
| csv.separator       | string    | ,              | separator character or words use to separate values                                                     |
| csv.format          | string    | (none)         | see Fixed Length Format                                                                                 |
| csv.format.string   | string    | (none)         | string format still in the future plan                                                                  |
| csv.format.integer  | string    | ###0           | integer number format (pattern)                                                                         |
| csv.format.decimal  | string    | ###0.##        | decimal format (pattern)                                                                                |
| csv.format.date     | string    | dd/MM/yyyy     | date format (pattern)                                                                                   |
| csv.format.datetime | string    | dd/MM/yyyy HH:mm:ss | datetime format (pattern)                                                                               |


##### DBInsert Output Properties

| Property              | Data Type      | Default Value | Description                                                                                        |
|-----------------------|----------------|---------------|----------------------------------------------------------------------------------------------------|
| dbinsert              | bool           | false         | execute sql insert or not                                                                          |
| dbinsert.datasource   | string         | datasource    | datasource name.                                                                                   |
| dbinsert.column       | CSV string     | empty         | comma separated values, apply for custom order of column name or use as column filter, empty then use original column list(auto) |
| dbinsert.table        | string         | table         | name of table to insert.                                                                           |
| dbinsert.quotes.name  | string         | empty         | one character for quotes of table-name and column-name                                             |
| dbinsert.quotes.value | string         | "             | one character for quotes of string-value and date-value                                            |
| dbinsert.pre          | string         | null          | your sql statements to put at the beginning of generated-sql. (Dynamic Value Enabled)              |
| dbinsert.post         | string         | null          | your sql statements to put at the end of generated-sql. (Dynamic Value Enabled)                    |


##### DBUpdate Output Properties

| Property              | Data Type      | Default Value | Description                                                                                        |
|-----------------------|----------------|---------------|----------------------------------------------------------------------------------------------------|
| dbupdate              | bool           | false         | execute sql insert or not.                                                                         |
| dbupdate.datasource   | string         | datasource    | datasource name.                                                                                   |
| dbupdate.column       | CSV string     | empty         | comma separated values, apply for custom order of column name or use as column filter, empty then use original column list |
| dbupdate.table        | string         | table         | name of table to update.                                                                           |
| dbupdate.id           | string         | [target.id]   | name of primary key column of the table                                                            |
| dbupdate.quotes.name  | string         | empty         | one character for quotes of table-name and column-name                                             |
| dbupdate.quotes.value | string         | "             | one character for quotes of string-value and date-value                                            |
| dbupdate.pre          | string         | null          | your sql statements to put at the beginning of generated-sql. (Dynamic Value Enabled)              |
| dbupdate.post         | string         | null          | your sql statements to put at the end of generated-sql. (Dynamic Value Enabled)                    |

##### DBExecute Output Properties

| Property             | Data Type | Default Value | Description                                                  |
| -------------------- | --------- | ------------- | ------------------------------------------------------------ |
| dbexecute            | bool      | false         | execute sql insert or not.                                   |
| dbexecute.datasource | string    | datasource    | datasource name.                                             |
| dbexecute.column     | string    | sql           | name of a column that contains sql-statement to execute (sql statement must be end with ';', otherwise will be appended by sql from next row before) |
| dbexecute.pre        | string    | null          | your sql statements to put at the beginning of generated-sql. (Dynamic Value Enabled) |
| dbexecute.post       | string    | null          | your sql statements to put at the end of generated-sql. (Dynamic Value Enabled) |

##### SourceConfig Output Properties

Generate source configuration for each table-name in the DataTable.

| Property        | Data Type | Default Value   | Description                                                                                                |
|-----------------|-----------|-----------------|------------------------------------------------------------------------------------------------------------|
| src             | bool      | false           | create configuration file and generate source for all table name                                           |
| src.sftp        | string    | null            | name of sftp.                                                                                              |
| src.sftp.output | string    | null            | custom output file name to put on the sftp server. (Dynamic Value Enabled)                                 |
| src.output      | string    | table-name.csv  | custom file name. (Dynamic Value Enabled)                                                                  |
| src.create.dir  | bool      | true            | auto create directory for non-existing path.                                                               |
| src.append      | bool      | false           | append or always replace                                                                                   |
| src.charset     | string    | UTF-8           | name of character set                                                                                      |
| src.eol         | string    | \n              | characters put at the end of line. (Dynamic Value Enabled)                                                 |
| src.eof         | string    | \n              | characters put at the end of file, this characters will appear after the last eol. (Dynamic Value Enabled) |
| src.table       | string    | table_name      | column name in current table that store Table Name.                                                        |
| src.id          | string    | id_name         | column name in current table that store Column Name of Primary Key.                                        |
| src.owner       | string    | empty           | name of schema/owner                                                                                       |
| src.datasource  | string    | datasource-name | datasource name for all sources                                                                            |
| src.outputs     | string    | sql,md          | comma separated output-type-name                                                                           |


##### TargetConfig Output Properties

Generate target configuration for current table (can be source and target) each sources in the current converter file.(one target for one source)

> Recommended: include only for all sources that already have data.

| Property        | Data Type | Default Value  | Description                                                  |
| --------------- | --------- | -------------- | ------------------------------------------------------------ |
| tar             | bool      | false          | create configuration file and generate source for all table name |
| tar.for.source  | bool      | false          | true = create target for each sources in the current converter, false = create target for current table |
| tar.for.name    | bool      | false          | true = use column name  for column value, false = use default value depending on data type for column value |
| tar.sftp        | string    | null           | name of sftp.                                                |
| tar.sftp.output | string    | null           | custom output file name to put on the sftp server. (Dynamic Value Enabled) |
| tar.output      | string    | table-name.csv | custom file name. (Dynamic Value Enabled)                    |
| tar.create.dir  | bool      | true           | auto create directory for non-existing path.                 |
| tar.append      | bool      | false          | append or always replace                                     |
| tar.charset     | string    | UTF-8          | name of character set                                        |
| tar.eol         | string    | \n             | characters put at the end of line. (Dynamic Value Enabled)   |
| tar.eof         | string    | \n             | characters put at the end of file, this characters will appear after the last eol. (Dynamic Value Enabled) |
| tar.outputs     | string    | sql,md         | comma separated output-type-name                             |


#### Transform Properties

```properties
Syntax> TRANSFORMATION(argument1,argument2,..),TRANSFORMATION(argument1,argument2,..),..
```

The transform function for a target is a calculator which insert/replace result as a column into current datatable.
And now have many transform functions that are listed below


##### FixedLength Transformer Function Properties

This transformation use the same formatter of TXT Output but this transform take effect to column value only, possible properties are described below

```batch
Syntax> fixedlength([[insertAsNewColumn] or [replace:[ColumnIndex]]],[format])
```

###### Parameters

| Parameters                                                     | Data Type | Default Value | Description                                                                                                  |
|----------------------------------------------------------------|-----------|---------------|--------------------------------------------------------------------------------------------------------------|
| [insertAsNewColumn]                                            | string    | unnamed:1     | syntax is [[ColumnName]:[insertColumnIndex]                                                                  |
| [ColumnName]                                                   | string    | [required]    | the word "replace" to replace existing column by index or the name of new column to insert.                  |
| [ColumnIdentifier] and [anotherColumnIndex] and [insertColumnIndex] | int       | 1             | index of any column, start at 1                                                                              |
| [format]                                                       | string    | STR:1024      | Syntax> [ColumnType:ColumnLength](,[ColumnType:ColumnLength])..\nExample> INT:3,DEC:19.4,DTE:8,DTT:16,STR:80 |

###### Optional Properties

| Property                    | Data Type | Default Value  | Description                             |
|-----------------------------|-----------|----------------|-----------------------------------------|
| fixedlength.length.mode     | string    | CHAR           | BYTE or CHAR                            |
| fixedlength.format.date     | string    | yyyyMMdd       | date format (pattern)                   |
| fixedlength.format.datetime | string    | yyyyMMddHHmmss | datetime format (pattern)               |
| fixedlength.fill.string     | char(1)   | blank:right    | the character to fill the string column |
| fixedlength.fill.number     | char(1)   | 0:left         | the character to fill the number column |
| fixedlength.fill.date       | char(1)   | blank:right    | the character to fill the date column   |


##### Concat Transformer Function Properties

This transformation used for column concatenation. After transformed by CONCAT, the type of all columns will changed to String. possible properties are described below

```batch
Syntax> concat([insertAsNewColumn],[[columnRange] or [columnIndex]],..)
```

###### Parameters

| Parameters                            | Data Type | Default Value | Description                                                                                 |
|---------------------------------------|-----------|---------------|---------------------------------------------------------------------------------------------|
| [insertAsNewColumn]                   | string    | unnamed:1     | syntax is [ColumnName]:[insertColumnIndex]                                                  |
| [ColumnName]                          | string    | [required]    | the word "replace" to replace existing column by index or the name of new column to insert. |
| [ColumnIdentifier] and [insertColumnIndex] | int       | 1             | index of any column, start at 1                                                             |
| [columnRange]                         | string    | 1-2           | range of column, syntax is columnIndex-anotherColumnIndex                                   |


##### RowCount Transformer Function Properties

This transformation is simple get the size of specific table. possible properties are described as below

```batch
Syntax> rowcount([insertAsNewColumn],[current or [dataTableIdentifier]])
```

###### Parameters

| Property              | Data Type | Default Value | Description                                                                                             |
|-----------------------|-----------|---------------|---------------------------------------------------------------------------------------------------------|
| [insertAsNewColumn]   | string    | unnamed:1     | syntax is [ColumnName]:[insertColumnIndex]                                                              |
| [ColumnName]          | string    | [required]    | the word "replace" to replace existing column by index or the name of new column to insert.             |
| [insertColumnIndex]   | int       | 1             | index of column to insert, start at 1                                                                   |
| [dataTableIdentifier] | string    | empty         | syntax is [TableType]:[Name] such as SRC:secondsource, TAR:firsttarget, MAP:secondsource_to_firsttarget |
| [columnRange]         | string    | 1-2           | range of column, syntax is columnIndex-anotherColumnIndex                                               |


##### Sum Transformer Function Properties

This transformation is simple get the size of specific table. possible properties are described as below

```batch
Syntax> sum([insertAsNewColumn],[current or [[TableType]:[TableName]]],[current or [RowIndex]],[[ColumnRange] or [ColumnIndex]],..)
```

###### Parameters

| Property              | Data Type | Default Value | Description                                                                                             |
|-----------------------|-----------|---------------|---------------------------------------------------------------------------------------------------------|
| [insertAsNewColumn]   | string    | unnamed:1     | syntax is [ColumnName]:[insertColumnIndex]                                                              |
| [ColumnName]          | string    | [required]    | the word "replace" to replace existing column by index or the name of new column to insert.             |
| [insertColumnIndex]   | int       | 1             | index of column to insert, start at 1                                                                   |
| [dataTableIdentifier] | string    | empty         | syntax is [TableType]:[Name] such as SRC:secondsource, TAR:firsttarget, MAP:secondsource_to_firsttarget |
| [columnRange]         | string    | 1-2           | range of column, syntax is columnIndex-anotherColumnIndex                                               |


##### Remove Transformer Function Properties

This transformation used for column deletion. After transformed by REMOVE, recommended to remove from the last column to avoid index out of bound exception. possible properties are described below

```batch
Syntax> remove([columnRange] or [columnIndex,anotherColumnIndex,..],..)
```

###### Parameters

| Property           | Data Type | Default Value | Description                                               |
|--------------------|-----------|---------------|-----------------------------------------------------------|
| [ColumnIdentifier] | int       | 1             | index of any column, start at 1                           |
| [columnRange]      | string    | 1-2           | range of column, syntax is columnIndex-anotherColumnIndex |


##### Row Split Transformer Function Properties

This transformation used for a column contains CSV like value. After transform by this function, rows of datatable will be multiply by the split-value-list. 
Possible properties are described below

```batch
Syntax> rowsplit([ColumnIdentifier],[regex])
```

###### Parameters

| Property           | Data Type | Default Value | Description                                               |
|--------------------|-----------|---------------|-----------------------------------------------------------|
| [ColumnIdentifier] | int       | 1             | index number of the column to be split, index start at 1                           |
| [regex]            | string    | [,]           | regex will be pass to the String.split(regex) function    |
| [DataType]         | string    | STR           | after split will be convert all values to this type, STR for string, INT for long integer, DEC for decimal, DTE for date and time |


##### Row Filter Transformer Function Properties

This transformation used to remove some row before go into the Output phase. After transform by this function, rows of datatable will be reduced. possible properties are described below

```batch
Syntax> rowfilter([FilterType],[columnIdentifier]=value)
```

###### Parameters

| Property           | Data Type | Default Value | Description                                               |
|--------------------|-----------|---------------|-----------------------------------------------------------|
| [ColumnIdentifier] | int       | 1             | index of any column, start at 1                           |
| [FilterType]       | string    | Exclude       | Include or Exclude                                        |


##### Get Transformer Function Properties

Get a column-value from any where.

```batch
Syntax> get([replace or [ColumnName]]:[insertColumnIndex],[current or [[TableType]:[TableName]]],[current or [rowIndex]],[columnIndex])
```

##### Compile Transformer Function Properties

Like the Get Transformer Function above, get a column-value from any where and then compile it as dynamic value expression.

```batch
Syntax> compile([replace or [ColumnName]]:[insertColumnIndex],[current or [[TableType]:[TableName]]],[current or [rowIndex]],[columnIndex])
```

----

## Dynamic Value Expression

Dynamic Value is used in Converter File, the usage of Dynamic Value has some differences between general properties and target.column.

Dynamic value for general properties need to covered by $[ ] but for target.column doesn't need cover.

```
Properties-Syntax> property=$[[Type]:[Type-Parameters]]
TargetColumn-Syntax> column.colname=[Type]:[Type-Parameters]

example:
source.name.query=$[TXT:../sql/select.sql]
target.name.column.colname=VAR:APPLICATION_START
```

When the content string of TXT file are loaded, content string can contains the Dynamic Value like this.

```sql
select c,d,e from cde where c in ($[SRC:abc.c],$[SRC:bcd.c])
```

>   The system will detect and compile dynamic value until no remaining Dynamic Value Expression.

##### Dynamic Value Types

Type | Value Identifier | Example | Description
-----|------------------|---------|------------
TXT  | Full path name of a text file | $[TXT:C:\path\file.ext] | Insert content from a specified file.
SRC  | [SourceName].[SourceColumn] | $[SRC:MySourceTable.id{,default-value}] | Insert list of values from a source table in formatted of CSV (value1,value2,...). When got empty CSV then return default-value if exist.  
TAR  | [TargetName].[TargetColumn] | $[TAR:MyTargetTable.id{,default-value}] | Insert list of values from a target table in formatted of CSV (value1,value2,...). When got empty CSV then return default-value if exist.
MAP  | [MappingName].[MappingColumn] | $[MAP:MappingTable.source_id{,default-value}] | Insert list of values from a mapping table in formatted of CSV (value1,value2,...). When got empty CSV then return default-value if exist.
CAL  | [FunctionName] (ParameterList in CSV format) | $[CAL:GET(SRC:MySourceTable,1,2)] | Calculate a function to produce a value. see 'Calculators' for detailed. 
VAR  | [VariableName] | $[VAR:APPLICATION_START] | Value from a variable. see 'Variables' for detailed. 

----

## Calculators

Calculators for use in CAL type dynamic value expression.

>   Row Identifier can be row-index start at 1, CURRENT or DynamicValue such as VAR:ROW_NUMBER.
>   Column Identifier can be column-index start at 1, Column Name or DynamicValue such as VAR:USER_VAR_COL1_NAME.

| Calculator | Parameters                                                   | Description                                                  |
| ---------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| GET        | CAL:GET([Table Identifier],[Row Identifier],[Column Identifier]) | Get a value from specified column.                           |
| COMPILE    | CAL:COMPILE([Table Identifier],[Row Identifier],[Column Identifier]) | Get a value from specified column and then compile as dynamic value. |
| FORMAT     | CAL:FORMAT([Table Identifier],[Row Identifier],[Column Identifier],[Pattern]) | Get a value from specified column and then format using [Pattern]. |
| NAME       | CAL:NAME([Table Identifier])                                 | Name of a table.                                             |
| ROWCOUNT   | CAL:ROWCOUNT([Table Identifier])                             | Number of rows of a table.                                   |
| SUM        | CAL:SUM([Table Identifier],[Row Identifier],[[Column Identifier],..]) | Summarize all values in specified column list.               |
| ENCRYPT    | CAL:ENCRYPT([Table Identifier],[Row Identifier],[Column Identifier]) | Similar to GET but the returned value will be encrypted (using DConvers Default Algorithm). |
| DECRYPT    | CAL:DECRYPT([Table Identifier],[Row Identifier],[Column Identifier]) | Similar to GET but the returned value will be decrypted (using DConvers Default Algorithm) that mean the value before DECRYPT must be encrypted. |

----

## Variables

System Variables used in the Dynamic Value Statement to get value from constant value or system value.

```
syntax: VAR:NAME
```

System variable can contain 2 groups of value as following
1. System Variables - value in this group can be changed.
2. Constant Values - value in this group can not be changed.

##### System Variables

| NAME              | Type   | Description                                                                       |
|-------------------|--------|-----------------------------------------------------------------------------------|
| ROW_NUMBER        | int    | row number will be reset at the beginning of any datatable processes.             |
| APPLICATION_STATE | int    | current state of application, find exit.code in this document for possible values |
| SOURCE_FILE_NUMBER | int   | file number will be increase by 1 for every source, no reset action for this variable |
| TARGET_FILE_NUMBER | int   | file number will be increase by 1 for every target, no reset action for this variable |
| MAPPING_FILE_NUMBER | int   | file number will be increase by 1 for every mapping, no reset action for this variable |

##### Constant Values

| NAME              | Type     | Description                                                                |
|-------------------|----------|----------------------------------------------------------------------------|
| NOW               | string   | not for now, in fact this variable contains the time to start application. |
| EMPTY_STRING      | string   | "" for some configuration that has another default string.                 |
| APPLICATION_START | datetime | date and time at start of this application                                 |
| SOURCE_OUTPUT_PATH | string  | value from property 'converter.source.output' in conversion configuration file.                                 |
| TARGET_OUTPUT_PATH | string  | value from property 'converter.target.output' in conversion configuration file.                                 |
| MAPPING_OUTPUT_PATH | string  | value from property 'converter.mapping.output' in conversion configuration file.                                 |

> You can see full list in source code of SystemVariable.java

----

#### Fixed Length Format

Explain how to defined Fixed Length Format for the property "txt.format".

----

## APPENDIX



### Built With

* [IntelliJ IDEA](https://maven.apache.org/) - The popular java ide.

### Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

### Versioning

We use [GIT](http://git.org/) for versioning. For the versions available, see the [tags on this repository](https://gitlab.the-c-level.com/Shared/DConvers/tags). 

### Authors

* **Prazit Jitmanozot** - *Full Stack* - [Prazit](https://gitlab.the-c-level.com/Prazit)

See also the list of [contributors](https://gitlab.the-c-level.com/Shared/DConvers/contributors) who participated in this project.

### License

This project is a part of C-Level Company Limited in Thailand and protected by the Thailand Raw MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Inspiration: this application birth in the Data Migration Project for SCT-Gold Version 2.x.
* Inspiration: but many features are required by the LHBank ETL Project.
* etc

----

-- end of document --