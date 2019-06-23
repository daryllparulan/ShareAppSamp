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
import com.example.daryo.shareappsamp.adapters.FEStorageAdapter;
import com.example.daryo.shareappsamp.interfaces.ItemCheckChangedListener;
import com.example.daryo.shareappsamp.interfaces.ItemClickListener;
import com.example.daryo.shareappsamp.utils.Item;
import com.example.daryo.shareappsamp.utils.ItemComparator;
import com.example.daryo.shareappsamp.utils.Util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class FEStorageFragment extends Fragment implements ItemClickListener, ItemCheckChangedListener{
    private RecyclerView recyclerView;
    private FEStorageAdapter adapter;

    private File path;
    private static final String TAG = "F_PATH";
    private String chosenFile;

    private Item[] fileList;
    ArrayList<String> choosenFiles = new ArrayList<String>();
    ArrayList<Item> choosenFileItem = new ArrayList<Item>();


    private String title;

    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<String>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_file_explorer_storage, container, false);

        Bundle bundle = getArguments();
        path = new File( bundle.getString("path") + "");
        title = bundle.getString("title");

        loadFileList();


        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_storage_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FEStorageAdapter(getContext(), fileList);
        adapter.setClickListener(this);
        adapter.setItemCheckChangedListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return rootView;
    }

    private void loadFileList() {
        setActionBar();
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory())
                            && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            if(fList == null){
                Log.e("TAG", "list is null either root or n permission, will return!");
                return;
            }
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {

                fileList[i] = new Item(fList[i],
                        false,
                        false,
                        0);

                for (int x = 0; x < choosenFiles.size(); x ++){
                    if(choosenFiles.get(x).equals(path.toString() + "/"  + fList[i])){
                        fileList[i].setIsSelected(true);
                        break;
                    }
                }

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].setIsDir(true);
                    Log.d("DIRECTORY", fileList[i].getFileName());
                } else {
                    // fileList[i].setFileSize(Util.getFileSize(new File(path.toString() + "/" + fileList[i].getFileName())));
                    Log.d("FILE", fileList[i].getFileName());
                }
            }

            // sort
            fileList = sortItems(fileList);

            //add no files if length is zero
            int len = (fileList.length == 0) ? 2 : 1;

            Item temp[] = new Item[fileList.length + len];
            for (int i = 0; i < fileList.length; i++) {
                temp[i + 1] = fileList[i];
            }
            temp[0] = new Item("Up", false, false, 0);

            //add no files
            if(fileList.length == 0)
                temp[1] = new Item("No Files", false, false, 0);

            fileList = temp;

        } else {
            Log.e(TAG, "path does not exist");
        }

    }

    public void up(){
        // if there are no more directories in the list, then
        // its the first level
        if (str.isEmpty()) {
            getFragmentManager().popBackStack();
            return;
        }

        // present directory removed from list
        String s = str.remove(str.size() - 1);

        // path modified to exclude present directory
        path = new File(path.toString().substring(0,
                path.toString().lastIndexOf(s)));
        fileList = null;

        loadFileList();

        adapter.setItems(fileList);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onItemClick(int position) {
        chosenFile = fileList[position].getFileName();
        if (fileList[position].isDir() && position != 0) {
            // Adds chosen directory to list
            str.add(chosenFile);
            fileList = null;
            path = new File(path + "/" + chosenFile);

            loadFileList();

            adapter.setItems(fileList);
            adapter.notifyDataSetChanged();
        }else if(position == 0){
            up();
        }
    }

    @Override
    public void onItemCheckChanged(int position, boolean isChecked) {
        if(isChecked) {
            choosenFiles.add(path.toString() + "/" + fileList[position].getFileName());
            Item it = new Item(path.toString() + "/" +fileList[position].getFileName(),
                    fileList[position].isDir(),
                    fileList[position].isSelected(),
                    Util.getFileSize(new File(path.toString() + "/" +fileList[position].getFileName())));
            choosenFileItem.add(it);
            Log.e("CF", it.getFileName());
            Log.e("CF", "size: " + ((float)it.getFileSize()/1024)/1024 + " mb");
            Log.e("CF",path.toString() + "/" + fileList[position].getFileName() );
        }else {
            for (int i = 0; i < choosenFiles.size(); i++){
                if(choosenFiles.get(i).equals(path.toString() + "/" + fileList[position].getFileName())){
                    choosenFiles.remove(i);
                    choosenFileItem.remove(i);
                }
            }
        }
        setActionBar();
    }

    private void setActionBar(){
        String subTitle = "";
        if(str != null && str.size() > 0){
            for (int x = 0; x < str.size(); x++){
                subTitle = subTitle + "/" + str.get(x);
            }
        }

        ((FileExplorerActivity)getActivity()).setSelected(choosenFileItem);
    }

    private Item[] sortItems(Item[] items){
        //TODO
        //you can change Item arrays tobe Arraylist so its more
        //convenient and standardized

        Item[] i;
        ArrayList<Item> modelList = new ArrayList<>();

        for(Item it : items){
            modelList.add(it);
        }

        Collections.sort(modelList, new ItemComparator());

        i = new Item[modelList.size()];
        for(int x = 0; x < modelList.size(); x++){
            i[x] = modelList.get(x);
        }

        return i;
    }

}
