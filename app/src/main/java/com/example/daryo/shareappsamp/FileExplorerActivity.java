package com.example.daryo.shareappsamp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.daryo.shareappsamp.adapters.FEStorageAdapter;
import com.example.daryo.shareappsamp.fragments.fileexplorer.FEMainFragment;
import com.example.daryo.shareappsamp.fragments.fileexplorer.FEStorageFragment;
import com.example.daryo.shareappsamp.utils.Item;
import com.example.daryo.shareappsamp.utils.PermissionsAndroid;

import java.io.File;
import java.util.ArrayList;

public class FileExplorerActivity extends AppCompatActivity {

    private Button send;
    private TextView status;

    ArrayList<Item> chosenFiles = new ArrayList<Item>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);

        send = findViewById(R.id.fe_btn_send);
        status = findViewById(R.id.fe_tv_status);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.file_explorer_fragment_container, new FEMainFragment()).commit();

        boolean isExternalStorage = PermissionsAndroid.getInstance().checkWriteExternalStoragePermission(this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && !isExternalStorage) {
            PermissionsAndroid.getInstance().requestForWriteExternalStoragePermission(this);
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FileExplorerActivity.this, SendActivity.class);
                i.putExtra("paths", chosenFiles);
                startActivity(i);
                finish();
            }
        });

        status.setText("Selected (0)");

    }

    public void inflateStorageFragment(String path, String title){

        Bundle bundle = new Bundle ();
        bundle.putString("path", path);
        bundle.putString("title", title);

        FEStorageFragment feStorageFragment = new FEStorageFragment();
        feStorageFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.file_explorer_fragment_container, feStorageFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.file_explorer_fragment_container);
        if(f instanceof FEStorageFragment){
            ((FEStorageFragment) f).up();
        }else {
            super.onBackPressed();
        }
    }

    public void setActionBarTitle(String title, String sub) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(sub);
    }

    public void setSelected(ArrayList<Item> chosenFiles){
        status.setText("Selected (" + chosenFiles.size() +")");
        this.chosenFiles = chosenFiles;
    }

}
