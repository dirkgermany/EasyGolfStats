package de.easygolfstats.file;

import androidx.annotation.WorkerThread;

import de.easygolfstats.model.Club;
import de.easygolfstats.types.ClubType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@WorkerThread

/**
 * Handles the lists of clubs in the bag
 */
public class BagController {
    private static String BAG_FILENAME = "bag.csv";
    private static int CLUB_NAME_POS = 0;
    private static int CLUB_TYPE_POS = 1;
    private static String CSV_SEPARATOR = ";";

    public static ArrayList<Club> readBagFromFile(String fileDirectory) {
        // Get items from file
        String filePath = fileDirectory + "/" + BAG_FILENAME;
        ArrayList<Club> fileItems = new ArrayList<>();

        List<ArrayList<String>> csvLines = CsvFile.readFile(filePath, CSV_SEPARATOR);
        if (null != csvLines) {
            Iterator<ArrayList<String>> it = csvLines.iterator();
            while (it.hasNext()) {
                ArrayList<String> nextItem = it.next();
                String clubName = nextItem.get(CLUB_NAME_POS);
                ClubType clubType =  ClubType.valueOf(nextItem.get(CLUB_TYPE_POS));
                fileItems.add(new Club(clubName, clubType));
            }
        }
        return fileItems;
    }

    public static void writeBagToFile(String fileDirectory, ArrayList<Club> clubs) {
        String filePath = fileDirectory + "/" + BAG_FILENAME;
        Iterator<Club> it = clubs.iterator();
        ArrayList<String> csvLines = new ArrayList<>();
        while (it.hasNext()) {
            Club club = it.next();
            String csvLine = new StringBuilder().append(club.getClubName())
                    .append(CSV_SEPARATOR)
                    .append(club.getClubType()).toString();

            csvLines.add(csvLine);
        }
        CsvFile.writeToFile(filePath, csvLines);
    }
}
