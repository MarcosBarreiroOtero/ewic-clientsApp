package es.ewic.clients;

import android.content.Context;
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
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

    OnMyReservationsListener mCallback;

    public interface OnMyReservationsListener {
        void onCreateNewRsv();
    }

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (MyReservationsFragment.OnMyReservationsListener) getActivity();
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
                if (!reservationRemarks.getText().toString().trim().equals("")) {
                    if (reservationRemarks.getVisibility() == View.GONE) {
                        reservationRemarks.setVisibility(View.VISIBLE);
                    } else {
                        reservationRemarks.setVisibility(View.GONE);
                    }
                }

            }
        });

        FloatingActionButton add_reservation = parent.findViewById(R.id.add_reservation);
        add_reservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onCreateNewRsv();
            }
        });
        return parent;
    }

    private void getReservations(ConstraintLayout parent, SwipeRefreshLayout swipeRefreshLayout) {

        String url = BackEndEndpoints.RESERVATION_BASE + "/" + client.getIdGoogleLogin();

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                reservations = ModelConverter.jsonArrayToReservationList(response);
                ListView reservationList = parent.findViewById(R.id.reservation_list);
                ReservationRowAdapter reservationRowAdapter = new ReservationRowAdapter(reservations, client, MyReservationsFragment.this, getResources(), getActivity().getPackageName());
                reservationList.setAdapter(reservationRowAdapter);
                swipeRefreshLayout.setRefreshing(false);
                TextView reservations_not_found = parent.findViewById(R.id.reservations_not_found);
                if (reservations.isEmpty()) {
                    reservations_not_found.setVisibility(View.VISIBLE);
                    reservationList.setVisibility(View.GONE);
                } else {
                    reservations_not_found.setVisibility(View.GONE);
                    reservationList.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
                swipeRefreshLayout.setRefreshing(false);
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        swipeRefreshLayout.setRefreshing(true);
                        getReservations(parent, swipeRefreshLayout);
                    }
                });
                snackbar.show();
            }
        });
    }
}