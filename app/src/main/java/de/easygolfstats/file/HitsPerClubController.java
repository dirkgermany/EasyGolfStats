package de.easygolfstats.file;

import androidx.annotation.WorkerThread;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
    private static String HITS_FILENAME_FINISHED = "hits_archived_";
    private static int CATEGORY_POS = 0;
    private static int CLUB_NAME_POS = 1;
    private static int QUALITY_GOOD_COUNTER_POS = 2;
    private static int QUALITY_NEUTRAL_COUNTER_POS = 3;
    private static int QUALITY_BAD_COUNTER_POS = 4;
    private static String CSV_SEPARATOR = ";";

    private static HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap;
    private static String fileDirectory = "";


    public static void initDataDirectory(String baseDirectory, String dataDirectory) {
        HitsPerClubController.fileDirectory = baseDirectory + "/" + dataDirectory;
        CsvFile.createDirectory(baseDirectory, dataDirectory);
    }

    public static boolean activeFileExists() {
        return CsvFile.fileExists(fileDirectory, HITS_FILENAME_ACTIVE + FILE_SUFFIX);
    }

    public static void beginNewSession() {
        finishStatistic();
        initializeFiles();
    }

    public static void initializeFiles() {
        BagController.initClubList(fileDirectory);
        if (!activeFileExists()) {
            ArrayList<Club> clubs = BagController.getClubListSorted();
            initHitFile(clubs);
        }
    }

    public static String finishStatistic () {

        try {
            String localDateTimeString = LocalDateTime.now().toString();
            String localDateTimePrepared = localDateTimeString.replaceAll("\\:", "-");
            localDateTimePrepared = localDateTimePrepared.replaceAll("\\.", "-");
            localDateTimePrepared = localDateTimePrepared.replaceAll("\\/", "-");

            String activeFileName = HITS_FILENAME_ACTIVE + FILE_SUFFIX;
            String archiveFileName = HITS_FILENAME_FINISHED + localDateTimePrepared + FILE_SUFFIX;

            CsvFile.renameFile(fileDirectory, activeFileName, archiveFileName);
            hitMap = null;
            return archiveFileName;
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }

    public static boolean isSessionOpen () {
        if (null == hitMap || hitMap.isEmpty()) {
            return false;
        }

        Iterator<ArrayList<HitsPerClub>> itOverAll = hitMap.values().iterator();
        while (itOverAll.hasNext()) {
            Iterator<HitsPerClub> itPerClub = itOverAll.next().iterator();
            while (itPerClub.hasNext()) {
                HitsPerClub hitsPerClub = itPerClub.next();
                if (hitsPerClub.getHitsOverAll() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setHitsPerClubAndCat(HitCategory category, Club club, HitsPerClub hitsPerClub) {
        HitsPerClub sourceHits = getHitsPerClubAndCat(category, club);
        sourceHits.setHitsGood(hitsPerClub.getHitsGood());
        sourceHits.setHitsBad(hitsPerClub.getHitsBad());
        sourceHits.setHitsNeutral(hitsPerClub.getHitsNeutral());

        writeHitsToFile();
    }

    public static HitsPerClub getHitsPerClubAndCat(HitCategory category, Club club) {
        if (null == hitMap) {
            return null;
        }
        ArrayList<HitsPerClub> hitsPerClubs = hitMap.get(category);
        if (null == hitsPerClubs) {
            hitsPerClubs = new ArrayList<>();
            HitsPerClub hitsPerClub = new HitsPerClub(club, 0, 0, 0);
            hitsPerClubs.add(hitsPerClub);
            hitMap.put(category,hitsPerClubs);
            return hitsPerClub;
        }

        Iterator<HitsPerClub> it = hitsPerClubs.iterator();
        while (it.hasNext()) {
            HitsPerClub hitsPerClub = it.next();
            if (hitsPerClub.getClubName().equals(club.getClubName())) {
                return hitsPerClub;
            }
        }

        HitsPerClub hitsPerClub = new HitsPerClub(club, 0, 0, 0);
        hitsPerClubs.add(hitsPerClub);
        hitMap.put(category,hitsPerClubs);
        return hitsPerClub;
    }
    
    public static Map<HitCategory, ArrayList<HitsPerClub>> readHitsFromActiveFile() {
        return readHitsFromFile(HITS_FILENAME_ACTIVE + FILE_SUFFIX, true);
    }
    
    public static HashMap<HitCategory, ArrayList<HitsPerClub>> readHitsFromHistoryFile(String fileName) {
        return readHitsFromFile(fileName, false);
    }

    private static HashMap<HitCategory, ArrayList<HitsPerClub>> readHitsFromFile(String fileName, boolean fillActiveHitMap) {
        // Get items from file
        String filePath = fileDirectory + "/" + fileName ;
        HashMap<HitCategory, ArrayList<HitsPerClub>> tempHitMap;

        tempHitMap = new HashMap<>();
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

                ArrayList<HitsPerClub> hits = tempHitMap.get(category);
                if (null == hits) {
                    hits = new ArrayList<>();
                }
                Club club = BagController.getClubByName(clubName);
                hits.add(new HitsPerClub(club, qualityGood, qualityNeutral, qualityBad));
                tempHitMap.put(category, hits);
            }
        }

        if (fillActiveHitMap) {
            hitMap = tempHitMap;
        }
        return tempHitMap;
    }

    public static void writeHitsToFile(HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap) {
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
                        .append(String.valueOf(hitsPerClub.getHitsGood()))
                        .append(CSV_SEPARATOR)
                        .append(String.valueOf(hitsPerClub.getHitsNeutral()))
                        .append(CSV_SEPARATOR)
                        .append(String.valueOf(hitsPerClub.getHitsBad())).toString();
                csvLines.add(csvLine);
            }
        }
        CsvFile.writeToFile(filePath, csvLines);
    }

    private static void writeHitsToFile () {
        writeHitsToFile(hitMap);
    }

    public static ArrayList<String> getHistoryFileNames() {
        ArrayList<String> historyFileNames = new ArrayList<>();

        Iterator<String> it = listFilesForFolder(new File(fileDirectory)).iterator();
        while (it.hasNext()) {
            String fileName = it.next();
            if (fileName.contains(HITS_FILENAME_FINISHED)) {
                historyFileNames.add(fileName);
            }
        }
        return historyFileNames;
    }

    private static ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> fileNames = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                fileNames.add(fileEntry.getName());
            }
        }
        return fileNames;
    }

    public static ArrayList<HitsPerClub> copyHitsPerClubFromFile(ArrayList<HitsPerClub>  destList) {
        destList.addAll(getHitsPerClubFromFile());
        return destList;
    }

    public static ArrayList<HitsPerClub> getHitsPerClubFromFile () {
        Map<HitCategory, ArrayList<HitsPerClub>> hitMap = readHitsFromActiveFile();

        HashMap<String, HitsPerClub> hitsPerClub = new HashMap<>();
        Iterator<Map.Entry<HitCategory, ArrayList<HitsPerClub>>> itMap = hitMap.entrySet().iterator();
        while (itMap.hasNext()) {
            Map.Entry<HitCategory, ArrayList<HitsPerClub>> pair = itMap.next();
            Iterator<HitsPerClub> itHits = pair.getValue().iterator();
            while (itHits.hasNext()) {
                HitsPerClub hitsPerClubAndCat = itHits.next();
                String clubName = hitsPerClubAndCat.getClubName();

                HitsPerClub summaryPerClub = hitsPerClub.get(clubName);
                if (null == summaryPerClub) {
                    Club club = BagController.getClubByName(clubName);
                    summaryPerClub = new HitsPerClub(club, hitsPerClubAndCat.getHitsGood(), hitsPerClubAndCat.getHitsNeutral(), hitsPerClubAndCat.getHitsBad());
                } else {
                    summaryPerClub.setHitsGood(summaryPerClub.getHitsGood() + hitsPerClubAndCat.getHitsGood());
                    summaryPerClub.setHitsNeutral(summaryPerClub.getHitsNeutral() + hitsPerClubAndCat.getHitsNeutral());
                    summaryPerClub.setHitsBad(summaryPerClub.getHitsBad() + hitsPerClubAndCat.getHitsBad());
                }
                hitsPerClub.put(clubName, summaryPerClub);
            }
        }
        ArrayList<HitsPerClub> values = new ArrayList<>(hitsPerClub.values());
        Collections.sort(values);
        return values;
    }

    public static void initHitFile(List<Club> clubs) {
        Iterator<Club> it = clubs.iterator();
        ArrayList<HitsPerClub> hitsPerClubs = new ArrayList<>();
        while (it.hasNext()) {
            Club club = it.next();
            HitsPerClub hitsPerClub = new HitsPerClub(club, 0, 0, 0);
            hitsPerClubs.add(hitsPerClub);
        }

        HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap = new HashMap<>();
        hitMap.put(HitCategory.REGULAR, hitsPerClubs);
        writeHitsToFile(hitMap);
    }

    public static void deleteHistoryFile(String fileName) {
        CsvFile.deleteFile(fileDirectory, fileName);
    }
    
    public static LocalDateTime extractDateFromArchivedFileName(String fileName) {
        String dateAsString = fileName.substring(0, fileName.indexOf(FILE_SUFFIX));
        dateAsString = dateAsString.substring(dateAsString.indexOf(HITS_FILENAME_FINISHED) + HITS_FILENAME_FINISHED.length());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss-n");
        return LocalDateTime.from(formatter.parse(dateAsString));
    }
}
