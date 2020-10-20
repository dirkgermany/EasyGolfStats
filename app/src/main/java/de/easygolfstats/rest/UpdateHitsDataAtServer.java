package de.easygolfstats.rest;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.easygolfstats.file.HitsPerClubController;
import de.easygolfstats.model.Hits;
import de.easygolfstats.model.HitsPerClub;
import de.easygolfstats.types.HitCategory;

public class UpdateHitsDataAtServer {

    // TODO: 20.10.20    Diese Klasse mit Timer versehen, der den Datenabgleich regelmäßig anstößt

    private void updateAtServer() {
        List<String> historyFileList = HitsPerClubController.getHistoryFileNames();

        Iterator<String> it = historyFileList.iterator();
        while (it.hasNext()) {
            String fileName = it.next();

            HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap = HitsPerClubController.readHitsFromHistoryFile(fileName);
            if (null != hitMap && !hitMap.isEmpty()) {
                LocalDate sessionDate = HitsPerClubController.extractDateFromArchivedFileName(fileName);
                processMap(sessionDate, hitMap);
            }


            HitsPerClubController.deleteHistoryFile(fileName);
        }


    }

    private void processMap(LocalDate sessionDate, HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap) {
        Iterator<Map.Entry<HitCategory, ArrayList<HitsPerClub>>> itMap = hitMap.entrySet().iterator();
        ArrayList<Hits> hitsList = new ArrayList<>();

        while (itMap.hasNext()) {
            Map.Entry<HitCategory, ArrayList<HitsPerClub>> entry = itMap.next();
            HitCategory hitCategory = entry.getKey();
            Iterator<HitsPerClub> itArray = entry.getValue().iterator();
            while (itArray.hasNext()) {
                Hits hits = new Hits();
                HitsPerClub hitsPerClub = itArray.next();

                hits.setUserId(RestCommunication.getInstance().getUserId());
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

}
