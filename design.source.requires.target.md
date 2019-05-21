# Design of source.requires.target
(deprecated design)


### Sequence Diagram

```sequence
Target->Target_DataTable:buildTarget
Target_DataTable-->Source_DataTable:lookup data from another source
Source_DataTable-->Target_DataTable:...
Target_DataTable-->Target_DataTable:lookup data from another target
Target_DataTable-->Source:required by source
Source->Source_DataTable:buildSource
Source_DataTable-->Source_DataTable:lookup data from another source
Source_DataTable-->Target_DataTable:lookup data from target
Target_DataTable-->Source_DataTable:...
Source_DataTable->Source_Output:printSource
Source_Output-->Source_DataTable:...
Source_DataTable-->Target_DataTable:...
Target_DataTable->Target_Output:printTarget
Target_Output-->Target_DataTable:...
Target_DataTable-->Target:...
```


### Alternative Way<br/><sup><sup>(recommended)</sup></sup>

Using 2 converters, first converter to prepare/compute data and then store it in a markdown output, the second converter need to buildSource from markdown output at first source and then normal steps without source.requires.target.



-------

-- end of document --