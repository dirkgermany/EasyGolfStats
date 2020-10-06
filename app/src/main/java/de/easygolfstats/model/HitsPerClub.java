package de.easygolfstats.model;

import de.easygolfstats.types.HitQuality;

public class HitsPerClub {
    private String clubName;
    private int hitsGood;
    private int hitsNeutral;
    private int hitsBad;

    public HitsPerClub(String clubName, int hitsGood, int hitsNeutral, int hitsBad) {
        this.clubName = clubName;
        this.hitsGood = hitsGood;
        this.hitsNeutral = hitsNeutral;
        this.hitsBad = hitsBad;
    }

    public void setHitsGood(int hitsGood) {
        this.hitsGood = hitsGood;
    }

    public void setHitsNeutral(int hitsNeutral) {
        this.hitsNeutral = hitsNeutral;
    }

    public void setHitsBad(int hitsBad) {
        this.hitsBad = hitsBad;
    }

    public String getClubName() {
        return clubName;
    }

    public int getHitsGood() {
        return hitsGood;
    }

    public int getHitsNeutral() {
        return hitsNeutral;
    }

    public int getHitsBad() {
        return hitsBad;
    }

    public int getHitsPositiveCalculated() {
        return hitsGood - hitsBad;
    }
}