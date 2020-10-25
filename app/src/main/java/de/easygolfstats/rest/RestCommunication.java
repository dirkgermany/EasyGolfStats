package de.easygolfstats.rest;

import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;

import java.util.concurrent.Executors;

import de.easygolfstats.Exception.EgsRestException;
import de.easygolfstats.types.CallbackResult;

public class RestCommunication {
    private static RestCommunication instance;
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

    public void forceCancelRequests() {
        AndroidNetworking.forceCancelAll();
    }

    public int sendPostLogin(RestCallbackListener callbackListener, String URL, String userName, String password) throws EgsRestException {
        final int requestId = requestIdCounter++;
        final RestCallbackListener caller = callbackListener;

        if (null == userName || userName.isEmpty() || null == password || password.isEmpty()) {
            callbackListener.CallbackLoginResponse(requestId, CallbackResult.MISSED_VALUE, "ERR", null, null, null);
            return requestId;
        }

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("userName", userName);
            loginData.put("password", password);
        } catch (JSONException e) {
            callbackListener.CallbackLoginResponse(requestId, CallbackResult.JSON_EXCEPTION, "ERR", null, null, null);
            return requestId;
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
                                caller.CallbackLoginResponse(requestId, CallbackResult.OK, result, tokenId, userId, serviceName);
                            }
                        } catch (JSONException e) {
                            caller.CallbackLoginResponse(requestId, CallbackResult.JSON_EXCEPTION, "ERR", null, null, null);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        caller.CallbackLoginResponse(requestId, CallbackResult.AN_ERROR, "ERR", null, null, null);
                    }
                });

        return requestId;
    }


    public int sendPostHitsRequest(RestCallbackListener callbackListener, String URL, String path, String tokenId, JSONArray hits, final String fileName) throws  EgsRestException {
        final int requestId = requestIdCounter++;
        final RestCallbackListener caller = callbackListener;

        AndroidNetworking.post(URL + "/" + path + "/" + "createHitsCollection/")
                .addJSONArrayBody(hits) // posting json
                .addHeaders("tokenId", tokenId)
                .addHeaders("fileName", fileName)
                .setTag("postHits")
                .setPriority(Priority.MEDIUM)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String result = (String) response.get("result");
                            caller.CallbackPostHitsResponse(requestId, CallbackResult.valueOf(result), result, fileName);
                        } catch (JSONException e) {
                            caller.CallbackPostHitsResponse(requestId, CallbackResult.JSON_EXCEPTION, "ERR", fileName);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        caller.CallbackPostHitsResponse(requestId, CallbackResult.AN_ERROR, "ERR", fileName);
                    }
                });

        return requestId;
    }

    public int sendGetClubRequest(RestCallbackListener callbackListener, String URL, String path, String tokenId, Long userId) throws EgsRestException {
        final int requestId = requestIdCounter++;
        final RestCallbackListener caller = callbackListener;

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
                        caller.CallbackGetClubResponse(requestId, CallbackResult.OK, "OK", response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        caller.CallbackGetClubResponse(requestId, CallbackResult.AN_ERROR, "ERR", null);
                    }
                });
        return requestId;
    }

    public int sendPingRequest(final RestCallbackListener callbackListener, String URL, String path, String tokenId) throws EgsRestException{
        final int requestId = requestIdCounter++;
        final RestCallbackListener caller = callbackListener;

        if (null == tokenId || tokenId.isEmpty()) {
            callbackListener.CallbackPingResponse(requestId, CallbackResult.MISSED_VALUE, "ERR", null, null, null, null, null, null);
            return requestId;
        }

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

                                caller.CallbackPingResponse(requestId, CallbackResult.OK,  status, responseGetStringSafe(response, "serviceName"),
                                        response.getString("hostName"), response.getString("hostAddress"),
                                        response.getString("port"), LocalDateTime.parse(response.getString("systime")), response.getString("uptime"));
                            }
                        } catch (JSONException e) {
                            callbackListener.CallbackPingResponse(requestId, CallbackResult.JSON_EXCEPTION, e.getMessage(), "", "", "", "", null, "");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        caller.CallbackPingResponse(requestId, CallbackResult.AN_ERROR, anError.getMessage(), "", "", "", "", null, "");
                    }
                });
        return requestId;
    }

    private String responseGetStringSafe(JSONObject jsonObject, String fieldName) {
        try {
            return jsonObject.getString(fieldName);
        } catch (JSONException e) {
            return "";
        }
    }
}
