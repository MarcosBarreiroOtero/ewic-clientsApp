package es.ewic.clients;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONArray;

import java.util.List;

import es.ewic.clients.adapters.ReservationRowAdapter;
import es.ewic.clients.model.Client;
import es.ewic.clients.model.Reservation;
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
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_my_reservations, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.toolbar_menu_my_reservations);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Reload list when refresh
        SwipeRefreshLayout swipeRefreshLayout = parent.findViewById(R.id.swipeRefreshLayoutReservatios);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getReservations(parent, swipeRefreshLayout);
        });

        getReservations(parent, swipeRefreshLayout);

        ListView reservationList = parent.findViewById(R.id.reservation_list);
        reservationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView reservationRemarks = view.findViewById(R.id.reservation_remarks);
                if (reservationRemarks.getVisibility() == View.GONE) {
                    reservationRemarks.setVisibility(View.VISIBLE);
                } else {
                    reservationRemarks.setVisibility(View.GONE);
                }

            }
        });
        return parent;
    }

    private void getReservations(ConstraintLayout parent, SwipeRefreshLayout swipeRefreshLayout) {

        String url = BackEndEndpoints.RESERVATION_BASE + "/" + client.getIdGoogleLogin();

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.e("HTTP", response.toString());
                reservations = ModelConverter.jsonArrayToReservationList(response);
                ListView reservationList = parent.findViewById(R.id.reservation_list);
                ReservationRowAdapter reservationRowAdapter = new ReservationRowAdapter(reservations, client, MyReservationsFragment.this, getResources(), getActivity().getPackageName());
                reservationList.setAdapter(reservationRowAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }
        }, error -> Log.e("HTTP", "error"));
    }
}