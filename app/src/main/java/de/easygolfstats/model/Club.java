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

    public ClubType getClubType () {
        return clubType;
    }

    public Integer getClubIndex () {
        return clubIndex;
    }


    @Override
    public int compareTo(Object o) {
        Club other = (Club) o;
        return this.clubIndex.compareTo(other.clubIndex);
    }
}