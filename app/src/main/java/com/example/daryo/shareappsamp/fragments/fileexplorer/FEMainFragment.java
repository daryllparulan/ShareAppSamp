package com.example.daryo.shareappsamp.fragments.fileexplorer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daryo.shareappsamp.FileExplorerActivity;
import com.example.daryo.shareappsamp.R;
import com.example.daryo.shareappsamp.adapters.FEMainAdapter;
import com.example.daryo.shareappsamp.interfaces.ItemClickListener;

import java.io.File;
import java.util.ArrayList;


public class FEMainFragment extends Fragment implements ItemClickListener {

    private RecyclerView recyclerView;
    private FEMainAdapter adapter;

    ArrayList<String> storageName = new ArrayList<>();

    //for internal path
    String extStore = System.getenv("EXTERNAL_STORAGE");

    //for external path
    String secStore = System.getenv("SECONDARY_STORAGE");

    public FEMainFragment(){
        if(extStore != null){
            storageName.add("Internal storage");
        }
        if(secStore != null){
            storageName.add("SD card");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_file_explorer_main, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_main_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FEMainAdapter(getContext(), storageName);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return rootView;
    }

    @Override
    public void onItemClick(int position) {
        // TODO
        Log.e("TAG", storageName.get(position) + " is clicked");
        if(position == 0){
            ((FileExplorerActivity)getActivity()).inflateStorageFragment(extStore, storageName.get(position));
        }else {
            ((FileExplorerActivity)getActivity()).inflateStorageFragment(secStore, storageName.get(position));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((FileExplorerActivity)getActivity()).setActionBarTitle("Storage", "");
    }
}
