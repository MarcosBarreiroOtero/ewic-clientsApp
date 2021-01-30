package es.ewic.clients;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import es.ewic.clients.adapters.DialogFilterShop;
import es.ewic.clients.adapters.ShopRowAdapter;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.ModelConverter;
import es.ewic.clients.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopListFragment extends Fragment {

    private static final String ARG_SHOP_NAME = "shop_name";
    private static final String ARG_SHOP_TYPE = "shop_type";
    private static final String ARG_USE_LOCATION = "use_location";


    private Double mLatitude;
    private Double mLongitude;
    private String shop_name;
    private String shop_type;
    private boolean use_location;
    private List<Shop> shops;
    private FusedLocationProviderClient mFusedLocationClient;

    OnShopListListener mCallback;

    public interface OnShopListListener {
        void onClickShop(Shop shop);
    }

    public ShopListFragment() {
        // Required empty public constructor
    }

    public static ShopListFragment newInstance(String shop_name, String shop_type, Boolean use_location) {
        ShopListFragment fragment = new ShopListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SHOP_NAME, shop_name);
        args.putString(ARG_SHOP_TYPE, shop_type);
        args.putBoolean(ARG_USE_LOCATION, use_location);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (ShopListFragment.OnShopListListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (getArguments() != null) {
            shop_name = getArguments().getString(ARG_SHOP_NAME);
            shop_type = getArguments().getString(ARG_SHOP_TYPE);
            use_location = getArguments().getBoolean(ARG_USE_LOCATION);
        }
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
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ShopListFragment.this.getLastLocation(parent, swipeRefreshLayout);

            }
        });

        getLastLocation(parent, swipeRefreshLayout);

        ListView shopList = parent.findViewById(R.id.shop_list);
        shopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Shop shop = shops.get(position);
                mCallback.onClickShop(shop);
            }
        });

        // Filter button
        FloatingActionButton filter_shop = parent.findViewById(R.id.filter_search_button);
        filter_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog(parent, inflater);
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
                    mLatitude = task.getResult().getLatitude();
                    mLongitude = task.getResult().getLongitude();
                }
                getShopList(parent, swipeRefreshLayout);
            }
        });
    }

    private void getShopList(ConstraintLayout parent, SwipeRefreshLayout swipeRefreshLayout) {
        String url = BackEndEndpoints.SHOP_BASE;

        String params = "";
        if (use_location) {
            if (mLatitude != null && mLongitude != null) {
                params += (params.isEmpty() ? "?" : "&") + "latitude=" + mLatitude + "&longitude=" + mLongitude;
            }
        }

        if (shop_name != null) {
            params += (params.isEmpty() ? "?" : "&") + "name=" + shop_name;
        }

        if (shop_type != null) {
            params += (params.isEmpty() ? "?" : "&") + "shopType=" + shop_type;
        }

        url += params;


        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, response -> {
            {
                shops = ModelConverter.jsonArrayToShopList(response);
                ListView shopList = parent.findViewById(R.id.shop_list);
                ShopRowAdapter shopRowAdapter = new ShopRowAdapter(ShopListFragment.this, shops, getResources(), getActivity().getPackageName());
                shopList.setAdapter(shopRowAdapter);
                swipeRefreshLayout.setRefreshing(false);
                TextView shops_not_found = parent.findViewById(R.id.shops_not_found);
                if (shops.isEmpty()) {
                    shopList.setVisibility(View.GONE);
                    shops_not_found.setVisibility(View.VISIBLE);
                } else {
                    shopList.setVisibility(View.VISIBLE);
                    shops_not_found.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
                swipeRefreshLayout.setRefreshing(false);
                ListView shopList = parent.findViewById(R.id.shop_list);
                ShopRowAdapter shopRowAdapter = new ShopRowAdapter(ShopListFragment.this, new ArrayList<>(), getResources(), getActivity().getPackageName());
                shopList.setAdapter(shopRowAdapter);
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                swipeRefreshLayout.setEnabled(false);
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        swipeRefreshLayout.setEnabled(true);
                        swipeRefreshLayout.setRefreshing(true);
                        getShopList(parent, swipeRefreshLayout);
                    }
                });
                snackbar.show();
            }
        });
    }

    private void showFilterDialog(ConstraintLayout parent, LayoutInflater inflater) {
        DialogFragment newFragment = DialogFilterShop.newInstance(shop_name, shop_type, use_location, true);
        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

}