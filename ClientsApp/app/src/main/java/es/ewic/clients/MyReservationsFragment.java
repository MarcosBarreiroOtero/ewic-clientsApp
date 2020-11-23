package es.ewic.clients;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import java.util.List;

import es.ewic.clients.model.Client;
import es.ewic.clients.model.Reservation;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.ModelConverter;
import es.ewic.clients.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyReservationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyReservationsFragment extends Fragment {

    private static final String ARG_CLIENT = "client";

    private Client client;

    private List<Reservation> reservations;

    public MyReservationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param clientData client data
     * @return A new instance of fragment MyReservationsFragment.
     */
    public static MyReservationsFragment newInstance(Client clientData) {
        MyReservationsFragment fragment = new MyReservationsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLIENT, clientData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            client = (Client) getArguments().getSerializable(ARG_CLIENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_my_reservations, container, false);

        // Reload list when refresh
        SwipeRefreshLayout swipeRefreshLayout = parent.findViewById(R.id.swipeRefreshLayoutReservatios);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getReservations(parent);
            swipeRefreshLayout.setRefreshing(false);
        });

        getReservations(parent);
        return parent;
    }

    private void getReservations(ConstraintLayout parent) {

        String url = BackEndEndpoints.RESERVATION_BASE + "/" + client.getIdGoogleLogin();

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.e("HTTP", response.toString());
                reservations = ModelConverter.jsonArrayToReservationList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
            }
        });
    }
}