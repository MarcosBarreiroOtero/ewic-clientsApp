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

import java.util.List;

import es.ewic.clients.adapters.ShopRowAdapter;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.FragmentUtils;
import es.ewic.clients.utils.ModelConverter;
import es.ewic.clients.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopListFragment extends Fragment {

    private Double mLatitude;
    private Double mLongitude;

    private List<Shop> shops;

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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_shop_list, container, false);


        // Reload list when refresh
        SwipeRefreshLayout swipeRefreshLayout = parent.findViewById(R.id.swipeRefreshLayoutShops);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getLastLocation(parent, swipeRefreshLayout);

        });

        getLastLocation(parent, swipeRefreshLayout);

        ListView shopList = parent.findViewById(R.id.shop_list);
        shopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Shop shop = shops.get(position);
                FragmentUtils.getInstance().replaceFragment(getActivity().getSupportFragmentManager(), ShopInformationFragment.newInstance(shop), false);
            }
        });


        return parent;
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation(ConstraintLayout parent, SwipeRefreshLayout swipeRefreshLayout) {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.e("Position", "Ok.");
                    mLatitude = task.getResult().getLatitude();
                    mLongitude = task.getResult().getLongitude();
                } else {
                    Log.e("Position", "getLastLocation:exception - " + task.getException(), task.getException());
                }
                getShopList(parent, swipeRefreshLayout);
            }
        });
    }

    private void getShopList(ConstraintLayout parent, SwipeRefreshLayout swipeRefreshLayout) {
        String url = BackEndEndpoints.SHOP_BASE;

        if (mLatitude != null && mLongitude != null) {
            url += "?latitude=" + mLatitude + "&longitude=" + mLongitude;
        }

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, response -> {
            {
                Log.i("Shop list", response.toString());
                shops = ModelConverter.jsonArrayToShopList(response);
                ListView shopList = parent.findViewById(R.id.shop_list);
                ShopRowAdapter shopRowAdapter = new ShopRowAdapter(ShopListFragment.this, shops, getResources(), getActivity().getPackageName());
                shopList.setAdapter(shopRowAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }
        }, error -> Log.e("HTTP", "error"));
    }

}