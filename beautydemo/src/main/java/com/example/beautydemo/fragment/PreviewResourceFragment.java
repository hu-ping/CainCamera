package com.example.beautydemo.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.beautydemo.R;
import com.example.beautydemo.adapter.PreviewResourceAdapter;

import com.smart.smartbeauty.api.SmartBeautyResource;
import com.smart.smartbeauty.api.SmartResourceData;

/**
 * 贴纸资源页面
 */
public class PreviewResourceFragment extends Fragment {

    private static final String TAG = "PreviewResourceFragment";

    // 内容显示列表
    private View mContentView;

    // 标题
    private LinearLayout mLayoutStickerTitle;

    // 容器
    private LinearLayout mLayoutStickerContent;

    // 贴纸列表 TODO 后续可以改成ViewPager的形式，用于支持多种贴纸类型
    private LinearLayout mResourceLayout;
    private RecyclerView mResourceView;

    // 布局管理器
    private LayoutInflater mInflater;
    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mInflater = LayoutInflater.from(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_preview_resource, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(mContentView);
    }

    private void initView(View view) {
        mLayoutStickerTitle = (LinearLayout) view.findViewById(R.id.layout_sticker_title);
        mLayoutStickerContent = (LinearLayout) view.findViewById(R.id.layout_sticker_content);
        if (mResourceLayout == null) {
            mResourceLayout = (LinearLayout) mInflater.inflate(R.layout.view_preview_resource, null);
        }
        mResourceView = (RecyclerView) mResourceLayout.findViewById(R.id.preview_resource_list);
        GridLayoutManager manager = new GridLayoutManager(mActivity, 6);
        mResourceView.setLayoutManager(manager);
        PreviewResourceAdapter adapter = new PreviewResourceAdapter(mActivity);
        mResourceView.setAdapter(adapter);
        adapter.setOnResourceChangeListener(new PreviewResourceAdapter.OnResourceChangeListener() {
            @Override
            public void onResourceChanged(SmartResourceData resourceData) {
                parseResource(resourceData);
            }
        });
        mLayoutStickerContent.addView(mResourceLayout);
    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    /**
     * 解码资源
     */
    private void parseResource(@Nullable SmartResourceData data) {
        if (data.type == null) {
            return;
        }

        SmartBeautyResource.changeDynamicResource(mActivity, data);
    }

}
