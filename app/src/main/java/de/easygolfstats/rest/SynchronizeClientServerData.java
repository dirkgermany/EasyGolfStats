package de.easygolfstats.rest;

import android.accounts.NetworkErrorException;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.easygolfstats.Exception.EgsRestException;
import de.easygolfstats.file.HitsPerClubController;
import de.easygolfstats.file.Settings;
import de.easygolfstats.model.Club;
import de.easygolfstats.model.Hits;
import de.easygolfstats.model.HitsPerClub;
import de.easygolfstats.types.ClubType;
import de.easygolfstats.types.HitCategory;

public class SynchronizeClientServerData implements RestCallbackListener {
    private String URL;
    private String path;
    private boolean pingSuccess = true;
    private Long userId;
    private String tokenId;
    private Settings settings;

    // TODO: 20.10.20    Diese Klasse mit Timer versehen, der den Datenabgleich regelmäßig anstößt

    public SynchronizeClientServerData(Context context, String basePath) {
        init(context, basePath);
    }

    public void init(Context context, String basePath) {
        RestCommunication.init(context);
        RestCommunication.getInstance().setRegisterListener(this);

        settings = new Settings(basePath + "/app.properties");
        String protocol = settings.getValue("protocol", "http");
        String address = settings.getValue("address", "84.44.128.8");
        String port = settings.getValue("port", "9090");

        this.path = settings.getValue("path", "easy_golf_stats");
        this.URL = protocol + "://" + address + ":" + port;

    }

    public void login () {
        String userName = settings.getValue("userName", "dirk");
        String password = settings.getValue("password", "");

        try {
            RestCommunication.getInstance().sendPostLogin(this.URL, userName, password);
        } catch (EgsRestException e) {
            e.printStackTrace();
        }
    }

    public void ping() throws EgsRestException {
        RestCommunication.getInstance().sendPingRequest(this.URL, this.path, this.tokenId);
        if (!pingSuccess) {
            throw new EgsRestException("PING an Easy Golf Stats Service nicht erfolgreich");
        }
    }

    public void writeHitsList(LocalDate sessionDate, List<Hits> hits, String fileName) throws EgsRestException{
        JSONArray jsonArray = new JSONArray(hits);
        RestCommunication.getInstance().sendPostHitsRequest(this.URL, this.path, this.tokenId, jsonArray, fileName);
    }

    public ArrayList<Club> getClubs() {
        return null;
//        sendGetClubRequest();
//        return (ArrayList<Club>) this.clubs;
    }


    private void clubRequest () throws EgsRestException{
        try {
            RestCommunication.getInstance().sendGetClubRequest(this.URL, this.path, this.tokenId, this.userId);
        } catch (EgsRestException e) {
            e.printStackTrace();
        }
    }

    private void updateAtServer() {
        List<String> historyFileList = HitsPerClubController.getHistoryFileNames();

        Iterator<String> it = historyFileList.iterator();
        while (it.hasNext()) {
            String fileName = it.next();

            HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap = HitsPerClubController.readHitsFromHistoryFile(fileName);
            if (null != hitMap && !hitMap.isEmpty()) {
                LocalDate sessionDate = HitsPerClubController.extractDateFromArchivedFileName(fileName);
                processMap(sessionDate, hitMap, fileName);
            }
        }
    }

    private void processMap(LocalDate sessionDate, HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap, String fileName) {
        Iterator<Map.Entry<HitCategory, ArrayList<HitsPerClub>>> itMap = hitMap.entrySet().iterator();
        ArrayList<Hits> hitsList = new ArrayList<>();

        while (itMap.hasNext()) {
            Map.Entry<HitCategory, ArrayList<HitsPerClub>> entry = itMap.next();
            HitCategory hitCategory = entry.getKey();
            Iterator<HitsPerClub> itArray = entry.getValue().iterator();
            while (itArray.hasNext()) {
                Hits hits = new Hits();
                HitsPerClub hitsPerClub = itArray.next();

                hits.setUserId(this.userId);
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
            try {
                writeHitsList(sessionDate, hitsList, fileName);
            } catch (EgsRestException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void CallbackGetClubResponse(String result, JSONObject jsonObject) {
        List<Club> clubs = new ArrayList<>();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("clubs");
            if (null != jsonArray) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject clubObject = (JSONObject) jsonArray.get(i);
                    Long _id = clubObject.getLong("_id");
                    String clubName = clubObject.getString("clubName");
                    ClubType clubType = ClubType.valueOf(clubObject.getString("clubType"));
                    Integer clubIndex = clubObject.getInt("clubIndex");
                    Club club = new Club(_id, clubName, clubType, clubIndex);

                    clubs.add(club);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void CallbackLoginResponse(String result, String tokenId, Long userId, String serviceName) {
        this.tokenId = tokenId; this.userId = userId;
    }

    @Override
    public void CallbackPostHitsResponse(String result, String fileName) {
        if (result.equalsIgnoreCase("OK")) {
            HitsPerClubController.deleteHistoryFile(fileName);
        }
    }

    @Override
    public void CallbackPingResponse(String status, String serviceName, String hostName, String hostAddress, String port, LocalDate serverSysDate, String upTime) {

    }
}
