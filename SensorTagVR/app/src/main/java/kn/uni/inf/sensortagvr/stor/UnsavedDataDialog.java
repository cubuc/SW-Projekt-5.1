package kn.uni.inf.sensortagvr.stor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 *
 * Created by gero on 05.07.17.
 */

public class UnsavedDataDialog extends DialogFragment {

    UnsavedDataDialog.NoticeDialogListener listener;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (UnsavedDataDialog.NoticeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement NoticeDialogListener");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("You have unsaved data items!")
                .setMessage("Do you want to save before you quit?")
                .setPositiveButton("Save & Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(UnsavedDataDialog.this);
                    }
                })
                .setNegativeButton("Just Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(UnsavedDataDialog.this);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNeutralClick(UnsavedDataDialog.this);
                    }
                });

        return builder.create();
    }

    /**
     * Interface to respond to user-inputs.
     * Necessary to interact with services in the calling activity
     */
    public interface NoticeDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNeutralClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }
}
