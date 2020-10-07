package de.easygolfstats.model;

import de.easygolfstats.types.ClubType;

public class Club implements  Comparable{
    private String clubName;
    private ClubType clubType;
    private Integer clubIndex;

    public Club(String clubName, ClubType clubType, Integer clubIndex) {
        this.clubName = clubName;
        this.clubType = clubType;
        this.clubIndex = clubIndex;
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

    public Integer getClubIndex () {
        return clubIndex;
    }

    public void setClubIndex (Integer clubIndex) {
        this.clubIndex = clubIndex;
    }


    @Override
    public int compareTo(Object o) {
        Club other = (Club) o;
        return this.clubIndex.compareTo(other.clubIndex);
    }
}