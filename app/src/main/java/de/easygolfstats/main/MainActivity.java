package de.easygolfstats.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.easygolfstats.R;
import de.easygolfstats.file.BagController;
import de.easygolfstats.file.HitsPerClubController;
import de.easygolfstats.file.Settings;
import de.easygolfstats.itemList.HitsPerClubAdapter;
import de.easygolfstats.log.Logger;
import de.easygolfstats.model.Club;
import de.easygolfstats.model.HitsPerClub;
import de.easygolfstats.types.ClubType;
import de.easygolfstats.types.HitCategory;

public class MainActivity extends AppCompatActivity implements ClubDialog.RefRouteDialogListener, HitsPerClubAdapter.ItemClickListener {
    private static final int CLUB_DIALOG_MODE_ADD = 1;
    private static final int CLUB_DIALOG_MODE_EDIT = 2;
    private boolean dialogIsActive = false;

    private static final String MARK_COLOR_ROUTE_DONE = "#9CB548";
    private static final String MARK_COLOR_NOT_ACTIVE = "#E6EAED";
    private static final String MARK_COLOR_NOT_DONE = "#FFFFFF";
    private static final String MARK_COLOR_IN_WORK = "#BD1550";
    private static final String MARK_COLOR_PAUSED = "#E97F02";

    private static MainActivity mainActivity;
    private static Thread serverThread;

    private ArrayList<HitsPerClub> hitsPerClubList;
    private RecyclerView rvHitsPerClub;

    private String fileDirectory;
    private boolean isRoundActive = false;
    private boolean isPaused = false;
    private boolean pauseButtonWasClicked = false;

    private GolfStatsManager golfStatsManager;
    private Logger logger;

    // ======================================================================================================
    // GUI control - reacting to user actions around route selection, adding and so on
    // ======================================================================================================

    /**
     * React to GO Button click
     */
    public void startOrStopRouting(View view) {
        logger.finest("startOrStopRouting", "Main control button was clicked");

        if (isRoundActive && !isPaused) {
            // Make a PAUSE
            buttonPauseClicked();
        } else {
            // START routing
            buttonGoClicked();
        }
    }


    @Override
    public void clubDialogCancel() {
        dialogIsActive = false;
    }

    /**
     * Callback when RefRouteDialog is finished with OK
     *
     * @param clubName        Name edited by user
     * @param clubType Description edited by user
     * @param listIndex           Is equal to or greater than 0 if dialogMode is REFROUTE_DIALOG_MODE_EDIT;
     *                            is NULL or lower than 0 if dialogMode is REFROUTE_DIALOG_MODE_ADD.
     * @param dialogMode          Distinguishes between EDIT or ADD mode.
     */
    @Override
    public void clubDialogOk(String clubName, ClubType clubType, Integer listIndex, int dialogMode) {
        // aware that listIndex can be -1 or null if dialogMode is to add - so ignore listIndex when adding an item

        switch (dialogMode) {
            case CLUB_DIALOG_MODE_ADD:
                int newListIndex = hitsPerClubList.size();
                Club newClub = new Club(clubName, clubType, listIndex);
                HitsPerClub hitsPerClub = new HitsPerClub( newClub,  0,  0,  0);
//                hitsPerClubList.add(newListIndex, newClub);
                rvHitsPerClub.getAdapter().notifyItemInserted(newListIndex);
                rvHitsPerClub.getAdapter().notifyItemRangeChanged(listIndex, hitsPerClubList.size());
//                BagController.writeBagToFile(fileDirectory, hitsPerClub);

                activateGoButton(hitsPerClubList.size() > 0);
                break;

            case CLUB_DIALOG_MODE_EDIT:
                if (null == listIndex || 0 > listIndex) {
                    Toast.makeText(this, "Fehler: Index des Listeneintrags unbekannt. " + "/n" + "Änderung wird verworfen", Toast.LENGTH_LONG);
                    return;
                }

 //               Club club = hitsPerClub.get(listIndex);
 //               club.setClubName(clubName);
 //               club.setClubType(clubType);
                rvHitsPerClub.getAdapter().notifyItemChanged(listIndex);
//                BagController.writeBagToFile(fileDirectory, hitsPerClub);
                break;

            default:
                Toast.makeText(this, "Fehler: Unbekannter Dialogmodus. " + "/n" + "Änderung wird verworfen", Toast.LENGTH_LONG);
        }
        dialogIsActive = false;
    }

    private void calculateHits(int listIndex, int val) {
        HitsPerClub hits = hitsPerClubList.get(listIndex);

    }

    private void updateItemRange() {
 //       BagController.writeBagToFile(fileDirectory, hitsPerClubList);
    }

    private void deleteItem(int listIndex) {
        hitsPerClubList.remove(listIndex);
        rvHitsPerClub.getAdapter().notifyItemRemoved(listIndex);
        rvHitsPerClub.getAdapter().notifyItemRangeChanged(listIndex, hitsPerClubList.size());
//        BagController.writeBagToFile(fileDirectory, hitsPerClubList);
        // Show user that he can start
        activateGoButton(hitsPerClubList.size() > 0);
    }

    private void editItem(int listIndex) {
        String clubName = hitsPerClubList.get(listIndex).getClubName();
//        ClubType clubType = hitsPerClubList.get(listIndex).getClubType();

        ClubDialog dialog = new ClubDialog();
        dialog.setClubName(clubName);
//        dialog.setClubType(clubType);
        dialog.setListIndex(listIndex);
        dialog.setDialogMode(CLUB_DIALOG_MODE_EDIT);

        dialog.show(getSupportFragmentManager(), "Referenzroute bearbeiten");
    }

    /**
     * Shows dialog with new reference route name and description
     */
    public void addItem(View view) {
        if (dialogIsActive) {
            return;
        }

        ClubDialog dialog = new ClubDialog();
        dialog.setDialogMode(CLUB_DIALOG_MODE_ADD);
        dialog.show(getSupportFragmentManager(), "Referenzroute hinzufügen");
    }

    public boolean changeItems(int oldPos, int newPos) {
        HitsPerClub itemMoveToPosOld = hitsPerClubList.get(newPos);
        HitsPerClub itemMoveToPosNew = hitsPerClubList.get(oldPos);
        hitsPerClubList.set(newPos, itemMoveToPosNew);
        hitsPerClubList.set(oldPos, itemMoveToPosOld);

        rvHitsPerClub.getAdapter().notifyItemChanged(newPos);
        rvHitsPerClub.getAdapter().notifyItemChanged(oldPos);

        updateItemRange();

        return true;
    }

    /**
     *
     * @param view
     * @param listIndex
     */
    @Override
    public void itemClicked(View view, int listIndex) {
        int viewId = view.getId();
        if (viewId == R.id.itemClubName || viewId == R.id.itemCountText) {
            return;
        }

//        RecyclerView.ViewHolder holder = rvHitsPerClub.findViewHolderForAdapterPosition(listIndex);
//        holder.itemView.getId();
//        TextView counter = (TextView) holder.itemView.findViewById(R.id.itemCountText);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupHitCategory);
        HitCategory hitCategory = null;
        int idx = radioGroup.getCheckedRadioButtonId();
        if (idx == findViewById((R.id.radioButtonRegular)).getId()) {
            hitCategory = HitCategory.REGULAR;
        }
        if (idx == findViewById((R.id.radioButtonPitch)).getId()) {
            hitCategory = HitCategory.PITCH;
        }
        if (idx == findViewById((R.id.radioButtonChip)).getId()) {
            hitCategory = HitCategory.CHIP;
        }
        if (idx == findViewById((R.id.radioButtonBunker)).getId()) {
            hitCategory = HitCategory.BUNKER;
        }

        HitsPerClub hitsPerClubOverAll = hitsPerClubList.get(listIndex);
        Club club = BagController.getClubByName(hitsPerClubOverAll.getClubName());

        HitsPerClub hitsPerClubAndCat = null;

        switch (hitCategory) {
            case PITCH:
                hitsPerClubAndCat = HitsPerClubController.getHitsPerClubAndCat(HitCategory.PITCH, club);
                break;
            case CHIP:
                hitsPerClubAndCat = HitsPerClubController.getHitsPerClubAndCat(HitCategory.CHIP, club);
                break;
            case BUNKER:
                hitsPerClubAndCat = HitsPerClubController.getHitsPerClubAndCat(HitCategory.BUNKER, club);
                break;

            case REGULAR:
            default:
                hitsPerClubAndCat = HitsPerClubController.getHitsPerClubAndCat(HitCategory.REGULAR, club);
                break;

        }

        switch (viewId) {
            case R.id.button_positive:
                hitsPerClubAndCat.incrementHitsGood(1);
                hitsPerClubList.get(listIndex).incrementHitsGood(1);
                break;

            case R.id.button_neutral:
                hitsPerClubAndCat.incrementHitsNeutral(1);
                hitsPerClubList.get(listIndex).incrementHitsNeutral(1);
                break;

            case R.id.button_negative:
                hitsPerClubAndCat.incrementHitsBad(1);
                hitsPerClubList.get(listIndex).incrementHitsBad(1);
                break;
            default:
        }

        HitsPerClubController.setHitsPerClubAndCat(hitCategory, club, hitsPerClubAndCat);
        rvHitsPerClub.getAdapter().notifyItemChanged(listIndex);
    }

    public static void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton("OK", null);
        builder.show();
    }

    private void resetRouting() {
        logger.finest("resetRouting", "Activate GO Button");
        Button button = findViewById(R.id.buttonNew);
        button.setText("GO");
        button.setHintTextColor(Color.parseColor(MARK_COLOR_IN_WORK));
        isPaused = false;
        isRoundActive = false;
    }

    private void buttonPauseClicked() {
        logger.finest("buttonPauseClicked", "React to user action");

        // if 'resume' was clicked and MapTrip not was yet on top
        // the button may not be clicked once again
        if (pauseButtonWasClicked) {
            return;
        }
        pauseButtonWasClicked = true;

        Button button = findViewById(R.id.buttonNew);
        button.setText("FORTSETZEN");
        button.setHintTextColor(Color.parseColor(MARK_COLOR_PAUSED));
        isPaused = true;
        golfStatsManager.interruptRouting();
    }

    private void buttonGoClicked() {
        logger.finest("buttonNewClicked", "");
        Button button = findViewById(R.id.buttonNew);
        boolean restartTour = false;
        button.setText("PAUSE");
        button.setHintTextColor(Color.parseColor(MARK_COLOR_NOT_DONE));

        if (!golfStatsManager.isWorking()) {
            refreshAllItems();
        }

        // if before active route was paused, resume
        // resume means that it's not recommended to start route from beginning of tour
        if (isPaused) {
            restartTour = true;
        }

        isPaused = false;
        isRoundActive = true;

        routeRefRoute(restartTour);
    }

    /*
    Take a reference route and start it.
    If the last route was paused it is resumed.
    If the last route was finished start the next route.
     */
    private void routeRefRoute(boolean restartTour) {
        logger.info("routeRefRoute", "see if there is to start a reference route; restartTour = " + restartTour);
        if (!restartTour) {
            logger.finest("routeRefRoute", "call mti.resetWorkingSwitch");
            golfStatsManager.resetWorkingSwitch();
        }

        if (golfStatsManager.nextRefRouteExists()) {
            logger.finest("routeRefRoute", "one more reference route exists; routeItem");
            int nextRefRouteId = golfStatsManager.getNextRefRouteId();
            markItemInWork(nextRefRouteId);
        } else {
            logger.finest("routeRefRoute", "no more reference route exists; resetRouting, mtiCalls.reset");
            resetRouting();
            golfStatsManager.reset();
            hideMapTrip(golfStatsManager);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.info("onDestroy", "Anwendung wird beendet.");
    }

    @Override
    public void onResume() {
        super.onResume();
        pauseButtonWasClicked = false;
        logger.fine("onResume", "Anwendung wurde in den Vordergrund geholt");
    }

    /**
     * To keep the switch from MapTrip to this view simple, this App is a singleTask (see Manifest)
     */
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Lookup the recyclerview in activity layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#FFFFFF"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);

        setTitle("Easy Golf Stats");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setTitle("Easy Golf Stats");
        actionBar.setLogo(R.drawable.titel_egs_simple);

        // Search instance of RecyclerView
        rvHitsPerClub = (RecyclerView) findViewById(R.id.recyclerViewRefRoutes);

        String basePath = getExternalFilesDir(null).getAbsolutePath();

        // Initialize reference routes list
        fileDirectory = basePath + "/data";

        // Prepare Logger
        // Basic path of files - here should be stored the loggers property file (if used)
        Logger.setBasePath(basePath);
        // Get a logger instance
        logger = Logger.createLogger("MainActivity");

        logger.finest("onCreate", "-->       New Instance        <--");
        logger.info("onCreate", "EasyGolfStats App wird initialisiert");
        logger.config("onCreate", "Basisverzeichnis: " + basePath);
        logger.config("onCreate", "Datenverzeichnis: " + fileDirectory);

        HitsPerClubController.initDataDirectory(basePath, "data");
        BagController.initClubList(fileDirectory);
        if (!HitsPerClubController.isStatisticOpen()) {
            ArrayList<Club> clubs = BagController.getClubListSorted();
            HitsPerClubController.initHitFile(fileDirectory, clubs);
        }
        hitsPerClubList = HitsPerClubController.getHitsPerClubFromFile();
        final HitsPerClubAdapter adapter = new HitsPerClubAdapter(hitsPerClubList, this);

        // Attach the adapter to the recyclerview to populate items
        rvHitsPerClub.setAdapter(adapter);

        // SWIPE and MOVE
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder
                            target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();
                        return changeItems(fromPos, toPos);
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
//                        refRoutes.remove(viewHolder.getAdapterPosition());
//                        adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvHitsPerClub);

        // Set layout manager to position the items
        // LinearLayoutManager for usage of dividers
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvHitsPerClub.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvHitsPerClub.getContext(),
                layoutManager.getOrientation());
        rvHitsPerClub.addItemDecoration(dividerItemDecoration);

        Settings settings = new Settings(basePath + "/app.properties");
        // init worker
        golfStatsManager = new GolfStatsManager(hitsPerClubList);

    }


    // ======================================================================================================
    // GUI elements manipulation
    // ======================================================================================================

    private void markItemPaused(int refRouteId) {
        try {
            RecyclerView.ViewHolder holder = rvHitsPerClub.findViewHolderForAdapterPosition(refRouteId);
            holder.itemView.setBackgroundColor(Color.parseColor(MARK_COLOR_PAUSED));
 //           holder.itemView.findViewById(R.id.itemDeleteButton).setBackgroundColor(Color.parseColor(MARK_COLOR_PAUSED));
            holder.itemView.refreshDrawableState();
        } catch (Exception e) {
            logger.warn("markItemPause", "Fehler bei markItemPaused: " + e.getMessage());
        }
    }

    private void markItemDone(int refRouteId) {
        try {
            RecyclerView.ViewHolder holder = rvHitsPerClub.findViewHolderForAdapterPosition(refRouteId);
            holder.itemView.setBackgroundColor(Color.parseColor(MARK_COLOR_ROUTE_DONE));
 //           holder.itemView.findViewById(R.id.itemDeleteButton).setBackgroundColor(Color.parseColor(MARK_COLOR_ROUTE_DONE));
            holder.itemView.refreshDrawableState();
        } catch (Exception e) {
            logger.warn("markItemDone", "Fehler bei markItemDone: " + e.getMessage());
        }
    }

    private void markItemNotDone(int refRouteId) {
        try {
            RecyclerView.ViewHolder holder = rvHitsPerClub.findViewHolderForAdapterPosition(refRouteId);
            holder.itemView.setBackgroundColor(Color.parseColor(MARK_COLOR_NOT_DONE));
 //           holder.itemView.findViewById(R.id.itemDeleteButton).setBackgroundColor(Color.parseColor(MARK_COLOR_NOT_DONE));
            holder.itemView.refreshDrawableState();
        } catch (Exception e) {
            logger.warn("markItemNotDone", "Fehler bei markItemNotDone: " + e.getMessage());
        }
    }

    private void markItemInWork(final int refRouteId) {
        try {
            RecyclerView.ViewHolder holder = rvHitsPerClub.findViewHolderForAdapterPosition(refRouteId);
            holder.itemView.setBackgroundColor(Color.parseColor(MARK_COLOR_IN_WORK));
//            holder.itemView.findViewById(R.id.itemDeleteButton).setBackgroundColor(Color.parseColor(MARK_COLOR_IN_WORK));
            holder.itemView.refreshDrawableState();
        } catch (Exception e) {
            logger.warn("markItemInWork", "Fehler bei markItemInWork: " + e.getMessage());
        }
    }

    /**
     * Unmark list items
     */
    private void refreshAllItems() {
        for (int i = 0; i < hitsPerClubList.size(); i++) {
            markItemNotDone(i);
        }
    }

    private void activateGoButton(boolean active) {
        Button button = findViewById(R.id.buttonNew);
        button.setEnabled(active);
        button.setClickable(active);

        if (active) {
            button.setTextColor(Color.parseColor("#8BC34A"));
        } else {
            button.setTextColor(Color.parseColor("#000000"));
        }
    }


    // ======================================================================================================
    // Threads
    // ======================================================================================================
    private void hideMapTrip(final GolfStatsManager golfStatsManager) {
        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("HideMapThread");
                String className = MainActivity.this.getClass().getCanonicalName();
                String packageName = getPackageName();
                golfStatsManager.showApp(packageName, className);
            }
        });
        serverThread.start();
    }

}
