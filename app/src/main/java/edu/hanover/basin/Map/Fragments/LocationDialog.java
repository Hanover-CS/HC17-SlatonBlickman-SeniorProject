package edu.hanover.basin.Map.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import edu.hanover.basin.R;

/**
 * Dialog fragment to show dialog that will display a message about location services and supplies shortcut for user action
 *
 * @author Slaton Blickman
 * @see DialogFragment
 */

public class LocationDialog extends DialogFragment {

    /**
     * Constructor for creating the dialog to display.
     * Sets the message about use of location services.
     * Sets Positive button to take user to Location Settings on positive Click.
     * Does nothing otherwise.
     *
     * @param savedInstanceState bundle
     * @return Dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.location_enabled)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Take the user to location settings to turn on location services
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do nothing on cancel
//                        Intent intent = new Intent(getActivity(), LoginActivity.class);
//                        startActivity(intent);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
