package com.bdureau.simplemodeltracker.track;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.bdureau.simplemodeltracker.BuildConfig;
import com.bdureau.simplemodeltracker.ConsoleApplication;
import com.bdureau.simplemodeltracker.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.util.ArrayList;


public class TrackMapFragment extends Fragment {
    private static final String TAG = "TrackMapFragment";
    private boolean ViewCreated = false;

    private MapView mMap = null;
    private ConsoleApplication myBT;
    private Marker marker, markerDest;
    private Polyline polyline = null;
    private IMapController mapController = null;
    private TextView textViewdistance;

    public void setDistance(String value) {
        textViewdistance.setText(value);
    }

    public TrackMapFragment(ConsoleApplication pBT) {
        myBT = pBT;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().load(this.getContext(),
                PreferenceManager.getDefaultSharedPreferences(this.getContext()));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        View view = inflater.inflate(R.layout.fragment_track_open_map, container, false);

        textViewdistance = (TextView) view.findViewById(R.id.textViewdistance);

        mMap = (MapView) view.findViewById(R.id.mapOpenMap);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);

        marker = new Marker(mMap);
        marker.setIcon(getResources().getDrawable(R.drawable.ic_person_map));
        mMap.getOverlays().add(marker);


        markerDest = new Marker(mMap);
        if (myBT.getAppConf().getModelType() == 0) {
            markerDest.setIcon(getResources().getDrawable(R.drawable.ic_rocket_map));
        } else if(myBT.getAppConf().getModelType() == 1) {
            markerDest.setIcon(getResources().getDrawable(R.drawable.ic_boat_map));
        } else if(myBT.getAppConf().getModelType() == 2) {
            markerDest.setIcon(getResources().getDrawable(R.drawable.ic_car_map));
        } else if(myBT.getAppConf().getModelType() == 3) {
            markerDest.setIcon(getResources().getDrawable(R.drawable.ic_hot_air_balloon_map));
        } else if(myBT.getAppConf().getModelType() == 4) {
            markerDest.setIcon(getResources().getDrawable(R.drawable.ic_plane_map));
        }
        mMap.getOverlays().add(markerDest);

        polyline = new Polyline();
        polyline.setColor(myBT.getAppConf().ConvertColor(myBT.getAppConf().getMapColor()));
        polyline.setWidth(10);
        mMap.getOverlays().add(polyline);


        mapController = mMap.getController();
        mapController.setZoom(18.0);

        CompassOverlay compassOverlay = new CompassOverlay(this.getContext(), mMap);
        compassOverlay.enableCompass();
        mMap.getOverlays().add(compassOverlay);

        ViewCreated = true;
        return view;
    }

    public void updateMap(double latitude, double longitude, double rocketLatitude, double rocketLongitude) {
        GeoPoint dest = new GeoPoint(rocketLatitude, rocketLongitude);
        if (mMap != null) {
            //tabPage1
            GeoPoint latLng = new GeoPoint(latitude, longitude);
            mapController.setCenter(latLng);
            if (marker != null) {
                marker.setPosition(latLng);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            }
            if (markerDest != null) {
                markerDest.setPosition(dest);
            }
            try {
                if (polyline != null) {
                    ArrayList<GeoPoint> pathPoints = new ArrayList();
                    pathPoints.add(latLng);
                    pathPoints.add(dest);
                    if (pathPoints != null) {
                        polyline.setPoints(pathPoints);
                        if (mMap.getZoomLevelDouble() > 10.0) {
                            mapController.animateTo(latLng);
                        } else {
                            mapController.animateTo(latLng, 15.0, 10L);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(getView().getContext(),
                PreferenceManager.getDefaultSharedPreferences(getView().getContext()));
        if (mMap != null) {
            mMap.onResume();
        } else {
            Log.d(TAG, "mMap is null");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().save(getView().getContext(),
                PreferenceManager.getDefaultSharedPreferences(getView().getContext()));
        if (mMap != null) {
            mMap.onPause();
        }
    }
}