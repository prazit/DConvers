package com.clevel.dconvers;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SourceUT {

    private Logger log = LoggerFactory.getLogger(SourceUT.class);

    @Test
    public void test() {
        log.debug("------- test is started");

        String emails = "prazit@the-c-level.com,bt.first@bt.asia, phongsathorn@the-c-level.com";
        List<String> emailList = Arrays.asList(emails.split("[,]"));
        log.debug("from: " + emails);
        log.debug("to: " + getNameFromEmail(emailList));

        /*radianPrint(new Point(0, 0), new Point(90, 90));
        radianPrint(new Point(0, 0), new Point(50, 50));
        radianPrint(new Point(0, 0), new Point(45, 90));
        radianPrint(new Point(0, 0), new Point(90, 45));*/

        /*dataTypePrint("03-01-20");
        dataTypePrint("|03-01-20|");
        dataTypePrint("|03-01-20|");
        dataTypePrint(" 03-01-20.");*/

        /*dataTypePrint("401.300.25");
        dataTypePrint("1,401,300.25");
        dataTypePrint("401 300.25");
        dataTypePrint("400300.25");
        dataTypePrint("31,690,00");*/

        /*containsNumberPrint("XXX-2-04177-X");
        containsNumberPrint("1XX-X-XXXXX-X");
        containsNumberPrint("XXX-X-XXXXX-1");
        containsNumberPrint("03-01-2020");
        containsNumberPrint("3,040.62");

        containsNumberPrint("บริการรับรายการเดินบัญชีทางอีเมล์กสิกรไทย");
        containsNumberPrint("วัน");
        containsNumberPrint("เดือน");
        containsNumberPrint("ปี");
        containsNumberPrint("รายการ");
        containsNumberPrint("ถอนเงิน");
        containsNumberPrint("ฝากเงิน");
        containsNumberPrint("ยอดคงเหลือ");
        containsNumberPrint("/");
        containsNumberPrint("ผู้ทำรายการ");
        containsNumberPrint("ช่องทาง");
        containsNumberPrint("ผู้ทำรายการ");
        containsNumberPrint("เวลาที่ทำรายการ");
        containsNumberPrint("วันที่มีผล");*/

        /*similarityPrint("เวลา","เวลป");
        similarityPrint("รายการ","ราชการ");
        similarityPrint("ถอนเงิน/ฝากเงิน","ถอนผิน/ปากเผัน");
        similarityPrint("วันที่","วันที่นีคด");*/

        log.debug("------- test is ended");
    }


    private String getNameFromEmail(List<String> emailAddressList) {
        String commaSeparatedName = "";
        for (String email : emailAddressList) {
            int atIndex = email.indexOf("@");
            commaSeparatedName += email.substring(0, atIndex).trim() + ",";
        }
        return commaSeparatedName.substring(0, commaSeparatedName.length() - 1);
    }


    private void radianPrint(Point point1, Point point2) {
        double radian = calRadian(point1.x, point1.y, point2.x, point2.y);
        double degree = Math.toDegrees(radian);
        log.debug("Point1(x:{},y:{}), Point2(x:{},y:{}), Radian({}), Degree({})", point1.x, point1.y, point2.x, point2.y, radian, degree);
    }

    //------- Temporary Functions

    /**
     * Calculate radian between two points.
     */
    private double calRadian(double point1X, double point1Y, double point2X, double point2Y) {
        double diffY = point2Y - point1Y;
        double diffX = point2X - point1X;
        return Math.atan(diffY / diffX);
    }

    /**
     * @return datatype of the valueColumn, possible value need to compatible with OCR_CONS_FIELD.DEFAULT_DATA_TYPE are following: date, number, text.
     */
    private String detectDataTypeByValue(String valueString) {

        /* Priority#1: need to replace invalid number character before detect datatype.
         *      dateRegex: [Oo] => 0
         *      dateRegex: [l|] => 1
         */
        valueString = StringUtils.strip(valueString, "|. ");
        valueString = replaceInvalidCharacterOfNumber(valueString);
        int valueLength = valueString.length();

        /* Priority#2: date without time: key is month in the middle : regex : (20)*(0[1-9]|[1-2][0-9]|3[0-1])[/.-]*(01|02|03|04|05|06|07|08|09|10|11|12)[/.-]*(20)*(0[1-9]|[1-2][0-9]|3[0-1])
         *      191231
         *      311219
         *      07.08.2020
         *      2020.08.07
         *      31/12/19
         *      31-12-19
         *      2020-02-29
         *      29-02-2020
         */
        String dateRegex = "(20)*(0[1-9]|[1-2][0-9]|3[0-1])[/.-]*(01|02|03|04|05|06|07|08|09|10|11|12)[/.-]*(20)*(0[1-9]|[1-2][0-9]|3[0-1])";
        if ((valueLength == 6 || valueLength == 8 || valueLength == 10)
                && matchRegEx(valueString, dateRegex)) {
            return "date";
        }

        /* Priority#3: time without date : regex : [0-2][0-9][:;][0-5][0-9]([:;][0-5][0-9])*
         *      22:15
         *      09:09
         */
        String timeRegex = "[0-2][0-9][:;][0-5][0-9]([:;][0-5][0-9])*";
        if ((valueLength == 5 || valueLength == 7)
                && matchRegEx(valueString, timeRegex)) {
            return "time";
        }

        /* Priority#4: date with time : regex : <DateRegEx> <TimeRegEx>
         *      311219 22:15
         */
        dateRegex += "[ ,]" + timeRegex;
        if ((valueLength == 12 || valueLength == 14 || valueLength == 16 || valueLength == 18)
                && matchRegEx(valueString, dateRegex)) {
            return "datetime";
        }

        /* Priority#5: number: key is 2 decimal places on the right side and one or more group of 3-digits on the left side : dateRegex : ([0-9]{0,3}[., ]{0,1}([0-9]{3}[., ]{0,1})*)[., ][0-9]{2}
         *      401.300.25
         *    1,401,300.25
         *      401 300.25
         *       400300.25
         */
        String numberRegex = "([0-9]{0,3}[., ]{0,1}([0-9]{3}[., ]{0,1})*)[., ][0-9]{2}";
        if (matchRegEx(valueString, numberRegex)) {
            return "number";
        }

        /* otherwise: text:
         **/
        return "text";
    }

    private String replaceInvalidCharacterOfNumber(String numberString) {
        /* Priority#1: need to replace invalid number character before detect datatype.
         *      dateRegex: [Oo] => 0
         *      dateRegex: [l|] => 1
         */
        numberString = numberString.replaceAll("[Oo]", "0");
        numberString = numberString.replaceAll("[l|]", "1");
        return numberString;
    }

    private boolean matchRegEx(String valueString, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(valueString).matches();
    }

    private void dataTypePrint(String value) {
        String dataType = detectDataTypeByValue(value);
        log.debug("value({}) dataType = {}", value, dataType);
    }

    private void containsNumberPrint(String text) {
        if (containsNumber(text)) {
            System.out.println("'" + text + "' contains some numbers.");
        } else {
            System.out.println("'" + text + "' has no number.");
        }
    }

    private boolean containsNumber(String text) {
        String regex = ".*[0-9].*";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(text).matches();
    }

    private void similarityPrint(String keyWord, String word) {
        System.out.println("'" + keyWord + "' is " + similarity(word, keyWord) + "% similar to '" + word + "'");
    }

    public double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }

        int longerLength = longer.length();
        if (longerLength == 0) {
            return 100;
        }

        return ((longerLength - editDistance(longer, shorter)) / (double) longerLength) * 100;
    }

    private int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

}
