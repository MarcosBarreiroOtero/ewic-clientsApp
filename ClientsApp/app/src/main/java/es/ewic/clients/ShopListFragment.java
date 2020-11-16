package es.ewic.clients;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import es.ewic.clients.adapters.ShopRowAdapter;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopListFragment extends Fragment {

    private Double mLatitude;
    private Double mLongitude;

    private FusedLocationProviderClient mFusedLocationClient;

    public ShopListFragment() {
        // Required empty public constructor
    }

    public static ShopListFragment newInstance() {
        return new ShopListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_shop_list, container, false);

        getLastLocation(parent);

        // Reload list when refresh
        SwipeRefreshLayout swipeRefreshLayout = parent.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getLastLocation(parent);
            swipeRefreshLayout.setRefreshing(false);
        });

        ListView shopList = parent.findViewById(R.id.shop_list);
        shopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });


        return parent;
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation(ConstraintLayout parent) {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLatitude = task.getResult().getLatitude();
                            mLongitude = task.getResult().getLongitude();
                        } else {
                            Log.w("Position", "getLastLocation:exception", task.getException());
                        }
                        getShopList(parent);
                    }
                });
    }

    private void getShopList(ConstraintLayout parent) {
        String url = BackEndEndpoints.SHOP_BASE;

        if (mLatitude != null && mLongitude != null) {
            url += "?latitude=" + mLatitude + "&longitude=" + mLongitude;
        }

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, response -> {
            {
                Log.i("Shop list", response.toString());
                ListView shopList = parent.findViewById(R.id.shop_list);
                ShopRowAdapter shopRowAdapter = new ShopRowAdapter(ShopListFragment.this, response, getResources(), getActivity().getPackageName());
                shopList.setAdapter(shopRowAdapter);
            }
        }, error -> Log.e("HTTP", "error"));
    }

}