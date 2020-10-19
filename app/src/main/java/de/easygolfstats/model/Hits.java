package de.easygolfstats.model;

import java.util.Date;

import de.easygolfstats.types.ClubType;
import de.easygolfstats.types.HitCategory;

public class Hits {
    private Long _id;

    private Long userId;
    private Date sessionDate;
    private HitCategory hitCategory;
    private ClubType clubType;
    private Integer hitCountGood;
    private Integer hitCountNeutral;
    private Integer hitCountBad;


    public Hits() {
    }

    public Hits(Long userId, Date sessionDate, HitCategory hitCategory, ClubType clubType, Integer hitCountGood, Integer hitCountNeutral, Integer hitCountBad) {
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

    public HitCategory getHitCategory () {
        return this.hitCategory;
    }

    public void setHitCategory (HitCategory hitCategory) {
        this.hitCategory = hitCategory;
    }

    public ClubType getClubType () {
        return this.clubType;
    }

    public void setClubType (ClubType clubType) {
        this.clubType = clubType;
    }

    public Date getSessionDate () {
        return this.sessionDate;
    }

    public void setSessionDate (Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Integer getHitCountGood () {
        return this.hitCountGood;
    }

    public void setHitCountGood (Integer hitCountGood) {
        this.hitCountGood = hitCountGood;
    }

    public Integer getHitCountNeutral () {
        return this.hitCountNeutral;
    }

    public void setHitCountNeutral (Integer hitCountNeutral) {
        this.hitCountNeutral = hitCountNeutral;
    }

    public Integer getHitCountBad () {
        return this.hitCountBad;
    }

    public void setHitCountBad (Integer hitCountBad) {
        this.hitCountBad = hitCountBad;
    }

}
