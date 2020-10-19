package de.easygolfstats.rest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.easygolfstats.file.HitsPerClubController;
import de.easygolfstats.model.Hits;
import de.easygolfstats.model.HitsPerClub;
import de.easygolfstats.types.HitCategory;

public class UpdateHitsDataAtServer {

    Diese Klasse mit Timer versehen, der den Datenabgleich regelmäßig anstößt

    private void updateAtServer() {
        List<String> historyFileList = HitsPerClubController.getHistoryFiles();

        Iterator<String> it = historyFileList.iterator();
        while (it.hasNext()) {
            String fileName = it.next();

            HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap = HitsPerClubController.readHitsFromHistoryFile(fileName);
            if (null != hitMap && !hitMap.isEmpty()) {
                Date sessionDate = extractDateFromFileName(fileName);
                processMap(sessionDate, hitMap);
            }


            HitsPerClubController.deleteHistoryFile(fileName);
        }


    }

    private void processMap(Date sessionDate, HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap) {
        Long userId =
        Iterator<Map.Entry<HitCategory, ArrayList<HitsPerClub>>> itMap = hitMap.entrySet().iterator();
        ArrayList<Hits> hitsList = new ArrayList<>();

        while (itMap.hasNext()) {
            Map.Entry<HitCategory, ArrayList<HitsPerClub>> entry = itMap.next();
            HitCategory hitCategory = entry.getKey();
            Iterator<HitsPerClub> itArray = entry.getValue().iterator();
            while (itArray.hasNext()) {
                Hits hits = new Hits();
                HitsPerClub hitsPerClub = itArray.next();

                hits.setUserId(XXX);
                hits.setHitCategory(hitCategory);
                hits.setClubType(hitsPerClub.getClub().getClubType());
                hits.setSessionDate(sessionDate);
                hits.setHitCountGood(hitsPerClub.getHitsGood());
                hits.setHitCountNeutral(hitsPerClub.getHitsNeutral());
                hits.setHitCountBad(hitsPerClub.getHitsBad());

                hitsList.add(hits);
            }
        }

        if (null != hitsList && !hitsList.isEmpty()) {
            sendToServer(hitsList);
        }

    }

    private void sendToServer(ArrayList<Hits> hits) {

    }

    private Date extractDateFromFileName (String fileName) {

    }
}
