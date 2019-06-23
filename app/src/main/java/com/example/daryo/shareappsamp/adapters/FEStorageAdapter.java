package com.example.daryo.shareappsamp.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.daryo.shareappsamp.R;
import com.example.daryo.shareappsamp.interfaces.ItemCheckChangedListener;
import com.example.daryo.shareappsamp.interfaces.ItemClickListener;
import com.example.daryo.shareappsamp.utils.Item;

import java.util.List;


public class FEStorageAdapter extends RecyclerView.Adapter<FEStorageAdapter.ViewHolder> {
    private Item[] items;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemCheckChangedListener itemCheckChangedListener;
    private Context context;


    // data is passed into the constructor
    public FEStorageAdapter(Context context, Item[] items) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.items = items;
    }

    public void setItems(Item[] items){
        this.items = items;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_recycler_fe_storage, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        //reset values first
        holder.mItemView.setOnClickListener(null);
        holder.mCheckBox.setOnCheckedChangeListener(null);
        holder.mCheckBox.setVisibility(View.VISIBLE);

        String fileName = items[position].getFileName();
        holder.myTextView.setText(fileName);

        holder.mCheckBox.setChecked(items[position].isSelected());

        if(position == 0 || items[position].getFileName().equals("No Files")){
            holder.mCheckBox.setVisibility(View.GONE);
        }

        //get parent layout
        RelativeLayout r = (RelativeLayout) ((ViewGroup) holder.myTextView.getParent());

        // put the image on the text view
        //check if icon is not 0 means nulls
        if (!items[position].getFileName().equals("No Files")) {

            //set gravity to left/reset
            r.setGravity(Gravity.LEFT);

            Drawable img = context.getResources().getDrawable(
                    position == 0 ? R.drawable.directory_up :
                            items[position].isDir() ? R.drawable.directory_icon :
                                    R.drawable.file_icon
            );
            img.setBounds(0, 0, 100, 100);

            holder.myTextView.setCompoundDrawables(img, null, null, null);

            // add margin between image and text (support various screen
            // densities)
            int dp5 = (int) (5 * context.getResources().getDisplayMetrics().density + 0.5f);
            holder.myTextView.setCompoundDrawablePadding(dp5);
        }else {
            holder.myTextView.setCompoundDrawables(null, null, null, null);

            //set gravity to center
            r.setGravity(Gravity.CENTER);
        }

        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(items[position].getFileName().equals("No Files")){
                    return;
                }
                if(items[position].isDir()){
                    if(holder.mCheckBox.isChecked()){
                        holder.mCheckBox.setChecked(false);
                    }else {
                        if (mClickListener != null) mClickListener.onItemClick(position);
                    }
                }else {
                    if(position != 0) {
                        holder.mCheckBox.setChecked(!items[position].isSelected());
                    }else {
                        if (mClickListener != null) mClickListener.onItemClick(position);
                    }
                }

            }
        });

        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                items[position].setIsSelected(isChecked);
                if (itemCheckChangedListener != null) {
                    itemCheckChangedListener.onItemCheckChanged(position, isChecked);
                }
            }
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return items.length;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;
        CheckBox mCheckBox;
        View mItemView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tv_file_or_directory_name);
            mCheckBox = itemView.findViewById(R.id.cb_file_or_directory);
            mItemView = itemView;
        }

    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return items[id].getFileName();
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // allow cb change events to be caught
    public void setItemCheckChangedListener(ItemCheckChangedListener itemCheckChangedListener){
        this.itemCheckChangedListener = itemCheckChangedListener;
    }


}
