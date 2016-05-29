package just.juced.justtest.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import just.juced.justtest.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmptyContentFragment extends Fragment {


    public EmptyContentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_empty_content, container, false);
    }

}
