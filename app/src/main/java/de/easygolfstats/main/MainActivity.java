package de.easygolfstats.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
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
import de.easygolfstats.types.HitCategory;

public class MainActivity extends AppCompatActivity implements HitsPerClubAdapter.ItemClickListener {

    private static final String MARK_COLOR_ROUTE_DONE = "#9CB548";
    private static final String MARK_COLOR_NOT_ACTIVE = "#E6EAED";
    private static final String MARK_COLOR_NOT_DONE = "#FFFFFF";
    private static final String MARK_COLOR_IN_WORK = "#BD1550";
    private static final String MARK_COLOR_PAUSED = "#E97F02";

    private static MainActivity mainActivity;

    private ArrayList<HitsPerClub> hitsPerClubList;
    private RecyclerView rvHitsPerClub;

    private String fileDirectory;
    private Logger logger;


    public void newPeriod(View view) {
        logger.finest("newPeriod", "NEW Button was clicked");


        HitsPerClubController.finishStatistic();
        int size = hitsPerClubList.size();

        hitsPerClubList.clear();
        rvHitsPerClub.getAdapter().notifyDataSetChanged();

        HitsPerClubController.initializeFiles();
        hitsPerClubList = HitsPerClubController.copyHitsPerClubFromFile(hitsPerClubList);
        size = hitsPerClubList.size();
        for (int i = 0; i < size; i++) {
            rvHitsPerClub.getAdapter().notifyItemInserted(0);
        }
        rvHitsPerClub.getAdapter().notifyDataSetChanged();

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.info("onDestroy", "Anwendung wird beendet.");
    }

    @Override
    public void onResume() {
        super.onResume();
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
        HitsPerClubController.initializeFiles();
        BagController.initClubList(fileDirectory);

        hitsPerClubList = HitsPerClubController.getHitsPerClubFromFile();
        final HitsPerClubAdapter adapter = new HitsPerClubAdapter(hitsPerClubList, this);

        // Attach the adapter to the recyclerview to populate items
        rvHitsPerClub.setAdapter(adapter);


        // Set layout manager to position the items
        // LinearLayoutManager for usage of dividers
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvHitsPerClub.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvHitsPerClub.getContext(),
                layoutManager.getOrientation());
        rvHitsPerClub.addItemDecoration(dividerItemDecoration);

        Settings settings = new Settings(basePath + "/app.properties");
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

    private void markItemNotDone(int index) {
        try {
            RecyclerView.ViewHolder holder = rvHitsPerClub.findViewHolderForAdapterPosition(index);
            holder.itemView.findViewById(R.id.itemCountText);


        } catch (Exception e) {
            logger.warn("markItemNotDone", "Fehler bei markItemNotDone: " + e.getMessage());
        }
    }

    private void refreshAllItems() {
        for (int i = 0; i < hitsPerClubList.size(); i++) {
            rvHitsPerClub.getAdapter().notifyItemChanged(i);

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

}
