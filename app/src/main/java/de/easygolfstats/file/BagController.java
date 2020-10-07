package de.easygolfstats.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.easygolfstats.model.Club;
import de.easygolfstats.types.ClubType;

public class BagController {
    private static String CLUB_FILENAME = "clubs.csv";
    private static int CLUB_NAME_POS = 0;
    private static int CLUB_TYPE_POS = 1;
    private static int CLUB_INDEX_POS = 2;
    private static String CSV_SEPARATOR = ";";

    private static ArrayList<Club> clubList;

    public static void initClubList (String fileDirectory) {
        if (null == clubList || clubList.isEmpty()) {
            readClubListFromFile(fileDirectory);
            Collections.sort(clubList);
        }
    }

    public static ArrayList<Club> getClubListSorted () {
        return clubList;
    }

    public static Club getClubByName (String clubName) {
        Iterator<Club> it = clubList.iterator();
        while (it.hasNext()) {
            Club club = it.next();
            if (club.getClubName().equals(clubName)) {
                return club;
            }
        }
        return null;
    }

    private static void readClubListFromFile (String fileDirectory) {
        String filePath = fileDirectory + "/" + CLUB_FILENAME;

        if (null == clubList) {
            clubList = new ArrayList<>();
        }

        List<ArrayList<String>> csvLines = CsvFile.readFile(filePath, CSV_SEPARATOR);
        if (null != csvLines) {
            Iterator<ArrayList<String>> it = csvLines.iterator();
            while (it.hasNext()) {
                ArrayList<String> nextItem = it.next();
                String clubName = nextItem.get(CLUB_NAME_POS);
                String clubTypeAsString = nextItem.get(CLUB_TYPE_POS);
                ClubType clubType = ClubType.valueOf(clubTypeAsString);
                String clubIndexAsString = nextItem.get(CLUB_INDEX_POS);
                Integer clubIndex = Integer.valueOf(clubIndexAsString);
                clubList.add(new Club(clubName, clubType, clubIndex));
            }
        }

        if (clubList.size() == 0) {
            createDefaultClubList(filePath);
        }
    }

    private static void createDefaultClubList (String filePath) {
        clubList.add(new Club ("Driver", ClubType.DRIVER_1, 0));
        clubList.add( new Club ("Putter", ClubType.PUTTER_1, 1));
        clubList.add(new Club ("SW", ClubType.SAND_WEDGE, 2));
        clubList.add( new Club ("GW", ClubType.GAP_WEDGE, 3));
        clubList.add(new Club ("PW", ClubType.PITCHING_WEDGE, 4));
        clubList.add(new Club ("Iron 9", ClubType.IRON_9, 5));
        clubList.add(new Club ("Iron 8", ClubType.IRON_8, 6));
        clubList.add(new Club ("Iron 7", ClubType.IRON_7, 7));
        clubList.add(new Club ("Iron 6", ClubType.IRON_6,  8));
        clubList.add(new Club ("Iron 5", ClubType.IRON_5, 9));
        clubList.add(new Club ("Hybrid 4", ClubType.HYBRID_4, 10));

        ArrayList<String> csvLines = new ArrayList<>();
        Iterator<Club> it = clubList.iterator();
        while (it.hasNext()) {
            Club club = it.next();
            String csvLine = new StringBuilder()
                    .append(club.getClubName())
                    .append(CSV_SEPARATOR)
                    .append(club.getClubType())
                    .append(CSV_SEPARATOR)
                    .append(club.getClubIndex())
                    .toString();
            csvLines.add(csvLine);
        }

        CsvFile.writeToFile(filePath, csvLines);
    }
}
