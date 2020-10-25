package de.easygolfstats.model;

public class HitsPerClub implements Comparable{
    private Club club;
    private int hitsGood;
    private int hitsNeutral;
    private int hitsBad;

    public HitsPerClub(Club club, int hitsGood, int hitsNeutral, int hitsBad) {
        this.club = club;
        this.hitsGood = hitsGood;
        this.hitsNeutral = hitsNeutral;
        this.hitsBad = hitsBad;
    }

    public void setHitsGood(int hitsGood) {
        this.hitsGood = hitsGood;
    }

    public void incrementHitsGood(int incrementVal) {
        this.hitsGood+=incrementVal;
    }

    public void setHitsNeutral(int hitsNeutral) {
        this.hitsNeutral = hitsNeutral;
    }

    public void incrementHitsNeutral(int incrementVal) {
        this.hitsNeutral+= incrementVal;
    }

    public void setHitsBad(int hitsBad) {
        this.hitsBad = hitsBad;
    }

    public void incrementHitsBad (int incrementVal) {
        this.hitsBad+=incrementVal;
    }

    public Club getClub() {
        return club;
    }

    public String getClubName() {
        return club.getClubName();
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

    public int getHitsOverAll() {
        return hitsGood + hitsBad + hitsNeutral;
    }

    @Override
    public int compareTo(Object o) {
        HitsPerClub other = (HitsPerClub) o;
        return this.club.compareTo(other.club);
    }
}