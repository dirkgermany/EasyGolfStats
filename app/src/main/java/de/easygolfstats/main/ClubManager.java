package de.easygolfstats.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.easygolfstats.file.CsvFile;
import de.easygolfstats.model.Club;
import de.easygolfstats.model.HitsPerClub;
import de.easygolfstats.types.ClubType;

public class ClubManager {
    private static String CLUB_FILENAME = "clubs.csv";
    private static int CLUB_NAME_POS = 0;
    private static int CLUB_TYPE_POS = 1;
    private static String CSV_SEPARATOR = ";";

    public static ArrayList<Club> getClubList (String fileDirectory) {
        String filePath = fileDirectory + "/" + CLUB_FILENAME;

        ArrayList<Club> fileItems = new ArrayList<>();

        List<ArrayList<String>> csvLines = CsvFile.readFile(filePath, CSV_SEPARATOR);
        if (null != csvLines) {
            Iterator<ArrayList<String>> it = csvLines.iterator();
            while (it.hasNext()) {
                ArrayList<String> nextItem = it.next();
                String clubName = nextItem.get(CLUB_NAME_POS);
                String clubTypeAsString = nextItem.get(CLUB_TYPE_POS);
                ClubType clubType = ClubType.valueOf(clubTypeAsString);
                fileItems.add(new Club(clubName, clubType));
            }
        }

        if (fileItems.size() == 0) {
            fileItems = createDefaultClubList(filePath);
        }
        return fileItems;
    }

    public static ArrayList<Club> createDefaultClubList (String filePath) {
        ArrayList<Club> clubs = new ArrayList<>();
        clubs.add(new Club ("Driver", ClubType.DRIVER_1));
        clubs.add( new Club ("Putter", ClubType.PUTTER_1));
        clubs.add(new Club ("SW", ClubType.SAND_WEDGE));
        clubs.add( new Club ("GW", ClubType.GAP_WEDGE));
        clubs.add(new Club ("PW", ClubType.PITCHING_WEDGE));
        clubs.add(new Club ("Iron 9", ClubType.IRON_9));
        clubs.add(new Club ("Iron 8", ClubType.IRON_8));
        clubs.add(new Club ("Iron 7", ClubType.IRON_7));
        clubs.add(new Club ("Iron 6", ClubType.IRON_6));
        clubs.add(new Club ("Iron 5", ClubType.IRON_5));
        clubs.add(new Club ("Hybrid 4", ClubType.HYBRID_4));

        ArrayList<String> csvLines = new ArrayList<>();
        Iterator<Club> it = clubs.iterator();
        while (it.hasNext()) {
            Club club = it.next();
            String csvLine = new StringBuilder()
                    .append(club.getClubName())
                    .append(CSV_SEPARATOR)
                    .append(club.getClubType()).toString();
            csvLines.add(csvLine);
        }

        CsvFile.writeToFile(filePath, csvLines);
        return clubs;
    }
}
