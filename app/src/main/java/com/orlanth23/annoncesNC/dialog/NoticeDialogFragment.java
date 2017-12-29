package com.orlanth23.annoncesnc.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orlanth23.annoncesnc.R;

public class NoticeDialogFragment extends DialogFragment {

    public static final String P_MESSAGE = "message";
    public static final String P_TYPE = "type";
    public static final String P_IMG = "image";
    public static final int TYPE_BOUTON_YESNO = 10;
    public static final int TYPE_BOUTON_OK = 20;
    public static final int TYPE_IMAGE_CAUTION = 100;
    public static final int TYPE_IMAGE_ERROR = 110;
    public static final int TYPE_IMAGE_INFORMATION = 120;
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            Log.e("ClassCastException", e.getMessage(), e);
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                + " doit implementer l'interface NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_layout, null);
        builder.setView(view);

        Integer typeBouton = getArguments().getInt(P_TYPE);
        Integer typeImage = getArguments().getInt(P_IMG);
        TextView textview = view.findViewById(R.id.msgDialog);

        /* Fenêtre de confirmation */
        // On applique le message d'erreur
        textview.setText(getArguments().getString(P_MESSAGE));

        switch (typeBouton) {
            case TYPE_BOUTON_OK:
                builder.setPositiveButton(R.string.dialogOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clic sur OK
                        mListener.onDialogPositiveClick(NoticeDialogFragment.this);
                    }
                });
                break;
            case TYPE_BOUTON_YESNO:
                builder.setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clic sur OK
                        mListener.onDialogPositiveClick(NoticeDialogFragment.this);
                    }
                }).setNegativeButton(R.string.dialogNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clic sur annuler
                        mListener.onDialogNegativeClick(NoticeDialogFragment.this);
                    }
                });
                break;
        }

        ImageView imgView = view.findViewById(R.id.imageDialog);
        switch (typeImage) {
            case TYPE_IMAGE_CAUTION:
                imgView.setImageResource(R.drawable.ic_attention);
                break;
            case TYPE_IMAGE_ERROR:
                imgView.setImageResource(R.drawable.ic_remove);
                break;
            case TYPE_IMAGE_INFORMATION:
                imgView.setImageResource(R.drawable.ic_information);
                break;
            default:
                imgView.setImageResource(R.drawable.ic_information);
                break;
        }

        // On retourne l'objet créé.
        return builder.create();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }
}
