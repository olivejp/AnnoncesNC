package com.orlanth23.annoncesnc.fragment;

import android.app.Fragment;
import android.os.Bundle;

import com.orlanth23.annoncesnc.dto.Annonce;
import com.orlanth23.annoncesnc.dto.Message;

import java.util.ArrayList;

public class MessageByAnnoncesFragment extends Fragment {

    private static final String PARAM_LIST = "PARAM_LIST";
    private static final String PARAM_ANNONCE = "PARAM_ANNONCE";

    public MessageByAnnoncesFragment() {
        // Required empty public constructor
    }

    public static MessageByAnnoncesFragment newInstance(ArrayList<Message> listMessages, Annonce annonce) {
        MessageByAnnoncesFragment fragment = new MessageByAnnoncesFragment();
        Bundle args = new Bundle();

        if (listMessages != null) {
            args.putParcelableArrayList(PARAM_LIST, listMessages);
        }
        if (annonce != null) {
            args.putParcelable(PARAM_ANNONCE, annonce);
        }

        fragment.setArguments(args);
        return fragment;
    }

}
