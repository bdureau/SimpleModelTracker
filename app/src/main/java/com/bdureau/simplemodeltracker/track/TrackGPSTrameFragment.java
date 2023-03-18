package com.bdureau.simplemodeltracker.track;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bdureau.simplemodeltracker.R;

public class TrackGPSTrameFragment extends Fragment {
    private static final String TAG = "TrackGPSTrameFragment";
    private boolean ViewCreated = false;
    private TextView tvRead;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_trame, container, false);
        tvRead = (TextView) view.findViewById(R.id.tvRead);
        ViewCreated = true;
        return view;
    }

    public void setTrame(String trame) {
        if (ViewCreated)
            tvRead.setText(tvRead.getText() + "\n" + trame);
    }

}
