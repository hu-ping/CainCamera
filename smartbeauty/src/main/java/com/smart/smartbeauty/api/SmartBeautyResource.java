package com.smart.smartbeauty.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filterlibrary.glfilter.resource.FilterHelper;
import com.cgfay.filterlibrary.glfilter.resource.MakeupHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.utilslibrary.utils.BitmapUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by deepglint on 2019/3/7.
 */

public class SmartBeautyResource {
    private static final String TAG = "SmartBeautyResource";
    // 资源列表(贴纸啥的)
    private static  List<ResourceData> mResourceList = null;

    // 滤镜列表
    private static  List<ResourceData> mFilterList = null;

    // 彩妆列表
    private static  List<ResourceData> mMakeupList = new ArrayList<>();

    public static void initResources(Context context) {
        ResourceHelper.initAssetsResource(context);
        FilterHelper.initAssetsFilter(context);
        MakeupHelper.initAssetsMakeup(context);

        mResourceList = ResourceHelper.getResourceList();
        mFilterList = FilterHelper.getFilterList();
        mMakeupList = MakeupHelper.getMakeupList();
    }

    public static Bitmap getResourceImageBitmap(Context context, int position) {
        ResourceData resource = mResourceList.get(position);

        // 如果是asset下面的，则直接解码
        if (!TextUtils.isEmpty(resource.thumbPath) && resource.thumbPath.startsWith("assets://")) {
            return BitmapUtils.getImageFromAssetsFile(context,
                    resource.thumbPath.substring("assets://".length()));
        }

        return null;
    }
    
    public static Bitmap getFilterImageBitmap(Context context, int position) {
        if (mFilterList.get(position).thumbPath.startsWith("assets://")) {
           return BitmapUtils.getImageFromAssetsFile(context,
                    mFilterList.get(position).thumbPath.substring("assets://".length()));
        } else {
            return BitmapUtils.getBitmapFromFile(mFilterList.get(position).thumbPath);
        }
        
    }

    public static SmartResourceData getResourceData(int position) {
        ResourceData data =  mResourceList.get(position);
        return toSmartResourceData(data);
    }

    public static SmartResourceData getFilterListData(int position) {
        ResourceData data =  mFilterList.get(position);
        return toSmartResourceData(data);
    }

    public static int getResourceListDataSize(){
        return mResourceList.size();
    }

    public static int getFilterListDataSize(){
        return mFilterList.size();
    }


    public static void changeDynamicResource(Context context, SmartResourceData data) {
        try {
            switch (data.type) {
                // 单纯的滤镜
                case FILTER: {
                    String folderPath = ResourceHelper.getResourceDirectory(context) + File.separator + data.unzipFolder;
                    DynamicColor color = ResourceJsonCodec.decodeFilterData(folderPath);
                    SmartBeautyRender.getInstance().changeDynamicResource(color);
                    break;
                }

                // 贴纸
                case STICKER: {
                    String folderPath = ResourceHelper.getResourceDirectory(context) + File.separator + data.unzipFolder;
                    DynamicSticker sticker = ResourceJsonCodec.decodeStickerData(folderPath);
                    SmartBeautyRender.getInstance().changeDynamicResource(sticker);
                    break;
                }

                // TODO 多种结果混合
                case MULTI: {
                    break;
                }

                // 所有数据均为空
                case NONE: {
                    SmartBeautyRender.getInstance().changeDynamicResource((DynamicSticker) null);
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "parseResource: ", e);
        }
    }


    public static void changeDynamicMakeup(Context context, int position, String makeupName) {
        if (position == 0) {
            String folderPath = MakeupHelper.getMakeupDirectory(context) + File.separator +
                    MakeupHelper.getMakeupList().get(1).unzipFolder;
            DynamicMakeup makeup = null;
            try {
                makeup = ResourceJsonCodec.decodeMakeupData(folderPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            SmartBeautyRender.getInstance().changeDynamicMakeup(makeup);
        } else {
            SmartBeautyRender.getInstance().changeDynamicMakeup(null);
        }
    }

    public static void changeDynamicFilter(Context context, SmartResourceData data) {
        if (!data.name.equals("none")) {
            String folderPath = FilterHelper.getFilterDirectory(context) + File.separator + data.unzipFolder;
            DynamicColor color = null;
            try {
                color = ResourceJsonCodec.decodeFilterData(folderPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            SmartBeautyRender.getInstance().changeDynamicFilter(color);
        } else {
            SmartBeautyRender.getInstance().changeDynamicFilter(null);
        }
    }

    public static void saveBitmap(String filePath, ByteBuffer buffer, int width, int height) {
        BitmapUtils.saveBitmap(filePath,buffer,width, height);
    }



    private static ResourceData toResourceData(SmartResourceData data) {
        ResourceData temp = new ResourceData(null, null, null, null, null);

        temp.name = data.name;         // 名称
        temp.zipPath = data.zipPath;      // 压缩包路径，绝对路径，"assets://" 或 "file://"开头

        switch (data.type) {
            case NONE:
                temp.type =  ResourceType.NONE;   // 资源类型
                break;

            case STICKER:
                temp.type =  ResourceType.STICKER;
                break;
            case FILTER:
                temp.type =  ResourceType.FILTER;
                break;
            case EFFECT:
                temp.type =  ResourceType.EFFECT;
                break;
            case MAKEUP:
                temp.type =  ResourceType.MAKEUP;
                break;
            case MULTI:
                temp.type =  ResourceType.MULTI;
                break;
        }

        temp.unzipFolder = data.unzipFolder;  // 解压文件夹名称
        temp.thumbPath = data.thumbPath;

        return temp;
    }


    private static SmartResourceData toSmartResourceData(ResourceData data) {
        SmartResourceData temp = new SmartResourceData(null, null, null, null, null);

        temp.name = data.name;         // 名称
        temp.zipPath = data.zipPath;      // 压缩包路径，绝对路径，"assets://" 或 "file://"开头

        switch (data.type) {
            case NONE:
                temp.type =  SmartResourceType.NONE;   // 资源类型
                break;

            case STICKER:
                temp.type =  SmartResourceType.STICKER;
                break;
            case FILTER:
                temp.type =  SmartResourceType.FILTER;
                break;
            case EFFECT:
                temp.type =  SmartResourceType.EFFECT;
                break;
            case MAKEUP:
                temp.type =  SmartResourceType.MAKEUP;
                break;
            case MULTI:
                temp.type =  SmartResourceType.MULTI;
                break;
        }

        temp.unzipFolder = data.unzipFolder;  // 解压文件夹名称
        temp.thumbPath = data.thumbPath;

        return temp;
    }

}
