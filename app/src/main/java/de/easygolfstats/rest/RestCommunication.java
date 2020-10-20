package de.easygolfstats.rest;

import android.accounts.NetworkErrorException;
import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.jacksonandroidnetworking.JacksonParserFactory;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import de.easygolfstats.file.Settings;
import de.easygolfstats.model.Club;
import de.easygolfstats.model.Hits;
import de.easygolfstats.types.ClubType;

public class RestCommunication {
    private static RestCommunication instance;
    private String URL;
    private String path;
    private List<Club> clubs = new ArrayList<>();
    private boolean pingSuccess = true;
    private Long userId;
    private String tokenId;
    private Settings settings;

    private RestCommunication(Context context, String basePath) {
        AndroidNetworking.initialize(context);
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        settings = new Settings(basePath + "/app.properties");
        String protocol = settings.getValue("protocol", "http");
        String address = settings.getValue("address", "84.44.128.8");
        String port = settings.getValue("port", "9090");

        this.path = settings.getValue("path", "easy_golf_stats");
        this.URL = protocol + "://" + address + ":" + port;
    }

    public static void init(Context context, String basePath) {
        instance = new RestCommunication(context, basePath);
    }

    public static RestCommunication getInstance() {
        return instance;
    }

    private void login() {
        String userName = settings.getValue("userName", "dirk");
        String password = settings.getValue("password", "");

        if (null == userName || userName.isEmpty() || null == password || password.isEmpty()) {
            return;
        }

        JSONObject bla = new JSONObject();
        try {
            bla.put("userName", userName);
            bla.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(URL + "/" + "login/")
                .addApplicationJsonBody(bla)
                .addJSONObjectBody(bla)
                .setContentType("application/json")
                .setTag("login")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Login ist angekommen
                        try {
                            if (((String) response.get("result")).equalsIgnoreCase("OK")) {
                                userId = new Long((Integer) response.get("userId"));
                                tokenId = (String) response.get("tokenId");
                            }
                        } catch (JSONException e) {
                            userId = null;
                            tokenId = null;
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        System.out.println(anError.getErrorBody());
                    }
                });

    }

    public void ping() throws NetworkErrorException {
        sendPingRequest();
        if (!pingSuccess) {
            throw new NetworkErrorException("PING an Easy Golf Stats Service nicht erfolgreich");
        }
    }

    public boolean writeHitsList(LocalDate sessionDate, List<Hits> hits) {
        JSONArray jsonArray = new JSONArray(hits);
        sendPostHitsRequest(jsonArray);
        return false;

    }

    public ArrayList<Club> getClubs() {
        login();
        return null;
//        sendGetClubRequest();
//        return (ArrayList<Club>) this.clubs;
    }

    private void prepareGetClubResponse(JSONObject jsonObject) {
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

    private boolean sendPostHitsRequest(JSONArray hits) {
        AndroidNetworking.post(URL + "/" + path + "/" + "createHitsCollection/")
                .addJSONArrayBody(hits) // posting json
                .addHeaders("tokenId", this.tokenId)
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

    private void sendGetClubRequest() {
        AndroidNetworking.get(URL + "/" + path + "/" + "listClubs/")
                .addHeaders("tokenId", this.tokenId)
                .addHeaders("userId", String.valueOf(this.userId))
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

    private void sendPingRequest() throws NetworkErrorException {
        pingSuccess = true;

        AndroidNetworking.get(URL + "/" + path + "/" + "ping/")
                .addHeaders("tokenId", this.tokenId)
                .setTag("ping")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // PING ist angekommen
                        try {
                            if (((String) response.get("status")).equalsIgnoreCase("OK")) {
                                pingSuccess = true;
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

    public Long getUserId() {
        return userId;
    }
}
