package de.easygolfstats.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import de.easygolfstats.R;
import de.easygolfstats.types.ClubType;

/**
 * Dialog for renaming or adding clubs
 */
public class ClubDialog extends AppCompatDialogFragment {

    private String clubName;
    private ClubType clubType;
    private Integer listIndex = -1;
    private int dialogMode;

    private EditText clubNameEditText;
    private EditText clubTypeEditText;
    private RefRouteDialogListener dialogListener;

    public void setClubName (String clubName) {
        this.clubName = clubName;
    }

    public void setClubType (ClubType clubType) {
        this.clubType = clubType;
    }

    public void setDialogMode (int dialogMode) {
        this.dialogMode = dialogMode;
    }

    public void setListIndex (Integer listIndex) {
        this.listIndex = listIndex;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.refroute_dialog, null);

        builder.setView(view)
                .setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel, nothing to do
                        dialogListener.clubDialogCancel();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK, Werte verarbeiten
                        String clubName = clubNameEditText.getText().toString();
                        ClubType clubType = ClubType.valueOf(clubTypeEditText.getText().toString());
                        dialogListener.clubDialogOk(clubName, clubType, listIndex, dialogMode);
                    }
                });

        clubNameEditText = view.findViewById(R.id.addRefRouteName);
        clubTypeEditText = view.findViewById(R.id.addRefRouteDescription);

        if (this.clubName != null && this.clubName.length() > 0) {
            clubNameEditText.setText(this.clubName);
        }
        if (this.clubType != null ) {
            clubTypeEditText.setText(this.clubType.toString());
        }

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            dialogListener = (RefRouteDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Exception in NewRefRouteDialo not implemented yet");
        }
    }

    /**
     * Interface to implement in calling class
     */
    public interface RefRouteDialogListener {
        /**
         * Delivers the values of RefRouteName and RefRouteDescription
         * @param clubName
         * @param clubType
         * @param listIndex
         * @param dialogMode
         */
        void clubDialogOk(String clubName, ClubType clubType, Integer listIndex, int dialogMode);
        void clubDialogCancel();
    }

}
