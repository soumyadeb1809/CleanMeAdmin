package com.example.soumyadeb.cleanmeadmin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by Soumya Deb on 05-12-2017.
 */

public class Tools {

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth()*2, vectorDrawable.getIntrinsicHeight()*2);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth()*2, vectorDrawable.getIntrinsicHeight()*2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static String idModifier(String input){
                input = input.replaceAll("/", "-");

        return input;
    }

    public static int getZonePos(String id){
        int pos = -1;
        for(int i=0; i<HomeActivity.zoneList.size(); i++){
            if(HomeActivity.zoneList.get(i).getId().equals(id)){
                pos = i;
                break;
            }
        }

        return pos;
    }

}
