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
import com.smart.smartbeauty.api.SmartResourceType;
import com.smart.smartbeauty.filter.SmartRenderManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 贴纸资源适配器
 */
public class PreviewResourceAdapter extends RecyclerView.Adapter<PreviewResourceAdapter.ResourceHolder> {

    private Drawable mPlaceHolder;
    private Context mContext;
    private int mSelected;

    private OnResourceChangeListener mListener;

    // 资源列表
    private static final List<SmartResourceData> mResourceList = new ArrayList<>();

    public PreviewResourceAdapter(Context context) {
        mContext = context;
        mSelected = 0;
        mPlaceHolder = context.getDrawable(R.drawable.ic_camera_thumbnail_placeholder);
        initResource();
    }

    private void initResource(){
        // 添加资源列表，如果可以是Assets文件夹下的，也可以是绝对路径下的zip包
        mResourceList.add(SmartBeautyResource.getResourceData(mContext,"none", "assets://resource/none.zip",
                SmartResourceType.NONE, "none", "assets://thumbs/resource/none.png"));

        mResourceList.add(SmartBeautyResource.getResourceData(mContext,"cat", "assets://resource/cat.zip",
                SmartResourceType.STICKER, "cat", "assets://thumbs/resource/cat.png"));

        mResourceList.add(SmartBeautyResource.getResourceData(mContext,"test_sticker1", "assets://resource/test_sticker1.zip",
                SmartResourceType.STICKER, "test_sticker1", "assets://thumbs/resource/sticker_temp.png"));

        mResourceList.add(SmartBeautyResource.getResourceData(mContext,"triple_frame", "assets://resource/triple_frame.zip",
                SmartResourceType.FILTER, "triple_frame", "assets://thumbs/resource/triple_frame.png"));

        mResourceList.add(SmartBeautyResource.getResourceData(mContext,"horizontal_mirror", "assets://resource/horizontal_mirror.zip",
                SmartResourceType.FILTER, "horizontal_mirror", "assets://thumbs/resource/horizontal_mirror.png"));

        mResourceList.add(SmartBeautyResource.getResourceData(mContext,"vertical_mirror", "assets://resource/vertical_mirror.zip",
                SmartResourceType.FILTER, "vertical_mirror", "assets://thumbs/resource/vertical_mirror.png"));

        mResourceList.add(SmartBeautyResource.getResourceData(mContext,"market", "assets://resource/market.zip",
                SmartResourceType.STICKER, "market", "assets://thumbs/resource/market.png"));
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
//        holder.resourceThumb.setImageBitmap(SmartBeautyResource.getResourceImageBitmap(mContext, position));

        //TODO: huping add.
        holder.resourceThumb.setImageBitmap(SmartBeautyResource.getResourceImageBitmap(mContext, mResourceList.get(position)));

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
//                    mListener.onResourceChanged(SmartBeautyResource.getResourceData(currentPosition));

                    //TODO  huping add.
                    mListener.onResourceChanged(mResourceList.get(currentPosition));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
//        return SmartBeautyResource.getResourceListDataSize();

        return mResourceList.size();
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
