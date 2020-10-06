package de.easygolfstats.itemList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.easygolfstats.R;
import de.easygolfstats.model.HitsPerClub;

import java.util.List;

public class HitsPerClubAdapter extends RecyclerView.Adapter<HitsPerClubAdapter.ViewHolder> {
    private List<HitsPerClub> hitsPerClubs;

    private ItemClickListener itemClickListenerVar;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView clubNameTextView;
        public TextView positiveCounter;
        public Button buttonPositive;
        public Button buttonNeutral;
        public Button buttonNegative;

        public ViewHolder (View itemView) {
            super (itemView);
            clubNameTextView = (TextView) itemView.findViewById(R.id.itemClubName);
            positiveCounter = (TextView) itemView.findViewById(R.id.itemCountText);
            buttonNegative = (Button) itemView.findViewById(R.id.button_negative);
            buttonNeutral = (Button) itemView.findViewById(R.id.button_neutral);
            buttonPositive = (Button) itemView.findViewById(R.id.button_positive);
        }
    }

    public HitsPerClubAdapter(List<HitsPerClub> hitsPerClubs, ItemClickListener itemClickListener) {
        this.hitsPerClubs = hitsPerClubs;
        itemClickListenerVar = itemClickListener;
    }

    @Override
    public HitsPerClubAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View refRouteView = inflater.inflate(R.layout.item_hits, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(refRouteView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HitsPerClubAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        HitsPerClub hitsPerClub = hitsPerClubs.get(position);
        final int finalVar = position;

        // Set item views based on your views and data model
        TextView clubNameTextView = viewHolder.clubNameTextView;
        TextView positiveCounter = viewHolder.positiveCounter;
        clubNameTextView.setText(hitsPerClub.getClubName());
        positiveCounter.setText(String.valueOf(hitsPerClub.getHitsPositiveCalculated()));
        Button buttonNeutral = viewHolder.buttonNeutral;
        Button buttonPositive = viewHolder.buttonPositive;
        Button buttonNegative = viewHolder.buttonNegative;

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListenerVar.itemClicked(v, finalVar);
            }
        };
        clubNameTextView.setOnClickListener(onClickListener);
        positiveCounter.setOnClickListener(onClickListener);
        buttonNegative.setOnClickListener(onClickListener);
        buttonNeutral.setOnClickListener(onClickListener);
        buttonPositive.setOnClickListener(onClickListener);
    }

    public interface ItemClickListener {
        public void itemClicked(View view, int index);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return hitsPerClubs.size();
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }
}