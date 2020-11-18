package es.ewic.clients;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopInformationFragment extends Fragment implements OnMapReadyCallback {


    private static final String ARG_SHOP_INFORMATION = "shopInformation";

    private JSONObject shopInformation;
    private MapView mMapView;

    public ShopInformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param shopInformation Parameter 1.
     * @return A new instance of fragment ShopInformation.
     */
    // TODO: Rename and change types and number of parameters
    public static ShopInformationFragment newInstance(JSONObject shopInformation) {
        ShopInformationFragment fragment = new ShopInformationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SHOP_INFORMATION, shopInformation.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                shopInformation = new JSONObject(getArguments().getString(ARG_SHOP_INFORMATION));
            } catch (JSONException e) {
                shopInformation = null;
            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_shop_information, container, false);


        TextView shop_type = parent.findViewById(R.id.shop_information_type);
        shop_type.setText(getString(getResources().getIdentifier(shopInformation.optString("type"), "string", getActivity().getPackageName())));

        TextView shop_location = parent.findViewById(R.id.shop_information_location);
        shop_location.setText(shopInformation.optString("location"));

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        return parent;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("Map", "Map ready");
        LatLng location = new LatLng(shopInformation.optDouble("latitude"), shopInformation.optDouble("longitude"));

        googleMap.addMarker(new MarkerOptions().position(location).title(shopInformation.optString("name")));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        googleMap.setMinZoomPreference(15);

        mapEnableMyPosition(googleMap);

//        map:ambientEnabled="true"
//        map:cameraMaxZoomPreference="1.0"
//        map:cameraMinZoomPreference="0.0"
//        map:liteMode="true"
//        map:uiCompass="true"
//        map:uiMapToolbar="true"
//        map:uiRotateGestures="true"
//        map:uiScrollGestures="true"
//        map:uiTiltGestures="true"
//        map:uiZoomControls="true"
//        map:uiZoomGestures="true"
//        map:zOrderOnTop="true"
    }

    @SuppressLint("MissingPermission")
    private void mapEnableMyPosition(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
    }
}