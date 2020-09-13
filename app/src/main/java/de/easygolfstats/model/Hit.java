package de.easygolfstats.model;

import de.easygolfstats.types.HitQuality;

public class Hit {
    private String clubName;
    private HitQuality hitQuality;

    public Hit(String clubName, HitQuality hitQuality) {
        this.clubName = clubName;
        this.hitQuality = hitQuality;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public HitQuality getHitQuality() {
        return hitQuality;
    }

    public void setHitQuality(HitQuality hitQuality) {
        this.hitQuality = hitQuality;
    }
}