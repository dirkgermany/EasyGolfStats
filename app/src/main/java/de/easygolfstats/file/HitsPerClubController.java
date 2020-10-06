package de.easygolfstats.file;

import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.easygolfstats.model.Club;
import de.easygolfstats.model.HitsPerClub;
import de.easygolfstats.types.ClubType;

@WorkerThread

/**
 * Handles the lists of clubs in the bag
 */
public class HitsPerClubController {
    private static String FILE_SUFFIX = ".csv";
    private static String HITS_FILENAME_ACTIVE = "hits_active";
    private static String HITS_FILENAME_FINISHED_TEMPLATE = "hits_archived_";
    private static int CLUB_NAME_POS = 0;
    private static int QUALITY_GOOD_COUNTER_POS = 1;
    private static int QUALITY_NEUTRAL_COUNTER_POS = 2;
    private static int QUALITY_BAD_COUNTER_POS = 3;
    private static String CSV_SEPARATOR = ";";


    public static void initDataDirectory(String baseDirectory, String dataDirectory) {
        CsvFile.createDirectory(baseDirectory, dataDirectory);
    }

    public static boolean isStatisticOpen (String fileDirectory) {
        return CsvFile.fileExists(fileDirectory, HITS_FILENAME_ACTIVE + FILE_SUFFIX);
    }

    public static void finishStatistic (String fileDirectory) {
        String activeFileName = HITS_FILENAME_ACTIVE + FILE_SUFFIX;
        String archiveFileName = HITS_FILENAME_FINISHED_TEMPLATE + new Date().toString() + FILE_SUFFIX;

        CsvFile.renameFile(fileDirectory, activeFileName, archiveFileName);
    }

    public static ArrayList<HitsPerClub> readHitsFromFile(String fileDirectory) {
        // Get items from file
        String filePath = fileDirectory + "/" + HITS_FILENAME_ACTIVE + FILE_SUFFIX;
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
        String filePath = fileDirectory + "/" + HITS_FILENAME_ACTIVE + FILE_SUFFIX;
        Iterator<HitsPerClub> it = hitsPerClubs.iterator();
        ArrayList<String> csvLines = new ArrayList<>();
        while (it.hasNext()) {
            HitsPerClub hitsPerClub = it.next();
            String csvLine = new StringBuilder()
                    .append(hitsPerClub.getClubName())
                    .append(CSV_SEPARATOR)
                    .append(hitsPerClub.getHitsGood())
                    .append(CSV_SEPARATOR)
                    .append(hitsPerClub.getHitsNeutral())
                    .append(CSV_SEPARATOR)
                    .append(hitsPerClub.getHitsBad()).toString();

            csvLines.add(csvLine);
        }
        CsvFile.writeToFile(filePath, csvLines);
    }

    public static void initHitFile(String fileDirectory, List<Club> clubs) {
        Iterator<Club> it = clubs.iterator();
        ArrayList<HitsPerClub> hitsPerClubs = new ArrayList<>();
        while (it.hasNext()) {
            Club club = it.next();
            HitsPerClub hitsPerClub = new HitsPerClub(club.getClubName(), 0, 0, 0);
            hitsPerClubs.add(hitsPerClub);
        }
        String filePath = fileDirectory + "/" + HITS_FILENAME_ACTIVE + FILE_SUFFIX;
        writeHitsToFile(fileDirectory, hitsPerClubs);
    }
}
