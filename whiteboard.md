# Whiteboard



## DConvers Plugin<br/><sup><sub>to generate Data Dictionary (multiple output tables in markdown formatted)</sub></sup>

| Status           | Required                                | Class Name              | Object Type | Remark                                                       |
| ---------------- | --------------------------------------- | ----------------------- | ----------- | ------------------------------------------------------------ |
| [**INPROGRESS**] | Data Dictionary Output<br />(proved)    | DataDictionaryOutput    | Output      | requires Data Dictionary Formatter                           |
|                  | (proved)                                | ^                       | ^           | requires Source Table that contains some required data, see 'Source Table' below |
| [**INPROGRESS**] | Data Dictionary Formatter<br />(proved) | DataDictionaryFormatter | Formatter   | requires Markdown Formatter (already exists, extend the MarkdownFormatter is recommended) |
| [WAITING]        | Source Table<br />(proved)              | SRC Table               | DataTable   | use normal sql to select information from DBMS for the required columns to genreate data dictionary |



### POC

|        | Concept                                                      | Remark                                                       |
| ------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| [DONE] | need to Test Copy Markdown Table into the Words file to prove this concept can use in generate document file | After copy into words document always **need to change font and resize all table mannually**. |
| [DONE] | need to POC METADATA result of MariaDB in the GoldspotDataMigration project | if METADATA provide all required columns [Passed] then redesign of Source Table to reduce required columns [Passed - see Source Table below]. |
| [DONE] | need to create SQL to get required columns from MariaDB      | [Passed - see 'SQL Query to build Source Table' below]       |



### Source Table

Required by DataDictionaryOutput.

| Required Column   | Description                                                  |
| ----------------- | ------------------------------------------------------------ |
| General Name      | use to show as Table Header                                  |
| Table Name        | use to genreate SQL to get example data and use to show as Table Name: |
| Table Description | use to show as Table Description:                            |

>   Required columns above is in the Table List that can use SQL query to get them from the DBMS.

| Parameter                   | Description                                                  |
| --------------------------- | ------------------------------------------------------------ |
| Rows Limit for Example data | default limit is 3 rows, but user can customize the number between 1 to 10. |
| Table Name                  | column name                                                  |



### SQL Query to build Source Table

```sql
SELECT table_schema,
       table_name,
       table_comment,
       ROUND(((data_length + index_length) / 1024 / 1024), 2) as `Size (MB)`

FROM information_schema.TABLES

WHERE TABLE_SCHEMA = 'btecmds'

ORDER BY table_schema, table_name;
```



### SQL Query to build Data Dictionary

```sql
SELECT COLUMN_NAME,
       case COLUMN_KEY
           when 'PRI' then 'PK'
           when 'MUL' then 'FK'
           else ''
           end                              as PK_FK,
       IF(IS_NULLABLE = 'YES', 'No', 'YES') as NOT_NULL,
       case COLUMN_TYPE
           when 'bigint(20)' then 'bigint'
           when 'datetime(6)' then 'datetime'
           else COLUMN_TYPE
           end                              as DATA_TYPE,
       COLUMN_COMMENT                       as DESCRIPTION

FROM information_schema.COLUMNS

WHERE TABLE_SCHEMA = 'btecmds'
  and TABLE_NAME = <table_name_replaced>

ORDER BY ORDINAL_POSITION;
```



### SQL Qeury to build Example Data

```sql
SELECT COLUMN_LIST

FROM <table_name_replaced>

WHERE 

ORDER BY FIRST_COLUMN_AS_PK
```





### Data Dictionary Output Properties

**!IMPORTANT** Data Dictionary Output always need one row in all tables at lease, because of the generator will retrieve example data first and get column information from the METADATA of the ResultSet.

>   The properties are extended version of the 'Markdown Output Properties'.

| Property                    | Data Type | Default Value | Description                                                  |
| --------------------------- | --------- | ------------- | ------------------------------------------------------------ |
| datadict                    | bool      | false         | create datadict file or not                                  |
| datadict.sftp               | string    | null          | name of sftp.                                                |
| datadict.sftp.output        | string    | null          | custom output file name to put on the sftp server. (Dynamic Value Enabled) |
| datadict.output             | string    | table-name.md | custom file name. (Dynamic Value Enabled)                    |
| datadict.create.dir         | bool      | true          | auto create directory for non-existing path.                 |
| datadict.append             | bool      | false         | append or always replace                                     |
| datadict.charset            | string    | UTF-8         | name of character set                                        |
| datadict.eol                | string    | \n            | characters put at the end of line. (Dynamic Value Enabled)   |
| datadict.eof                | string    | \n            | characters put at the end of file, this characters will appear after the last eol. (Dynamic Value Enabled) |
| datadict.comment            | bool      | true          | print comment as first block of content                      |
| datadict.comment.datasource | bool      | true          | print datasource information in a comment block              |
| datadict.comment.query      | bool      | true          | print query string in a comment block                        |
| datadict.title              | string    | title         | name of the column which contains the Title Name used to show on the header of table |
| datadict.table              | string    | table         | name of the column which contains the Table Name used to generate SQL-Select to retrieve information of all columns in the table |
| datadict.description        | string    | description   | name of the column which contains the Table Description used to show on the header of table |
| datadict.query              | string    | null          | SQL Query to retrieve Data Dictionary, single keyword '<table_name>' is required to use this SQL for each table one by one (Dynamic Value Enabled) |
| datadict.column             | string    | column        | name of the column which contains the Column Name used to get column_name from the result that come from SQL-Query above |
| datadict.example.rows       | int       | 3             | Rows Limit for Example Data, possible numbers are 1 to 10    |
| datadict.example.columns    | int       | 5             | Column Limit, possible  numbers are 3 to 10                  |



### Generated Output

Generated by DataDictionaryOutput.



-- start of ouyput --

>   This data dictionary is generated by DConvers v.xxxxx
>
>   This dictionary contains 2 tables from source(yyyyyyyy)
>
>   Datasource : {}
>   Query : {}

```sql
select required_columns from DBMS
```



#### MY First Table

Table Name: BT_MST_MY_FIRST

Table Description: This description of 'BT MST MY FIRST' table.

| **Column Name** | **Not Null** | **Data Type** | **Description**                       |
| --------------- | ------------ | ------------- | ------------------------------------- |
| ID              | Yes          | BIGINT (20)   | Category ID                           |
| CODE            | Yes          | VARCHAR (10)  | Category Code                         |
| NAME            | Yes          | VARCHAR (200) | Category Name                         |
| DESCRIPTION     |              | VARCHAR (255) | Category Description                  |
| ACTIVE          |              | INT (1)       | Record status Active / In-Active      |
| CREATE_BY       |              | VARCHAR (255) | Username who creates this record      |
| CRATE_DATE      |              | DATETIME (6)  | Date and Time create this record      |
| MODIFY_BY       |              | VARCHAR (255) | Last Username who modify this record  |
| MODIFY_DATE     |              | DATETIME (6)  | Last Date and Time modify this record |

**Example data:**

| **ID** | CODE | NAME     |
| ------ | ---- | -------- |
| 1      | C01  | Network  |
| 2      | C02  | ERP      |
| 3      | C03  | Analytic |



#### BT MST MY SECOND Table

Table Name: BT_MST_MY_SECOND

Table Description: This description of 'BT MST MY SECOND' table.

| **Column Name**   | **PK/FK** | **Not Null** | **Data Type** | **Description**                       |
| ----------------- | --------- | ------------ | ------------- | ------------------------------------- |
| ID                | PK        | Yes          | BIGINT (20)   | Category ID                           |
| COUNTRY_ID        | FK        | Yes          | BIGINT (20)   | Refer to Country of origin table      |
| CATEGORY_ID       | FK        | Yes          | BIGINT (20)   | Refer to Category table               |
| IT_GROUP_ID       | FK        | Yes          | BIGINT (20)   | Refer to IT Responsible Group table   |
| SYSTEM_NAME       |           | Yes          | VARCHAR (200) | System name                           |
| REQUIRE_IT_REVIEW |           |              | INT (1)       | System require IT review              |
| REQUIRE_AD_ACC    |           |              | INT (1)       | System require AD account             |
| TICKET_SYSTEM     |           | Yes          | VARCHAR (50)  | Ticket system name                    |
| SYSTEM_EMAIL      |           |              | VARCHAR (200) | System email                          |
| SUPPORT_EMAIL     |           |              | VARCHAR (200) | Support email                         |
| ACTIVE            |           |              | INT (1)       | Record status Active / In-Active      |
| CREATE_BY         |           |              | VARCHAR (255) | Username who creates this record      |
| CRATE_DATE        |           |              | DATETIME (6)  | Date and Time create this record      |
| MODIFY_BY         |           |              | VARCHAR (255) | Last Username who modify this record  |
| MODIFY_DATE       |           |              | DATETIME (6)  | Last Date and Time modify this record |

Example data:

| ID   | **Category Name** | **System Name** | **Group Name** | **Country Name** | **Ticket System** |
| ---- | ----------------- | --------------- | -------------- | ---------------- | ----------------- |
| 1    | Network           | Windows Logon   | IT Security    | Thailand         | BITS              |
| 2    | Network           | Email           | IT Security    | Thailand         | BITS              |
| 3    | Network           | VPN             | IT Security    | Thailand         | BITS              |
| 4    | Network           | Share Drive     | IT Security    | Thailand         | BITS              |
| 5    | ERP               | SAP             | SAP Team       | Thailand         | BITS              |
| 6    | Analytic          | QlikSense       | QlikSense Team | Thailand         | BITS              |
| 7    | Analytic          | BI              | BI Team        | Thailand         | BITS              |
| 8    | ERP               | CRM             | CRM Team       | Thailand         | BITS              |

-- end of ouyput --



### Configuration

>   Example config for DataDictionaryOutput plugin

##### DataDictionaryGenerator.conf

```pro
plugins.output.datadict=com.clevel.dconvers.plugins.output.DataDictionaryOutput

#------- SHARED -------

include=Generator/config/shared/BTMMRF-Datasource.conf
include=Generator/config/shared//Output-Path.conf

#------- CONVERTER -------

converter=Generator/config/DataDictionaryGenerator.conf
converter.index=10

#------- SRC:ALITLNDP_TF -------
# TableList.sql contains same sql in the topic 'SQL Query to build Source Table'

source=ALITLNDP_TF
source.ALITLNDP_TF.index=10
source.ALITLNDP_TF.datasource=BTMMRF
source.ALITLNDP_TF.query=$[TXT:Generator/sql/TableList.sql]
source.ALITLNDP_TF.id=table_name

source.ALITLNDP_TF.markdown=true
source.ALITLNDP_TF.markdown.output=Generator/output/ALITLNDP_TF.md

source.ALITLNDP_TF.datadict=true
source.ALITLNDP_TF.datadict.output=Generator/output/DataDictionary.md
source.ALITLNDP_TF.datadict.table=table_name
source.ALITLNDP_TF.datadict.example=3
```





---

-- end of document --