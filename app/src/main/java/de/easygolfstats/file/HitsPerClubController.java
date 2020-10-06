package de.easygolfstats.file;

import androidx.annotation.WorkerThread;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.easygolfstats.model.Club;
import de.easygolfstats.model.HitsPerClub;
import de.easygolfstats.types.HitCategory;

@WorkerThread

/**
 * Handles the lists of clubs in the bag
 */
public class HitsPerClubController {
    private static String FILE_SUFFIX = ".csv";
    private static String HITS_FILENAME_ACTIVE = "hits_active";
    private static String HITS_FILENAME_FINISHED_TEMPLATE = "hits_archived_";
    private static int CATEGORY_POS = 0;
    private static int CLUB_NAME_POS = 1;
    private static int QUALITY_GOOD_COUNTER_POS = 2;
    private static int QUALITY_NEUTRAL_COUNTER_POS = 3;
    private static int QUALITY_BAD_COUNTER_POS = 4;
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

    public static Map<HitCategory, ArrayList<HitsPerClub>> readHitsFromFile(String fileDirectory) {
        // Get items from file
        String filePath = fileDirectory + "/" + HITS_FILENAME_ACTIVE + FILE_SUFFIX;
        Map<HitCategory, ArrayList<HitsPerClub>> fileItems = new HashMap<>();

        List<ArrayList<String>> csvLines = CsvFile.readFile(filePath, CSV_SEPARATOR);
        if (null != csvLines) {
            Iterator<ArrayList<String>> it = csvLines.iterator();
            while (it.hasNext()) {
                ArrayList<String> nextItem = it.next();
                HitCategory category = HitCategory.valueOf(nextItem.get(CATEGORY_POS));
                String clubName = nextItem.get(CLUB_NAME_POS);
                Integer qualityGood = Integer.valueOf(nextItem.get(QUALITY_GOOD_COUNTER_POS));
                Integer qualityNeutral = Integer.valueOf(nextItem.get(QUALITY_NEUTRAL_COUNTER_POS));
                Integer qualityBad = Integer.valueOf(nextItem.get(QUALITY_BAD_COUNTER_POS));

                ArrayList<HitsPerClub> hits = fileItems.get(category);
                if (null == hits) {
                    hits = new ArrayList<>();
                }
                hits.add(new HitsPerClub(clubName, qualityGood, qualityNeutral, qualityBad));
                fileItems.put(category, hits);
            }
        }
        return fileItems;
    }

    public static void writeHitsToFile(String fileDirectory, HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap) {
        String filePath = fileDirectory + "/" + HITS_FILENAME_ACTIVE + FILE_SUFFIX;
        Iterator<Map.Entry<HitCategory, ArrayList<HitsPerClub>>> it = hitMap.entrySet().iterator();
        ArrayList<String> csvLines = new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry<HitCategory, ArrayList<HitsPerClub>> pair = it.next();
            HitCategory category = pair.getKey();
            Iterator<HitsPerClub> itHits = pair.getValue().iterator();
            while (itHits.hasNext()) {
                HitsPerClub hitsPerClub = itHits.next();
                String csvLine = new StringBuilder()
                        .append(category.name())
                        .append(CSV_SEPARATOR)
                        .append(hitsPerClub.getClubName())
                        .append(CSV_SEPARATOR)
                        .append(hitsPerClub.getHitsGood())
                        .append(CSV_SEPARATOR)
                        .append(hitsPerClub.getHitsNeutral())
                        .append(CSV_SEPARATOR)
                        .append(hitsPerClub.getHitsBad()).toString();
                csvLines.add(csvLine);
            }
        }
        CsvFile.writeToFile(filePath, csvLines);
    }

    public static ArrayList<HitsPerClub> getHitsPerClubFromFile (String fileDirectory) {
        Map<HitCategory, ArrayList<HitsPerClub>> hitMap = readHitsFromFile(fileDirectory);

        HashMap<String, HitsPerClub> hitsPerClub = new HashMap<>();
        Iterator<Map.Entry<HitCategory, ArrayList<HitsPerClub>>> itMap = hitMap.entrySet().iterator();
        while (itMap.hasNext()) {
            Map.Entry<HitCategory, ArrayList<HitsPerClub>> pair = itMap.next();
            //           HitCategory category = pair.getKey();
            Iterator<HitsPerClub> itHits = pair.getValue().iterator();
            while (itHits.hasNext()) {
                HitsPerClub hit = itHits.next();
                String clubName = hit.getClubName();

                HitsPerClub summaryPerClub = hitsPerClub.get(clubName);
                if (null == summaryPerClub) {
                    summaryPerClub = new HitsPerClub(clubName, 0, 0, 0);
                } else {
                    summaryPerClub.setHitsGood(summaryPerClub.getHitsGood() + hit.getHitsGood());
                    summaryPerClub.setHitsNeutral(summaryPerClub.getHitsNeutral() + hit.getHitsNeutral());
                    summaryPerClub.setHitsBad(summaryPerClub.getHitsBad() + hit.getHitsBad());
                }
                hitsPerClub.put(clubName, summaryPerClub);
            }
        }

        return new ArrayList<>(hitsPerClub.values());
    }

    public static void initHitFile(String fileDirectory, List<Club> clubs) {
        Iterator<Club> it = clubs.iterator();
        ArrayList<HitsPerClub> hitsPerClubs = new ArrayList<>();
        while (it.hasNext()) {
            Club club = it.next();
            HitsPerClub hitsPerClub = new HitsPerClub(club.getClubName(), 0, 0, 0);
            hitsPerClubs.add(hitsPerClub);
        }

        HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap = new HashMap<>();
        hitMap.put(HitCategory.REGULAR, hitsPerClubs);
        writeHitsToFile(fileDirectory, hitMap);
    }
}
