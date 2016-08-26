package picmap.com.picmap.activities;


import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import picmap.com.picmap.R;
import picmap.com.picmap.models.ImageMarker;


/**
 * Created by Yur on 26/08/16.
 */
public class MapActivity extends FragmentActivity implements ClusterManager.OnClusterClickListener<ImageMarker>,OnMapReadyCallback, ClusterManager.OnClusterItemClickListener<ImageMarker> {

    private ClusterManager<ImageMarker> mClusterManager;

    protected GoogleMap mMap;
    private SupportMapFragment mapFragment;

    public static final int THAMBNAIL_SIZE=100;
    public List<ImageMarker> data =new ArrayList<>();
    public ArrayList<String> paths=new ArrayList<>();




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        // Obtain the SupportMapFragment and get notified when the map_layout is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        paths=getIntent().getStringArrayListExtra("data");

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null) {
            startDemo();
        }
    }

    protected GoogleMap getMap() {
        setUpMapIfNeeded();
        return mMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng yerevan = new LatLng(40.177200, 44.503490);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(yerevan));
    }


    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class ImageMarkerRenderer extends DefaultClusterRenderer<ImageMarker> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public ImageMarkerRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.cluster_view, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(ImageMarker person, MarkerOptions markerOptions) {
            mImageView.setImageBitmap(person.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<ImageMarker> cluster, MarkerOptions markerOptions) {
            mClusterImageView.setImageDrawable(getResources().getDrawable(R.drawable.bubble_mask));
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<ImageMarker> cluster) {
        double minLat = 0;
        double minLng = 0;
        double maxLat = 0;
        double maxLng = 0;
        for (ImageMarker p : cluster.getItems()) {
            double lat = p.getPosition().latitude;
            double lng = p.getPosition().longitude;
            if (minLat == 0 & minLng == 0 & maxLat == 0 & maxLng == 0) {
                minLat = maxLat = lat;
                minLng = maxLng = lng;
            }
            if (lat > maxLat) {
                maxLat = lat;
            }
            if (lng > maxLng) {
                maxLng = lng;
            }
            if (lat < minLat) {
                minLat = lat;
            }
            if (lng < minLng) {
                minLng = lng;
            }
        }

        LatLng sw = new LatLng(minLat, minLng);
        LatLng ne = new LatLng(maxLat, maxLng);
        LatLngBounds bounds = new LatLngBounds(sw, ne);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

        return true;
    }

    @Override
    public boolean onClusterItemClick(ImageMarker item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.780633, -122.396788), 14f));

        mClusterManager = new ClusterManager<ImageMarker>(this, getMap());
        mClusterManager.setRenderer(new ImageMarkerRenderer());
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);

        addItems();
        mClusterManager.cluster();
    }

    private void addItems() {
//        try{
//            InputStream inputStream = getResources().openRawResource(R.raw.coffees);
//            List<Coffee> items = new ImageMarkerReader().read(inputStream);
//            mClusterManager.addItems(items);
//        }catch(JSONException e){
//            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
//        }
   /*     data=getAllShownImagesPath(this);
        Log.d("MY LOG", data.toString());*/

        List<ImageMarker> items=new ArrayList<>();
//        double lat = 40.177200;
//        double lng = 44.503490;
//        int i=0;
        for (String s:paths) {

            float lat=ReadExiflong(s)[0];
            float lng=ReadExiflong(s)[1];

           /* double dist = i / 60d;
            lat = lat + dist;
            lng = lng + dist;
            i++;*/
            //Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.ic_cloud_download_black_24dp);
            /*String path="/storage/emulated/0/Pictures/PicsArt/PicsArt_08-19-11.13.07.jpg";*/
            Bitmap bitmap= null;

            try {
                bitmap = getThumbnail(Uri.parse("file://"+s));
            } catch (IOException e) {
                e.printStackTrace();
            }
            items.add(new ImageMarker(new LatLng(lat, lng), "Starbucks",bitmap));
        }


        mClusterManager.addItems(items);
    }


    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THAMBNAIL_SIZE) ? (originalSize / THAMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = MapActivity.this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    float[] ReadExiflong(String file) {
        float[] gpsCoords = new float[2];
        try {
            ExifInterface exifInterface = new ExifInterface(file);
            exifInterface.getLatLong(gpsCoords);
            // gpsCoords = Double.parseDouble(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));


            Toast.makeText(MapActivity.this,
                    "finished",
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(MapActivity.this,
                    e.toString(),
                    Toast.LENGTH_LONG).show();
        }
        return gpsCoords;
    }
}
