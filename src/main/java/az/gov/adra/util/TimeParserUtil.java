package az.gov.adra.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public interface TimeParserUtil {

    DateTimeFormatter birthDateFormatter = DateTimeFormatter.ofPattern("dd-MM");

    static String formatBirthDate(LocalDate birthDate) {
         String[] dayAndMonth = birthDateFormatter.format(birthDate).split("-");
         switch (dayAndMonth[1]) {
             case "1":
             case "01":
                 dayAndMonth[1] = "Yanvar";
                 break;
             case "2":
             case "02":
                 dayAndMonth[1] = "Fevral";
                 break;
             case "3":
             case "03":
                 dayAndMonth[1] = "Mart";
                 break;
             case "4":
             case "04":
                 dayAndMonth[1] = "Aprel";
                 break;
             case "5":
             case "05":
                 dayAndMonth[1] = "May";
                 break;
             case "6":
             case "06":
                 dayAndMonth[1] = "İyun";
                 break;
             case "7":
             case "07":
                 dayAndMonth[1] = "İyul";
                 break;
             case "8":
             case "08":
                 dayAndMonth[1] = "Avqust";
                 break;
             case "9":
             case "09":
                 dayAndMonth[1] = "Sentyabr";
                 break;
             case "10":
                 dayAndMonth[1] = "Oktyabr";
                 break;
             case "11":
                 dayAndMonth[1] = "Noyabr";
                 break;
             case "12":
                 dayAndMonth[1] = "Dekabr";
                 break;
         }
         return String.valueOf(dayAndMonth[0] + " " + dayAndMonth[1]);
    }

}
