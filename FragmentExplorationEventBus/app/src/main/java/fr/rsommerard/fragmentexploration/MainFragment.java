package fr.rsommerard.fragmentexploration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Romain on 15/10/2016.
 */

public class MainFragment extends Fragment {

    private static final String TITLE = "TITLE";

    public static MainFragment newInstance(String title) {
        MainFragment fragment = new MainFragment();

        Bundle args = new Bundle();
        args.putString(TITLE, title);

        fragment.setArguments(args);

        return fragment;
    }

    TextView title;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = (TextView) view.findViewById(R.id.title);

        if (getArguments() != null) {
            Bundle args = getArguments();
            if (args.containsKey(TITLE)) {
                title.setText(args.getString(TITLE));
            }
        }

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new TitleClickedEvent());
            }
        });
    }
}
