package de.easygolfstats.model;

import de.easygolfstats.types.ClubType;

public class Club {
    private String clubName;
    private ClubType clubType;

    public Club(String clubName, ClubType clubType) {
        this.clubName = clubName;
        this.clubType = clubType;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public ClubType getClubType () {
        return clubType;
    }

    public void setClubType (ClubType clubType) {
        this.clubType = clubType;
    }

}