package de.easygolfstats.rest;

import android.accounts.NetworkErrorException;
import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.JsonArray;
import com.jacksonandroidnetworking.JacksonParserFactory;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.easygolfstats.file.Settings;
import de.easygolfstats.model.Club;
import de.easygolfstats.model.Hits;
import de.easygolfstats.types.ClubType;

public class RestCommunication {
    private static String URL;
    private static List<Club>clubs = new ArrayList<>();
    private static boolean pingSuccess = true;

    private static RestCommunication instanceOfRestCommunication;

    public static void init (Context context, String basePath) {
        instanceOfRestCommunication = new RestCommunication(context, basePath);
    };

    public static RestCommunication getInstance () {
        return instanceOfRestCommunication;
    }

    private RestCommunication (Context context, String basePath) {
        AndroidNetworking.initialize(context);
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        Settings settings = new Settings(basePath + "/app.properties");
        String protocol = settings.getValue("protocol", "http");
        String address = settings.getValue("address", "84.44.128.8");
        String port = settings.getValue("port", "9090");
        String path = settings.getValue("path", "easy_golf_stats");

        this.URL = protocol + "://" + address + ":" + port + "/" + path + "/";
    }

    public void ping () throws NetworkErrorException {
        sendPingRequest(getTokenId());
        if (!pingSuccess) {
            throw new NetworkErrorException("PING an Easy Golf Stats Service nicht erfolgreich");
        }
    }

    public boolean writeHitsList(Date sessionDate, List<Hits> hits) {
        JSONArray jsonArray = new JSONArray(hits);
        sendPostHitsRequest(jsonArray, getTokenId());
        return false;

    }

    public ArrayList<Club> getClubs () {
        sendGetClubRequest(getUserId(), getTokenId());
        return (ArrayList<Club>) this.clubs;
    }

    private void prepareGetClubResponse(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("clubs");
            if (null != jsonArray) {
                for (int i = 0; i<jsonArray.length();i++) {
                    JSONObject clubObject = (JSONObject)jsonArray.get(i);
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

    private static boolean sendPostHitsRequest(JSONArray hits, String tokenId) {
        AndroidNetworking.post(URL + "createHitsCollection/")
                .addJSONArrayBody(hits) // posting json
                .addHeaders("tokenId", tokenId)
                .setTag("postHits")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // do anything with response
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });
        return false;
    }

    private void sendGetClubRequest (Long userId, String tokenId) {
        AndroidNetworking.get(URL + "listClubs/")
                .addHeaders("tokenId", tokenId)
                .addHeaders("userId", String.valueOf(userId))
                .setTag("getClubs")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        prepareGetClubResponse(response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        System.out.println(anError.getErrorBody());
                    }
                });

    }

    private void sendPingRequest (String tokenId) throws NetworkErrorException {
        pingSuccess = true;

        AndroidNetworking.get(URL + "ping/")
                .addHeaders("tokenId", tokenId)
                .setTag("ping")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // PING ist angekommen
                        try {
                            if (!((String)response.get("status")).equalsIgnoreCase("OK")) {
                                pingSuccess = false;
                            }
                        } catch (JSONException e) {
                            pingSuccess = false;
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        System.out.println(anError.getErrorBody());
                    }
                });
    }

    private String getTokenId() {
        wenn null, anmelden und mit userId holen
    }

    private Long getUserId() {
        wenn null, anmelden und mit tokenId holen
    }
}
