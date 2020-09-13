package de.easygolfstats.file;

import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.easygolfstats.model.HitsPerClub;

@WorkerThread

/**
 * Handles the lists of clubs in the bag
 */
public class HitsPerTypeController {
    private static String HITS_FILENAME_TEMPLATE = "hits_.csv";
    private static int CLUB_NAME_POS = 0;
    private static int QUALITY_GOOD_COUNTER_POS = 1;
    private static int QUALITY_NEUTRAL_COUNTER_POS = 2;
    private static int QUALITY_BAD_COUNTER_POS = 3;
    private static String CSV_SEPARATOR = ";";

    public static ArrayList<HitsPerClub> readHitsFromFile(String fileDirectory) {
        // Get items from file
        String filePath = fileDirectory + "/" + HITS_FILENAME_TEMPLATE;
        ArrayList<HitsPerClub> fileItems = new ArrayList<>();

        List<ArrayList<String>> csvLines = CsvFile.readFile(filePath, CSV_SEPARATOR);
        if (null != csvLines) {
            Iterator<ArrayList<String>> it = csvLines.iterator();
            while (it.hasNext()) {
                ArrayList<String> nextItem = it.next();
                String clubName = nextItem.get(CLUB_NAME_POS);
                Integer qualityGood = Integer.valueOf(nextItem.get(QUALITY_GOOD_COUNTER_POS));
                Integer qualityNeutral = Integer.valueOf(nextItem.get(QUALITY_NEUTRAL_COUNTER_POS));
                Integer qualityBad = Integer.valueOf(nextItem.get(QUALITY_BAD_COUNTER_POS));
                fileItems.add(new HitsPerClub(clubName, qualityGood, qualityNeutral, qualityBad));
            }
        }
        return fileItems;
    }

    public static void writeHitsToFile(String fileDirectory, ArrayList<HitsPerClub> hitsPerClubs) {
        String filePath = fileDirectory + "/" + HITS_FILENAME_TEMPLATE;
        Iterator<HitsPerClub> it = hitsPerClubs.iterator();
        ArrayList<String> csvLines = new ArrayList<>();
        while (it.hasNext()) {
            HitsPerClub hitsPerClub = it.next();
            String csvLine = new StringBuilder()
                    .append(hitsPerClub.getClubName())
                    .append(CSV_SEPARATOR)
                    .append(hitsPerClub.getHitsGood())
                    .append(hitsPerClub.getHitsNeutral())
                    .append(hitsPerClub.getHitsBad()).toString();

            csvLines.add(csvLine);
        }
        CsvFile.writeToFile(filePath, csvLines);
    }
}
