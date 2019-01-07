
> Generated by dconvers at 2018/12/21 18:13:09.  
> This markdown file contains 15 rows from source(branch) in converter(sample-converter.conf)  
> DataSource : push  
> Query : 
-- sample sql : to use this content please see Dynamic Value Expression in readme.md --

select b.*, TRIM(a.addr1) as village_no, TRIM(a.addr2) as road, TRIM(a.addr3) as village, a.email, a.faxno, a.mobileno, a.postcode, a.telno
from branch b, address a
where b.address_id = a.id
and b.activeflag = 1
order by b.branchcode
;
  


# TABLE: BRANCH<br/><sup><sup>(branch)</sup></sup>
| No. | id | activeflag | modifydate (yyyy/MM/dd HH:mm:ss) | branchcode | ebranchname                | tbranchname                              | branchtype | closedate (yyyy/MM/dd HH:mm:ss) | domain                    | juristicperson | opendate (yyyy/MM/dd HH:mm:ss) | placeorder | pushorder | premiumauto | realtimeprice | user_id | account_id | address_id | agent | branch | menu | slip | pre_print | dw_cash_stock | automatch | automatch_silver | sendemailtrade | match_clear_port | net_position | village_no | road           | village                                  | email               | faxno       | mobileno    | postcode | telno                              |
|----:|---:|-----------:|:--------------------------------:|------------|----------------------------|------------------------------------------|-----------:|:-------------------------------:|---------------------------|----------------|:------------------------------:|-----------:|----------:|------------:|--------------:|--------:|-----------:|-----------:|------:|-------:|------|-----:|----------:|--------------:|----------:|-----------------:|---------------:|-----------------:|-------------:|------------|----------------|------------------------------------------|---------------------|-------------|-------------|----------|------------------------------------|
| 1   |  2 |          1 | 2016/07/27 11:55:25              | 100        | SCTGOLD                    | SCTGOLD                                  |          1 | 9999/12/31 00:00:00             | www.sctgold.com           | null           | 2012/10/21 00:00:00            |          1 |         0 |           1 |             1 |       2 |       null |          2 |     0 |      0 | 0    |    1 |         1 |             1 |         1 |                0 |              1 |                1 |            1 | 99-101     | เจริญกรุง      | แขวงวังบูรพาภิรมย์ เขตพระนคร กรุงเทพฯ    |                     | 02-222-7100 | 02-222-4111 | 10200    | 02-222-3111                        |
| 2   | 18 |          1 | 2016/07/27 11:56:42              | 102        | Thongpatra Gold and Silver | บริษัท ทองภัทรโกลด์ แอนด์ ซิลเวอร์ จำกัด |          1 | 9456/12/31 00:00:00             | www.thongpatra.com        | null           | 2016/07/19 00:00:00            |          1 |         0 |           1 |             1 |       2 |       null |       2838 |     1 |      1 | 2    |    1 |         1 |             1 |         0 |                0 |              1 |                1 |            0 | 130/7,8,9  | เฟื่องนคร      | แขวงวังบูรพาภิรมย์ เขตพระนคร กทม         |                     | 02-224-7733 |             | 10200    | 02-222-2895                        |
| 3   | 21 |          1 | 2017/08/09 17:18:34              | 103        | Tae Jing Seng              | แต้จิงเส็ง                               |          1 | 2099/12/31 00:00:00             | www.tjsgold.com           | null           | 2017/01/30 00:00:00            |          1 |         1 |           1 |             1 |       2 |        936 |       4905 |     1 |      0 | 2    |    1 |         1 |             1 |         0 |                0 |              0 |                1 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 4   | 19 |          1 | 2017/06/07 10:35:30              | 104        | Oriental Pride Co.,Ltd.    | บริษัท ออเรียลทัล ไพรด์ จำกัด            |          1 | 2017/06/07 00:00:00             |                           | null           | 2016/12/10 00:00:00            |          0 |         0 |           1 |             1 |       2 |       null |       4518 |     1 |      0 | 2    |    1 |         1 |             1 |         0 |                0 |              0 |                1 |            0 |            |                |                                          | witit.t@sctgold.com |             |             |          |                                    |
| 5   | 22 |          1 | 2017/01/30 16:04:14              | 105        | Watcharin Gold             | วัชรินทร์โกลด์                           |          1 | 2099/12/31 00:00:00             | www.watcharin.com         | null           | 2017/01/30 00:00:00            |          1 |         1 |           1 |             1 |      97 |       null |       4917 |     1 |      0 | 2    |    1 |         1 |             1 |         0 |                0 |              0 |                1 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 6   | 11 |          1 | 2016/07/27 11:56:12              | 300        | Saeng Ma Nee Gold          | Saeng Ma Nee Gold                        |          1 | 9999/12/31 00:00:00             | www.smngold.com           | null           | 2014/01/23 00:00:00            |          1 |         1 |           1 |             1 |       2 |          6 |          1 |     1 |      0 | 1    |    1 |         1 |             1 |         0 |                0 |              1 |                0 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 7   |  8 |          1 | 2016/07/27 11:55:51              | 410        | Udon Gold Trade            | Udon Gold Trade                          |          1 | 9999/12/31 00:00:00             | www.udongold.com          | null           | 2014/10/30 00:00:00            |          1 |         0 |           1 |             1 |       2 |       null |         66 |     1 |      0 | 1    |    1 |         1 |             1 |         0 |                0 |              1 |                0 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 8   | 15 |          1 | 2017/03/31 15:50:43              | 470        | Muangploy Goldsmith        | ห้างทองเมืองพลอย                         |          1 | 2017/03/31 00:00:00             | www.muangploy.co.th       | null           | 2016/03/14 00:00:00            |          1 |         0 |           1 |             1 |       2 |       null |       2752 |     1 |      0 | 2    |    1 |         1 |             1 |         0 |                0 |              1 |                1 |            0 | 1760/3-5   | ถนนประชาราษฎร์ | ตำบลธาตุเชิงชุม อำเภอเมือง จังหวัดสกลนคร |                     | 042-736100  |             | 47000    | 042-712865, 042-733900, 042-716900 |
| 9   | 10 |          1 | 2017/12/26 09:20:19              | 500        | Yong Chiang Long Gold      | Yong Chiang Long Gold                    |          1 | 9999/12/31 00:00:00             | www.yclgoldonline.com     | null           | 2014/01/16 00:00:00            |          1 |         0 |           1 |             1 |      80 |       null |        874 |     1 |      0 | 2    |    1 |         1 |             1 |         0 |                0 |              1 |                1 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 10  | 12 |          1 | 2017/10/05 10:18:37              | 600        | Wang Yak Mee               | Wang Yak Mee                             |          1 | 2017/10/01 00:00:00             | www.wangyakmeegold.com    | null           | 2014/10/30 00:00:00            |          1 |         0 |           1 |             1 |       2 |       null |       1431 |     1 |      0 | 3    |    0 |         0 |             0 |         0 |                0 |              1 |                1 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 11  | 20 |          1 | 2017/01/30 14:14:08              | 601        | HangThong Vangthongdee     | ห้างทองหวังทองดี                         |          1 | 2099/12/31 00:00:00             | www.vtdgold.com           | null           | 2017/01/30 00:00:00            |          1 |         1 |           1 |             1 |      95 |       null |       4904 |     1 |      0 | 2    |    1 |         1 |             1 |         0 |                0 |              0 |                1 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 12  |  3 |          1 | 2017/12/05 21:13:47              | 700        | HangThong Mungkornkoo      | HangThong Mungkornkoo                    |          1 | 9999/12/31 00:00:00             | www.mkkgold.com           | null           | 2013/05/30 00:00:00            |          1 |         0 |           1 |             1 |      14 |          1 |         66 |     1 |      0 | 2    |    1 |         1 |             1 |         0 |                0 |              1 |                1 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 13  |  5 |          1 | 2018/05/28 08:33:53              | 730        | Hangthong Somnuek          | Hangthong Somnuek                        |          1 | 9999/12/31 00:00:00             | www.thongsomnuek.com      | null           | 2013/06/12 00:00:00            |          1 |         1 |           1 |             1 |      15 |          3 |         67 |     1 |      0 | 2    |    1 |         1 |             1 |         0 |                0 |              1 |                1 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 14  | 13 |          1 | 2016/07/27 11:56:27              | 901        | Thomson Gold               | Thomson Gold                             |          1 | 9999/12/31 00:00:00             | www.thomsongoldonline.com | null           | 2015/11/20 00:00:00            |          1 |         1 |           1 |             1 |       2 |        640 |       1597 |     1 |      0 | 2    |    1 |         1 |             1 |         0 |                0 |              1 |                1 |            0 |            |                |                                          |                     |             |             |          |                                    |
| 15  |  1 |          1 | null                             | S01        | System Default             | System default                           |          0 | null                            | System default            | null           | null                           |          1 |         0 |           1 |             1 |    null |       null |          1 |     1 |      0 | 0    |    1 |         1 |             1 |         0 |                0 |              0 |                0 |            0 |            |                |                                          |                     |             |             |          |                                    |
