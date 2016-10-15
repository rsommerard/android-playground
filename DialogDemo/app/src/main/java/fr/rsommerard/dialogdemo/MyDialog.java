package fr.rsommerard.dialogdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Romain on 15/10/2016.
 */
public class MyDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class because this dialog has a simple UI
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Make a selection")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Nothing happening here
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Nothing happening here either
                    }
                });

        return builder.create();
    }
}
