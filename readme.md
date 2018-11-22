
# DConvers<br/>

DConvers or Data Conversion is a small program with the basic concept to convert data from source table to target table. 

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

To run this program as well, you need JDK 1.8 and some predefined configuration (datasources,sources,targets). Data sources and converter files are defined within a [conversion file](#ConversionFile), sources and targets are in a [converter file](#ConverterFile).  
  
Database driver library is required for some datasource.   

```
your-conversion.conf     # store datasources and converters
converter-1.conf         # store sources and targets as set number 1
converter-2.conf         # store sources and targets as set number 2
```

### Installing

A step by step series of examples that tell you how to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

### Testing

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

### Running

Windows Scheduler Guide is required here.

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo


## Configuring

Explain how to write the configuration files before run the DConvers application.  
All configuration files in DConvers project are the standard properties file.
The possible values for any property is depends on the DataType of the property as described below

Data Type | Possible Values | Remark
----------|-----------------|-------
bool | true, false | boolean
int | 0,1,2,3,... | long number
dec | 0.00,... | decimal number
string | all string | character array as string


### Conversion File

Conversion file is a properties file which contains 4 groups of property as follow
1 Conversion Properties
2 DataSource Properties 
3 SFTP Properties 
4 Converter File Properties 

#### Conversion Properties
#### DataSource Properties 
#### SFTP Properties 
#### Converter File Properties 


### Converter File

Converter file is a properties file which contains 3 groups of property as follow
1 Converter Properties
2 Source Properties
3 Target Properties

#### Converter Properties

You can see full example in 'sample-converter.conf' file.

Property | Description
---------|------------
converter.index | Some project has many converters, this property make all converters are sorted by this index.

#### Source Properties

Before define the source properties, you need to enable the source property group first.

```properties
source=source_name
source.source_name.property=value
```

You can see full example in 'sample-converter.conf' file. However, the possible properties of the data source are described in this table only. 

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
source.index | int | 0 | Some converters contain a lot of sources, this property make all sources are sorted by this index.
source.datasource | string | empty string | Name of datasource to provide data for this source.
source.query | string | empty string | A query string is used to retrieve data from a datasource. (Dynamic Value Enabled)
source.id | string | id | Name of column that contains a key value for a data table of this source.
source.output | Property Set |  | see [Output Properties](#Output_Properties)
source.transform | Property Set |  | see [Transform Properties](#Transform_Properties)

#### Target Properties

Before define the target properties, you need to enable the target property group first.

```properties
target=target_name
target.target_name.property=value
```

You can see full example in 'sample-converter.conf' file. However, the possible properties of the target are described in this table only. 

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
target.index | int | 0 | Some converters contain a lot of sources, this property make all sources are sorted by this index.
target.source | string | empty string | A query string is used to retrieve data from a datasource. In a query string can contains runtime contents called "Dynamic Value" (see Dynamic Value Expression for detailed)
target.id | string | id | name of primary key column, this is used by mapping table and used as default value of Output.DBUpdate.ID.
target.[outputs] | Property Set |  | see [Output Properties](#Output_Properties)
target.[transforms] | Property Set |  | see [Transform Properties](#Transform_Properties)

#### Output Properties

The DConvers program has 7 optional output types with different set of property, they are listed below
- SQL File Output (create,insert,update)
- Markdown File Output (markdown table)
- PDF File Output (using jasper report library)
- TXT File Output (fixed length format)
- CSV File Output (comma separated values)
- DBInsert Output (execute sql insert)
- DBUpdate Output (execute sql update)

##### SQL Output Properties

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
sql | bool | false | create sql file or not 
sql.sftp | string | null | name of sftp.
sql.sftp.output | string | null | custom output file name to put on the sftp server. (Dynamic Value Enabled)
sql.output | string | table-name.sql | custom file name. (Dynamic Value Enabled)
sql.create.dir | bool | true | auto create directory for non-existing path. 
sql.append | bool | false | append or always replace
sql.charset | string | UTF-8 | name of character set
sql.eol | string | \n | characters put at the end of line
sql.quotes.name | string | empty | one character for quotes of table-name and column-name
sql.quotes.value | string | empty | one character for quotes of string-value and date-value
sql.table | string | target name | name of table to generate sql for
sql.create | bool | false | generate sql create statement or not
sql.insert | bool | false | generate sql insert statement or not
sql.update | bool | false | generate sql update statement or not
sql.pre | string | null | your sql statements to put at the beginning of file
sql.post | string | null | your sql statements to put at the end of file

> Remark: SQL Output for MySQL may be need property sql.post=SET FOREIGN_KEY_CHECKS = 0;

##### Markdown Output Properties

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
markdown | bool | false | create markdown file or not 
markdown.sftp | string | null | name of sftp.
markdown.sftp.output | string | null | custom output file name to put on the sftp server. (Dynamic Value Enabled)
markdown.output | string | table-name.md | custom file name. (Dynamic Value Enabled)
markdown.create.dir | bool | true | auto create directory for non-existing path. 
markdown.append | bool | false | append or always replace
markdown.charset | string | UTF-8 | name of character set
markdown.eol | string | \n | characters put at the end of line

##### PDF Output Properties

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
pdf | bool | false | create markdown file or not 
pdf.sftp | string | null | name of sftp.
pdf.sftp.output | string | null | custom output file name to put on the sftp server. (Dynamic Value Enabled)
pdf.output | string | table-name.md | custom file name. (Dynamic Value Enabled)
pdf.create.dir | bool | true | auto create directory for non-existing path. 
pdf.jrxml | string | empty | custom jrxml file for the layout of PDF.

##### TXT Output Properties

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
txt | bool | false | create text file or not 
txt.sftp | string | null | name of sftp.
txt.sftp.output | string | null | custom output file name to put on the sftp server. (Dynamic Value Enabled)
txt.output | string | table-name.txt | custom file name. (Dynamic Value Enabled)
txt.create.dir | bool | true | auto create directory for non-existing path. 
txt.append | bool | false | append or always replace
txt.charset | string | UTF-8 | name of character set
txt.eol | string | \n | characters put at the end of line
txt.separator | string | empty | separator character or words use to separate values 
txt.format | string | STR:1024 | see Fixed Length Format
txt.format.date | string | YYYYMMdd | date format (pattern)
txt.format.datetime | string | YYYYMMddHHmmss | datetime format (pattern)
txt.fill.string | char(1) | blank:right | the character to fill the string column 
txt.fill.number | char(1) | 0:left | the character to fill the number column 
txt.fill.date | char(1) | blank:right | the character to fill the date column 

##### CSV Output Properties

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
csv | bool | false | create csv file or not 
csv.sftp | string | null | name of sftp.
csv.sftp.output | string | null | custom output file name to put on the sftp server. (Dynamic Value Enabled)
csv.output | string | table-name.csv | custom file name. (Dynamic Value Enabled)
csv.create.dir | bool | true | auto create directory for non-existing path. 
csv.append | bool | false | append or always replace
csv.charset | string | UTF-8 | name of character set
csv.eol | string | \n | characters put at the end of line
csv.separator | string | , | separator character or words use to separate values 

##### DBInsert Output Properties

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
dbinsert | bool | false | execute sql insert or not 
dbinsert.datasource | string | datasource | datasource name. 
dbinsert.table | string | table | name of table to insert.
dbinsert.quotes.name | string | empty | one character for quotes of table-name and column-name
dbinsert.quotes.value | string | empty | one character for quotes of string-value and date-value
dbinsert.pre | string | null | your sql statements to put at the beginning of generated-sql. (Dynamic Value Enabled)
dbinsert.post | string | null | your sql statements to put at the end of generated-sql. (Dynamic Value Enabled)

##### DBUpdate Output Properties

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
dbupdate | bool | false | execute sql insert or not.
dbupdate.datasource | string | datasource | datasource name. 
dbupdate.table | string | table | name of table to update.
dbupdate.id | string | [target.id] | name of primary key column of the table
dbupdate.quotes.name | string | empty | one character for quotes of table-name and column-name
dbupdate.quotes.value | string | empty | one character for quotes of string-value and date-value
dbupdate.pre | string | null | your sql statements to put at the beginning of generated-sql. (Dynamic Value Enabled)
dbupdate.post | string | null | your sql statements to put at the end of generated-sql. (Dynamic Value Enabled)


#### Transform Properties

```properties
Systax> TRANSFORMATION(argument1,argument2,..),TRANSFORMATION(argument1,argument2,..),..
```

The DConvers program has only 3 types for the transformers, described as following
1. (CC) Calculation using Current Table and choose to insert as new column or replace existing column
2. (CA) Calculation using Another Table and choose to insert as new column or replace existing column
3. (RC) Remove Column by specified index/condition

And now, has only 3 transformer function with different set of property, they are listed below
- CC.fixedlength function - format value of specified columns and choose to insert as new column or replace existing column
- CC.concat function - concat value of specified columns and choose to insert as new column or replace existing column
- CC.calc function - calculate value by basic column expression and choose to insert as new column or replace existing column
    + column expression is basic math expression using only the index of column, see Column Expression for detailed
- CA.rowcount function - count rows of the specified table and choose to insert as new column or replace existing column
- RC.remove function - remove specified columns

##### FixedLength Transformer Function Properties

This transformation use the same formatter of TXT Output but this transform take effect to column value only, possible properties are described below

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
transform | <td colspan="2">fixedlength([[insertAsNewColumn] or [replace:[ColumnIndex]]],[format])</td> 
[insertAsNewColumn] | string | unnamed:1 | syntax is [[ColumnName]:[insertColumnIndex]
[ColumnName] | string | [required] | the word "replace" to replace existing column by index or the name of new column to insert.
[columnIndex] and [anotherColumnIndex] and [insertColumnIndex] | int | 1 | index of any column, start at 1
[format] | string | STR:1024 | see [Fixed Length Format](#Fixed Length Format)
fixedlength.format.date | string | YYYYMMdd | date format (pattern)
fixedlength.format.datetime | string | YYYYMMddHHmmss | datetime format (pattern)
fixedlength.fill.string | char(1) | blank:right | the character to fill the string column 
fixedlength.fill.number | char(1) | 0:left | the character to fill the number column 
fixedlength.fill.date | char(1) | blank:right | the character to fill the date column 

##### Fixed Length Format

```properties
Syntax> [ColumnType:ColumnLength](,[ColumnType:ColumnLength])..
Example> INT:3,DEC:19.4,DTE:8,DTT:16,STR:80
``` 

##### Concat Transformer Function Properties

This transformation used for column concatenation. After transformed by CONCAT, the type of all columns will changed to String. possible properties are described below

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
transform | <td colspan=3>CONCAT([insertAsNewColumn],[[columnRange] or [columnIndex]],..)</td>
[insertAsNewColumn] | string | unnamed:1 | syntax is [ColumnName]:[insertColumnIndex] 
[ColumnName] | string | [required] | the word "replace" to replace existing column by index or the name of new column to insert.
[columnIndex] and [insertColumnIndex] | int | 1 | index of any column, start at 1
[columnRange] | string | 1-2 | range of column, syntax is columnIndex-anotherColumnIndex

##### RowCount Transformer Function Properties

This transformation is simple get the size of specific table. possible properties are described as below

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
transform | <td colspan=3>ROWCOUNT([insertAsNewColumn],[current or [dataTableIdentifier]])</td>
[insertAsNewColumn] | string | unnamed:1 | syntax is [ColumnName]:[insertColumnIndex] 
[ColumnName] | string | [required] | the word "replace" to replace existing column by index or the name of new column to insert.
[insertColumnIndex] | int | 1 | index of column to insert, start at 1
[dataTableIdentifier] | string | empty | syntax is [TableType]:[Name] such as SRC:secondsource, TAR:firsttarget, MAP:secondsource_to_firsttarget
[columnRange] | string | 1-2 | range of column, syntax is columnIndex-anotherColumnIndex

##### Sum Transformer Function Properties

This transformation is simple get the size of specific table. possible properties are described as below

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
transform | <td colspan=3>SUM([insertAsNewColumn],[current or [[TableType]:[TableName]]],[current or [RowIndex]],[[ColumnRange] or [ColumnIndex]],..)</td>
[insertAsNewColumn] | string | unnamed:1 | syntax is [ColumnName]:[insertColumnIndex] 
[ColumnName] | string | [required] | the word "replace" to replace existing column by index or the name of new column to insert.
[insertColumnIndex] | int | 1 | index of column to insert, start at 1
[dataTableIdentifier] | string | empty | syntax is [TableType]:[Name] such as SRC:secondsource, TAR:firsttarget, MAP:secondsource_to_firsttarget
[columnRange] | string | 1-2 | range of column, syntax is columnIndex-anotherColumnIndex

##### Remove Transformer Function Properties

This transformation used for column deletion. After transformed by REMOVE, recommended to remove from the last column to avoid index out of bound exception. possible properties are described below

Property | Data Type | Default Value | Description
---------|-----------|---------------|------------
transform | <td colspan=3>REMOVE([columnRange] or [columnIndex,anotherColumnIndex,..],..)</td>
[columnIndex] | int | 1 | index of any column, start at 1
[columnRange] | string | 1-2 | range of column, syntax is columnIndex-anotherColumnIndex



#### Dynamic Value Expression for Source.Query

$([Type]:[Value-Identifier])

When the query string contains the Dynamic Value, it will look like this: ```select c,d,e from cde where c in ($(SRC:abc.c),$(SRC:bcd.c))```.

The possible types of Dynamic Value for Query are described as below.

Type | Value Identifier | Example | Description
-----|------------------|---------|------------
TXT | Full path name of a text file | $(TXT:C:\path\file.ext) | Insert content from a specified file.
SRC  | [SourceName].[SourceColumn] | $(SRC:MySourceTable.id) | Insert list of values from a source table in formatted of CSV (value1,value2,...).  
TAR  | [TargetName].[TargetColumn] | $(TAR:MyTargetTable.id) | Insert list of values from a target table in formatted of CSV (value1,value2,...).
MAP  | [MappingName].[MappingColumn] | $(MAP:MappingTable.source_id) | Insert list of values from a mapping table in formatted of CSV (value1,value2,...).


#### Dynamic Value Expression for Target.Column.Value

[Type]:[Value-Identifier]

When the query string contains the Dynamic Value, it will look like this: ```select c,d,e from cde where c in ($(SRC:abc.c),$(SRC:bcd.c))```.

The possible types of Dynamic Value for Query are described as below.

Type | Value Identifier | Example | Description
-----|------------------|---------|------------
SRC  | [SourceName].[SourceColumn] | $(SRC:MySourceTable.id) | Insert list of values from a source table in formatted of CSV (value1,value2,...).  
TAR  | [TargetName].[TargetColumn] | $(TAR:MyTargetTable.id) | Insert list of values from a target table in formatted of CSV (value1,value2,...).
MAP  | [MappingName].[MappingColumn] | $(MAP:MappingTable.source_id) | Insert list of values from a mapping table in formatted of CSV (value1,value2,...).
MORE | | | |

#### Fixed Length Format

Explain how to defined Fixed Length Format for the property "txt.format".


## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [GIT](http://git.org/) for versioning. For the versions available, see the [tags on this repository](https://gitlab.the-c-level.com/Shared/DConvers/tags). 

## Authors

* **Prazit Jitmanozot** - *Full Stack* - [Prazit](https://gitlab.the-c-level.com/Prazit)

See also the list of [contributors](https://gitlab.the-c-level.com/Shared/DConvers/contributors) who participated in this project.

## License

This project is a part of C-Level Company Limited in Thailand and protected by the Thailand Raw MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
