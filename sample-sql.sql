
-- sample sql : to use this content please see Dynamic Value Expression in readme.md --

select b.*, TRIM(a.addr1) as village_no, TRIM(a.addr2) as road, TRIM(a.addr3) as village, a.email, a.faxno, a.mobileno, a.postcode, a.telno
from branch b, address a
where b.address_id = a.id
and b.activeflag = 1
order by b.branchcode
;
