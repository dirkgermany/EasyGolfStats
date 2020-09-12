package de.easygolfstats.main;

import android.content.Context;
import de.easygolfstats.log.Logger;
import de.easygolfstats.model.RefRoute;

import java.util.ArrayList;

public class RefRouteManager {
    private int lastRefRouteId = -1;
    private int activeRefRouteId = 0;
    private boolean isWorking = false;
    private ArrayList<RefRoute> refRoutes;
    private int startReferenceId = -1;

    private Logger logger = Logger.createLogger("RefRouteManager");


    public RefRouteManager(ArrayList<RefRoute> refRoutes) {
        this.refRoutes = refRoutes;
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
        while (activeRefRouteId < refRoutes.size()) {
            if (refRoutes.get(activeRefRouteId).isActive()) {
                return true;
            }
            ++activeRefRouteId;
        }
        return false;
    }

    public int getNextRefRouteId() {
        while (activeRefRouteId < refRoutes.size()) {
            if (refRoutes.get(activeRefRouteId).isActive()) {
                break;
            }
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

    public void initMti(Context context) {
    }

    public void findServer() {
    }

    public void showApp(String packageName, String className) {
    }

    public void showMessageButton () {
    }

    public void interruptRouting() {
    }
}
