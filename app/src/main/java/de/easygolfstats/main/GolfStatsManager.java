package de.easygolfstats.main;

import de.easygolfstats.log.Logger;
import de.easygolfstats.model.Club;

import java.util.ArrayList;

public class GolfStatsManager {
    private int lastRefRouteId = -1;
    private int activeRefRouteId = 0;
    private boolean isWorking = false;
    private ArrayList<Club> clubs;

    private Logger logger = Logger.createLogger("RefRouteManager");


    public GolfStatsManager(ArrayList<Club> clubs) {
        this.clubs = clubs;
    }

    public void reset() {
        lastRefRouteId = -1;
        activeRefRouteId = 0;
        isWorking = false;
    }

    // =========================================================
    //      Routing status
    // =========================================================

    public boolean isWorking() {
        return isWorking;
    }

    public void resetWorkingSwitch() {
        isWorking = true;
    }

    // =========================================================
    //      Route handling
    // =========================================================

    public int getLastRefRouteId() {
        return lastRefRouteId;
    }

    public boolean nextRefRouteExists() {
        while (activeRefRouteId < clubs.size()) {
                return true;
        }
        return false;
    }

    public int getNextRefRouteId() {
        while (activeRefRouteId < clubs.size()) {
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
