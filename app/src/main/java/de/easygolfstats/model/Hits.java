package de.easygolfstats.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;

import de.easygolfstats.types.ClubType;
import de.easygolfstats.types.HitCategory;

public class Hits {
    private Long _id;

    private Long userId;
    private LocalDateTime sessionDate;
    private HitCategory hitCategory;
    private ClubType clubType;
    private Integer hitCountGood;
    private Integer hitCountNeutral;
    private Integer hitCountBad;


    public Hits() {
    }

    public Hits(Long userId, LocalDateTime sessionDate, HitCategory hitCategory, ClubType clubType, Integer hitCountGood, Integer hitCountNeutral, Integer hitCountBad) {
        this.userId = userId;
        this.sessionDate = sessionDate;
        this.hitCategory = hitCategory;
        this.clubType = clubType;
        this.hitCountGood = hitCountGood;
        this.hitCountNeutral = hitCountNeutral;
        this.hitCountBad = hitCountBad;
    }

    public Long get_id() {
        return _id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public HitCategory getHitCategory() {
        return this.hitCategory;
    }

    public void setHitCategory(HitCategory hitCategory) {
        this.hitCategory = hitCategory;
    }

    public ClubType getClubType() {
        return this.clubType;
    }

    public void setClubType(ClubType clubType) {
        this.clubType = clubType;
    }

    public LocalDateTime getSessionDate() {
        return this.sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Integer getHitCountGood() {
        return this.hitCountGood;
    }

    public void setHitCountGood(Integer hitCountGood) {
        this.hitCountGood = hitCountGood;
    }

    public Integer getHitCountNeutral() {
        return this.hitCountNeutral;
    }

    public void setHitCountNeutral(Integer hitCountNeutral) {
        this.hitCountNeutral = hitCountNeutral;
    }

    public Integer getHitCountBad() {
        return this.hitCountBad;
    }

    public void setHitCountBad(Integer hitCountBad) {
        this.hitCountBad = hitCountBad;
    }

    public boolean clubUsed() {
        return getHitCountGood() != 0 || getHitCountNeutral() != 0 || getHitCountBad() != 0;
    }

    public JSONObject getAsJsonObject() {
        JSONObject jObject = new JSONObject();

        try {
            jObject.put("userId", this.userId);
            jObject.put("sessionDateTime", this.sessionDate);
            jObject.put("hitCategory", this.hitCategory);
            jObject.put("clubType", this.clubType);
            jObject.put("hitCountGood", this.hitCountGood);
            jObject.put("hitCountNeutral", this.hitCountNeutral);
            jObject.put("hitCountBad", this.hitCountBad);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject;
    }
}
