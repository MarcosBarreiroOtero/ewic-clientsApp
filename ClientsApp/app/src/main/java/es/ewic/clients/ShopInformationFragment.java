package es.ewic.clients;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ramijemli.percentagechartview.PercentageChartView;

import es.ewic.clients.model.Client;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.FormUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopInformationFragment extends Fragment implements OnMapReadyCallback {


    private static final String ARG_SHOP_INFORMATION = "shopInformation";

    OnShopInformationListener mCallback;

    private Shop shopInformation;
    private MapView mMapView;

    public interface OnShopInformationListener {
        void onBookShopEntry(Shop shopData);
    }

    public ShopInformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param shop shop Information;
     * @return A new instance of fragment ShopInformation.
     */
    // TODO: Rename and change types and number of parameters
    public static ShopInformationFragment newInstance(Shop shop) {
        ShopInformationFragment fragment = new ShopInformationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SHOP_INFORMATION, shop);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (ShopInformationFragment.OnShopInformationListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shopInformation = (Shop) getArguments().getSerializable(ARG_SHOP_INFORMATION);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_shop_information, container, false);

        TextView shop_type = parent.findViewById(R.id.shop_information_type);
        shop_type.setText(getString(getResources().getIdentifier(shopInformation.getType(), "string", getActivity().getPackageName())));

        TextView shop_location = parent.findViewById(R.id.shop_information_location);
        shop_location.setText(shopInformation.getLocation());

        PercentageChartView percentageChartView = parent.findViewById(R.id.shop_information_percentage);
        float percentage = ((float) shopInformation.getActualCapacity() / shopInformation.getMaxCapacity()) * 100;
        FormUtils.configureSemaphorePercentageChartView(getResources(), percentageChartView, percentage);
        TextView shop_capacity = parent.findViewById(R.id.shop_information_capacity);
        shop_capacity.setText(shopInformation.getActualCapacity() + "/" + shopInformation.getMaxCapacity());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        Button shop_information_book_entry = parent.findViewById(R.id.shop_information_book_entry);
        shop_information_book_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onBookShopEntry(shopInformation);
            }
        });
        return parent;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("Map", "Map ready");
        LatLng location = new LatLng(shopInformation.getLatitude(), shopInformation.getLongitude());

        googleMap.addMarker(new MarkerOptions().position(location).title(shopInformation.getName()));

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