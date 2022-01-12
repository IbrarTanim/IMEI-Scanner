package com.zavaly.imeiscanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.zavaly.imeiscanner.R;
import com.zavaly.imeiscanner.dto.entities.ImeiCache;

import java.util.ArrayList;
import java.util.List;

public class CheckRecyclerAdapter extends RecyclerView.Adapter<CheckRecyclerAdapter.CheckViewHolder>{

    private Context context;
    private List<ImeiCache> imeiList = new ArrayList<>();

    public CheckRecyclerAdapter(Context context, List<ImeiCache> imeiList) {
        this.context = context;
        this.imeiList = imeiList;
    }

    @NonNull
    @Override
    public CheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_check_rv, parent, false);
        return new CheckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckViewHolder holder, int position) {

        if (imeiList != null){

            holder.imeiNumberTV.setText(imeiList.get(position).getImeiNumber());

            if (imeiList.get(position).isSmsResult()){

                holder.resultIV.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.done_32, null));

            }else {

                holder.resultIV.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.cancel_32, null));

            }

        }

    }

    @Override
    public int getItemCount() {
        return imeiList.size();
    }

    static class CheckViewHolder extends RecyclerView.ViewHolder{

        MaterialTextView imeiNumberTV;
        ImageView resultIV;

        public CheckViewHolder(@NonNull View itemView) {
            super(itemView);
            imeiNumberTV = itemView.findViewById(R.id.row_imei_number);
            resultIV = itemView.findViewById(R.id.row_result_iv);
        }
    }

}
