package de.easygolfstats.rest;

import org.json.JSONObject;
import org.threeten.bp.LocalDate;

import de.easygolfstats.types.CallbackResult;

public interface RestCallbackListener {

    /**
     * Called when the getClubRequest returns
     * @param requestId Identifier to get relation between request and callback
     * @param jsonObject Contains a JSONArray with GolfClubs
     */
    public void CallbackGetClubResponse(int requestId, CallbackResult callbackResult, String result, JSONObject jsonObject);

    /**
     * Called when the postLoginRequest returns
     * @param requestId Identifier to get relation between request and callback
     * @param result Request result - OK or error text
     * @param tokenId Token
     * @param userId UserId
     */
    public void CallbackLoginResponse(int requestId, CallbackResult callbackResult, String result, String tokenId, Long userId, String serviceName);

    /**
     * Called when the postHitsRequest returns
     * @param requestId Identifier to get relation between request and callback
     * @param result Request result - OK or error text
     * @param fileName FileName was send with request to identify the file which contained the hits
     */
    public void CallbackPostHitsResponse(int requestId, CallbackResult callbackResult, String result, String fileName);

    /**
     * Called when the getPingRequest returns
     * @param requestId Identifier to get relation between request and callback
     * @param status Request status - OK or error text
     * @param hostName Host where Service is running
     * @param serviceName Name of the service which processes the request
     * @param hostAddress Server address
     * @param port Port that the server listens
     * @param serverSysDate DateTime (LocalDate) of server
     * @param upTime Serer runtime in millis
     */
    public void CallbackPingResponse(int requestId, CallbackResult callbackResult, String status, String serviceName, String hostName, String hostAddress, String port, LocalDate serverSysDate, String upTime);
}
