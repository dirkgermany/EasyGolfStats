package de.easygolfstats.model;

import de.easygolfstats.types.ClubType;

public class Club implements  Comparable{
    private Long _id;
    private Long userId;
    private String clubName;
    private ClubType clubType;
    private Integer clubIndex;

    public Club(Long userId, String clubName, ClubType clubType, Integer clubIndex) {
        this.userId = userId;
        this.clubName = clubName;
        this.clubType = clubType;
        this.clubIndex = clubIndex;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getId() {
        return _id;
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