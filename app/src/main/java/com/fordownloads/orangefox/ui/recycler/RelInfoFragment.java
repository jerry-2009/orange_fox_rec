package com.fordownloads.orangefox.ui.recycler;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.fordownloads.orangefox.App;
import com.fordownloads.orangefox.R;
import org.jetbrains.annotations.NotNull;

public class RelInfoFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rel_info, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((RecyclerView)getActivity().findViewById(R.id.releaseRecycler)).setAdapter(App.getDataAdapterInfo()); //см. App.java
    }

}