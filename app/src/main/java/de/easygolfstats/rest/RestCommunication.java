package de.easygolfstats.rest;

import android.content.Context;
import android.telecom.Call;

import androidx.annotation.WorkerThread;

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

import java.util.concurrent.Executors;

import de.easygolfstats.Exception.EgsRestException;
import de.easygolfstats.types.CallbackResult;

public class RestCommunication {
    private static RestCommunication instance;
    private RestCallbackListener restCallbackListener;
    private int requestIdCounter = 0;

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

    @WorkerThread
    public int sendPostLogin(String URL, String userName, String password) throws EgsRestException {
        final int requestId = requestIdCounter++;

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
                .setExecutor(Executors.newSingleThreadExecutor())
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
                                getRestCallbackListener().CallbackLoginResponse(requestId, CallbackResult.OK, result, tokenId, userId, serviceName);
                            }
                        } catch (JSONException e) {
                            getRestCallbackListener().CallbackLoginResponse(requestId, CallbackResult.JSON_EXCEPTION, "ERR", null, null, null);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        getRestCallbackListener().CallbackLoginResponse(requestId, CallbackResult.AN_ERROR, "ERR", null, null, null);
                    }
                });

        return requestId;
    }


    public int sendPostHitsRequest(String URL, String path, String tokenId, JSONArray hits, final String fileName) throws  EgsRestException {
        final int requestId = requestIdCounter++;

        AndroidNetworking.post(URL + "/" + path + "/" + "createHitsCollection/")
                .addJSONArrayBody(hits) // posting json
                .addHeaders("tokenId", tokenId)
                .addHeaders("fileName", fileName)
                .setTag("postHits")
                .setPriority(Priority.MEDIUM)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        getRestCallbackListener().CallbackPostHitsResponse(requestId, CallbackResult.OK, "OK", fileName);
                    }

                    @Override
                    public void onError(ANError error) {
                        getRestCallbackListener().CallbackPostHitsResponse(requestId, CallbackResult.AN_ERROR, "ERR", fileName);
                    }
                });

        return requestId;
    }

    public int sendGetClubRequest(String URL, String path, String tokenId, Long userId) throws EgsRestException {
        final int requestId = requestIdCounter++;

        AndroidNetworking.get(URL + "/" + path + "/" + "listClubs/")
                .addHeaders("tokenId", tokenId)
                .addHeaders("userId", String.valueOf(userId))
                .setTag("getClubs")
                .setPriority(Priority.LOW)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        getRestCallbackListener().CallbackGetClubResponse(requestId, CallbackResult.OK, "OK", response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        getRestCallbackListener().CallbackGetClubResponse(requestId, CallbackResult.AN_ERROR, "ERR", null);
                    }
                });
        return requestId;
    }

    public int sendPingRequest(String URL, String path, String tokenId) throws EgsRestException{
        final int requestId = requestIdCounter++;

        AndroidNetworking.get(URL + "/" + path + "/" + "ping/")
                .addHeaders("tokenId", tokenId)
                .setTag("ping")
                .setPriority(Priority.HIGH)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // PING ist angekommen
                        try {
                            String status = response.getString("status");
                            if (status.equalsIgnoreCase("OK")) {
                                getRestCallbackListener().CallbackPingResponse(requestId, CallbackResult.OK,  status, response.getString("serviceName"),
                                        response.getString("hostName"), response.getString("hostAddress"),
                                        response.getString("port"), LocalDate.parse(response.getString("systime")), response.getString("upTime"));
                            }
                        } catch (JSONException e) {
                            getRestCallbackListener().CallbackPingResponse(requestId, CallbackResult.JSON_EXCEPTION, e.getMessage(), "", "", "", "", null, "");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        getRestCallbackListener().CallbackPingResponse(requestId, CallbackResult.AN_ERROR, anError.getMessage(), "", "", "", "", null, "");
                    }
                });
        return requestId;
    }
}
