package com.example.beautydemo.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.example.beautydemo.R;
import com.smart.smartbeauty.api.SmartBeautyResource;
import com.smart.smartbeauty.api.SmartResourceData;

/**
 * 贴纸资源适配器
 */
public class PreviewResourceAdapter extends RecyclerView.Adapter<PreviewResourceAdapter.ResourceHolder> {

    private Drawable mPlaceHolder;
    private Context mContext;
    private int mSelected;

    private OnResourceChangeListener mListener;

    public PreviewResourceAdapter(Context context) {
        mContext = context;
        mSelected = 0;
        mPlaceHolder = context.getDrawable(R.drawable.ic_camera_thumbnail_placeholder);
    }

    @NonNull
    @Override
    public ResourceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_preview_resource_view,
                parent, false);
        ResourceHolder holder = new ResourceHolder(view);
        holder.resourceRoot = (LinearLayout) view.findViewById(R.id.resource_root);
        holder.resourcePanel = (FrameLayout) view.findViewById(R.id.resource_panel);
        holder.resourceThumb = (ImageView) view.findViewById(R.id.resource_thumb);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceHolder holder, int position) {
        holder.resourceThumb.setImageBitmap(SmartBeautyResource.getResourceImageBitmap(mContext, position));

        if (position == mSelected) {
            holder.resourcePanel.setBackgroundResource(R.drawable.ic_camera_effect_selected);
        } else {
            holder.resourcePanel.setBackgroundResource(0);
        }

        final int currentPosition = position;
        holder.resourceRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected == currentPosition) {
                    return;
                }
                int last = mSelected;
                mSelected = currentPosition;
                notifyItemChanged(last);
                notifyItemChanged(currentPosition);
                if (mListener != null) {
                    mListener.onResourceChanged(SmartBeautyResource.getResourceData(currentPosition));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return SmartBeautyResource.getResourceListDataSize();
    }

    public class ResourceHolder extends RecyclerView.ViewHolder {

        public LinearLayout resourceRoot;
        public FrameLayout resourcePanel;
        public ImageView resourceThumb;

        public ResourceHolder(View itemView) {
            super(itemView);
        }
    }


    public interface OnResourceChangeListener {
        void onResourceChanged(SmartResourceData resourceData);
    }

    public void setOnResourceChangeListener(OnResourceChangeListener listener) {
        mListener = listener;
    }

}
