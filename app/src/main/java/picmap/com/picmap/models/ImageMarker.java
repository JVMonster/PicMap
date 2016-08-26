package picmap.com.picmap.models;


import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Yur on 26/08/16.
 */

public class ImageMarker implements ClusterItem {
    public String getName() {
        return name;
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    public Bitmap getProfilePhoto() {
        return profilePhoto;
    }

    public final String name;
    public final Bitmap profilePhoto;
    private final LatLng mPosition;

    public ImageMarker(LatLng position, String name,Bitmap pictureResource) {
        this.name = name;
        profilePhoto = pictureResource;
        mPosition = position;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}