package de.easygolfstats.main;

import de.easygolfstats.log.Logger;
import de.easygolfstats.model.HitsPerClub;

import java.util.ArrayList;

public class GolfStatsManager {
    private int lastRefRouteId = -1;
    private int activeRefRouteId = 0;
    private boolean isWorking = false;
    private ArrayList<HitsPerClub> hitsPerClubList;

    private Logger logger = Logger.createLogger("GolfStatsManager");


    public GolfStatsManager(ArrayList<HitsPerClub> hitsPerClubList) {
        this.hitsPerClubList = hitsPerClubList;
    }

    public void reset() {
        lastRefRouteId = -1;
        activeRefRouteId = 0;
        isWorking = false;
    }


    public boolean isWorking() {
        return isWorking;
    }

    public void resetWorkingSwitch() {
        isWorking = true;
    }

    public boolean nextRefRouteExists() {
        while (activeRefRouteId < hitsPerClubList.size()) {
                return true;
        }
        return false;
    }

    public int getNextRefRouteId() {
        while (activeRefRouteId < hitsPerClubList.size()) {
            activeRefRouteId++;
        }
        return activeRefRouteId;
    }

    public int getActiveRefRouteId() {
        return activeRefRouteId;
    }

    /**
     * Start routing a reference route
     */
    public void routeItem(String packageName, String className, String routesPath, Integer routeId, boolean restartTour) {
    }

    public void showApp(String packageName, String className) {
    }

    public void showMessageButton () {
    }

    public void interruptRouting() {
    }
}
