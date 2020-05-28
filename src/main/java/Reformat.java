import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class Reformat {

    private static Map<String, List<String[]>> dtList = new HashMap<>();
    private static List<Integer> secList = new ArrayList<>();

    public static void main(String args[]) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.println("파일이름을 입력해 주세요 (ex : file,file2,file2) '.csv 제외'");

        String[] fileNames = scanner.nextLine().split(",");

        for (String fileName : fileNames) {
            readDataFromCsv("./" + fileName + ".csv");
        }

        FileWriter fw = new FileWriter("./surf.dat", false);

        List<String> keyList = sortKeyList();

        printFileHeader(keyList, fw);

        printData(keyList, fw);

        System.out.println("surf.dat 파일 생성에 성공했습니다.");

        fw.close();
    }

    private static List<String> sortKeyList() {
        List<String> keyList = new ArrayList(dtList.keySet());

        keyList.sort(((o1, o2) -> {
            LocalDateTime ldt = strToLocalDateTime(o1);
            LocalDateTime ldt2 = strToLocalDateTime(o2);
            if (ldt.isAfter(ldt2))
                return 1;
            return -1;
        }));

        return keyList;
    }

    private static void printFileHeader(List<String> keyList, FileWriter fw) throws IOException {
        fw.write("SURF.DAT        2.0             Header structure with coordinate parameters\n");
        fw.write("   1                                                                       \n");
        fw.write("Produced by SMERGE Version: 5.57  Level: 070627                            \n");
        fw.write("NONE                                                                       \n");
        LocalDateTime startDt = strToLocalDateTime(keyList.get(0));
        LocalDateTime endDt = strToLocalDateTime(keyList.get(keyList.size() - 1));
        fw.write("\t" + startDt.getYear() + "\t"
                + startDt.getDayOfYear() + "\t"
                + startDt.getHour() + "\t"
                + endDt.getYear() + "\t"
                + endDt.getDayOfYear() + "\t"
                + endDt.getHour() + "\t"
                + "9" + "\t"
                + secList.size() + "\n");
        Collections.sort(secList);
        secList.forEach(o -> {
            try {
                fw.write("\t\t" + o + "\n");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("surf 파일 생성 실패");
            }
        });
    }


    public static void readDataFromCsv(String filePath) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(filePath)); // 1
        String[] nextLine;
        // remove header
        reader.readNext();
        while ((nextLine = reader.readNext()) != null) {   // 2
            if (dtList.containsKey(nextLine[1]) && !dtList.get(nextLine[1]).contains(nextLine)) {
                dtList.get(nextLine[1]).add(nextLine);
            } else {
                List<String[]> strList = new ArrayList<>();
                strList.add(nextLine);
                dtList.put(nextLine[1], strList);
            }

            if (!secList.contains(Integer.parseInt(nextLine[0])))
                secList.add(Integer.parseInt(nextLine[0]));
        }
    }


    private static void printData(List<String> keyList, FileWriter fw) {
        keyList.forEach(each -> {
            LocalDateTime localDateTime = strToLocalDateTime(each);
            try {
                fw.write(localDateTime.getYear() + "\t"
                        + localDateTime.getDayOfYear() + "\t"
                        + localDateTime.getHour() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("surf 파일 생성 실패");
            }

            List<String[]> tempList = dtList.get(each);

            tempList.sort((o1, o2) -> {
                // sort by section number
                if (Integer.parseInt(o1[0]) >= Integer.parseInt(o2[0])) {
                    return 1;
                }
                return -1;
            });

            tempList.forEach(nextLine -> {
                try {
                    for (int i = 2; i < nextLine.length; i++) {
                        if (nextLine[i].contains("."))
                            fw.write(String.format("%9.3f", Float.parseFloat(nextLine[i])));
                        else
                            fw.write(String.format("%5d", Integer.parseInt(nextLine[i])));
                    }
                    fw.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("surf 파일 생성 실패");
                }
            });

        });
    }

    public static LocalDateTime strToLocalDateTime(String str) {
        String[] dateAndTime = str.split(" ");
        String[] date = dateAndTime[0].split("\\.");
        String[] time = dateAndTime[1].split(":");

        return LocalDateTime.of(
                Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]),
                Integer.parseInt(time[0]), Integer.parseInt(time[1]));
    }
}
