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

import de.easygolfstats.Exception.EgsRestException;
import de.easygolfstats.file.Settings;
import de.easygolfstats.model.Club;
import de.easygolfstats.model.Hits;

public class RestCommunication {
    private static RestCommunication instance;
    private RestCallbackListener restCallbackListener;

    private RestCommunication(Context context) {
        AndroidNetworking.initialize(context);
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

    }

    public static void init(Context context) {
        instance = new RestCommunication(context);
    }

    public static RestCommunication getInstance() {
        return instance;
    }

    public void setRegisterListener (RestCallbackListener restCallbackListener) {
        getInstance().restCallbackListener = restCallbackListener;
    }

    private RestCallbackListener getRestCallbackListener () {
        return this.restCallbackListener;
    }

    public void sendPostLogin(String URL, String userName, String password) throws EgsRestException {
        if (null == userName || userName.isEmpty() || null == password || password.isEmpty()) {
            throw new EgsRestException("userName or password is empty");
        }

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("userName", userName);
            loginData.put("password", password);
        } catch (JSONException e) {
            throw new EgsRestException(e.getMessage());
        }

        AndroidNetworking.post(URL + "/" + "login/")
                .addJSONObjectBody(loginData)
                .setContentType("application/json")
                .setTag("login")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Login ist angekommen
                        try {
                            String result = (String) response.get("result");
                            if (result.equalsIgnoreCase("OK")) {
                                Long userId = new Long((Integer) response.get("userId"));
                                String tokenId = (String) response.get("tokenId");
                                String serviceName = (String)response.get("serviceName");
                                getRestCallbackListener().CallbackLoginResponse(result, tokenId, userId, serviceName);
                            }
                        } catch (JSONException e) {
                            getRestCallbackListener().CallbackLoginResponse("ERR", null, null, null);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        getRestCallbackListener().CallbackLoginResponse("ERR", null, null, null);
                        System.out.println(anError.getErrorBody());
                    }
                });
    }


    public void sendPostHitsRequest(String URL, String path, String tokenId, JSONArray hits, final String fileName) {
        AndroidNetworking.post(URL + "/" + path + "/" + "createHitsCollection/")
                .addJSONArrayBody(hits) // posting json
                .addHeaders("tokenId", tokenId)
                .addHeaders("fileName", fileName)
                .setTag("postHits")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        getRestCallbackListener().CallbackPostHitsResponse("OK", fileName);
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });
    }

    public void sendGetClubRequest(String URL, String path, String tokenId, Long userId) throws EgsRestException {
        AndroidNetworking.get(URL + "/" + path + "/" + "listClubs/")
                .addHeaders("tokenId", tokenId)
                .addHeaders("userId", String.valueOf(userId))
                .setTag("getClubs")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        getRestCallbackListener().CallbackGetClubResponse("OK", response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        getRestCallbackListener().CallbackGetClubResponse("ERR", null);
                    }
                });
    }

    public void sendPingRequest(String URL, String path, String tokenId) {
        AndroidNetworking.get(URL + "/" + path + "/" + "ping/")
                .addHeaders("tokenId", tokenId)
                .setTag("ping")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // PING ist angekommen
                        try {
                            String status = response.getString("status");
                            if (status.equalsIgnoreCase("OK")) {
                                getRestCallbackListener().CallbackPingResponse(status, response.getString("serviceName"),
                                        response.getString("hostName"), response.getString("hostAddress"),
                                        response.getString("port"), LocalDate.parse(response.getString("systime")), response.getString("upTime"));
                            }
                        } catch (JSONException e) {
                            getRestCallbackListener().CallbackPingResponse(e.getMessage(), "", "", "", "", null, "");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        getRestCallbackListener().CallbackPingResponse(anError.getMessage(), "", "", "", "", null, "");
                    }
                });
    }
}
