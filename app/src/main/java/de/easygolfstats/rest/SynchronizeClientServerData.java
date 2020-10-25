package de.easygolfstats.rest;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;

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
import de.easygolfstats.types.CallbackResult;
import de.easygolfstats.types.ClubType;
import de.easygolfstats.types.HitCategory;

public class SynchronizeClientServerData implements RestCallbackListener {
    private String URL;
    private String path;
    private boolean pingSuccess = true;
    private Long userId;
    private String tokenId;
    private Settings settings;
    ArrayList<Club> clubs = null;


    // TODO: 20.10.20    Diese Klasse mit Timer versehen, der den Datenabgleich regelmäßig anstößt

    public SynchronizeClientServerData(Context context, String basePath) {
        init(context, basePath);
    }

    public void init(Context context, String basePath) {
        RestCommunication.init(context);

        settings = new Settings(basePath + "/app.properties");
        String protocol = settings.getValue("protocol", "http");
        String address = settings.getValue("address", "84.44.128.8");
        String port = settings.getValue("port", "9090");

        this.path = settings.getValue("path", "easy_golf_stats");
        this.URL = protocol + "://" + address + ":" + port;

    }

    private String getTokenId() {
        if (null == this.tokenId || this.tokenId.isEmpty()) {
            this.tokenId = settings.getValue("tokenId", "");
        }
        return this.tokenId;
    }

    private void setTokenId(String tokenId) {
        settings.setValue("tokenId", tokenId);
        this.tokenId = tokenId;
    }

    private Long getUserId() {
        if (null == this.userId) {
            String userId = settings.getValue("userId", "");
            if (null != userId && !userId.isEmpty()) {
                this.userId = Long.parseLong(userId);
                return this.userId;
            }
        }
        return null;
    }

    private void setUserId(Long userId) {
        settings.setValue("userId", String.valueOf(userId));
    }

    private CallbackResult establishConnection() {
        if (!ping().equals(CallbackResult.OK)) {
            return login();
        }
        return CallbackResult.OK;
    }

    private CallbackResult login() {
        final String userName = settings.getValue("userName", "dirk");
        final String password = settings.getValue("password", "");

        try {
            int requestId = RestCommunication.getInstance().sendPostLogin(this, this.URL, userName, password);
            return CallbackSynchronizer.wait(requestId, "LOGIN", 5000L);
        } catch (EgsRestException e) {
            e.printStackTrace();
        }

        return CallbackResult.ERR_UNKNOWN;
    }

    private CallbackResult ping() {
        try {
            int requestId = RestCommunication.getInstance().sendPingRequest(this, this.URL, this.path, getTokenId());
            return CallbackSynchronizer.wait(requestId, "PING", 5000L);
        } catch (EgsRestException e) {
            e.printStackTrace();
        }
        return CallbackResult.ERR_UNKNOWN;
    }

    public CallbackResult writeHitsList(LocalDateTime sessionDate, List<Hits> hits, String fileName) {
        CallbackResult connectionEstablished = establishConnection();
        if (!CallbackResult.OK.equals(connectionEstablished)) {
            return connectionEstablished;
        }

        JSONArray jsonArray = new JSONArray();
        Iterator<Hits> it = hits.iterator();
        while (it.hasNext()) {
            Hits hitEntry = it.next();
            if (hitEntry.clubUsed()) {
                JSONObject jsonObject = hitEntry.getAsJsonObject();
                jsonArray.put(jsonObject);
            }
        }
        if (jsonArray.length() != 0) {
            // hits in file, some values <> 0
            try {
                int requestId = RestCommunication.getInstance().sendPostHitsRequest(this, this.URL, this.path, getTokenId(), jsonArray, fileName);
                CallbackResult waitResult = CallbackSynchronizer.wait(requestId, "WRITE_HITS_LIST", 10000L);
                switch (waitResult) {
                    case OK:
                    case CREATE_DUPLICATE_KEY:
                        break;

                    default:
                        return waitResult;
                }

            } catch (EgsRestException e) {
                return CallbackResult.REST_EXCEPTION;
            }
        }

        HitsPerClubController.deleteHistoryFile(fileName);
        return CallbackResult.OK;
    }

    public ArrayList<Club> getClubs() {
        CallbackResult connectionEstablished = establishConnection();
        if (!CallbackResult.OK.equals(connectionEstablished)) {
            return null;
        }

        this.clubs = new ArrayList<>();
        try {
            int requestId = RestCommunication.getInstance().sendGetClubRequest(this, this.URL, this.path, getTokenId(), getUserId());
            CallbackResult waitResult = CallbackSynchronizer.wait(requestId, "GET_CLUBS", 10000L);
            if (!CallbackResult.OK.equals(waitResult)) {
                return null;
            }
        } catch (EgsRestException e) {
            e.printStackTrace();
        }

        return this.clubs;
    }

    public boolean updateAtServer() {
        List<String> historyFileList = HitsPerClubController.getHistoryFileNames();

        Iterator<String> it = historyFileList.iterator();
        while (it.hasNext()) {
            String fileName = it.next();

            HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap = HitsPerClubController.readHitsFromHistoryFile(fileName);
            if (null != hitMap && !hitMap.isEmpty()) {
                LocalDateTime sessionDate = HitsPerClubController.extractDateFromArchivedFileName(fileName);
                if (!processMap(sessionDate, hitMap, fileName)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean processMap(LocalDateTime sessionDate, HashMap<HitCategory, ArrayList<HitsPerClub>> hitMap, String fileName) {
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
            if (CallbackResult.OK.equals(writeHitsList(sessionDate, hitsList, fileName))) {
                return true;
            }
            return false;
        }
        return true;
    }


    @Override
    public void CallbackGetClubResponse(int requestId, CallbackResult callbackResult, String result, JSONObject jsonObject) {

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

                    this.clubs.add(club);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CallbackSynchronizer.callBackCalled(requestId, callbackResult, "GetClubs");
    }

    @Override
    public void CallbackLoginResponse(int requestId, CallbackResult callbackResult, String result, String tokenId, Long userId, String serviceName) {
        setTokenId(tokenId);
        setUserId(userId);
        CallbackSynchronizer.callBackCalled(requestId, callbackResult, "LoginResponse");
    }

    @Override
    public void CallbackPostHitsResponse(int requestId, CallbackResult callbackResult, String result, String fileName) {
        CallbackSynchronizer.callBackCalled(requestId, callbackResult, "PostHits");
    }

    @Override
    public void CallbackPingResponse(int requestId, CallbackResult callbackResult, String status, String serviceName, String hostName, String hostAddress, String port, LocalDateTime serverSysDateTime, String upTime) {
        CallbackSynchronizer.callBackCalled(requestId, callbackResult, "Ping");
    }
}
