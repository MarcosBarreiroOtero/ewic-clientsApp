package es.ewic.clients.adapters;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;

import es.ewic.clients.R;
import es.ewic.clients.model.Client;
import es.ewic.clients.model.Reservation;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.DateUtils;
import es.ewic.clients.utils.RequestUtils;

public class ReservationRowAdapter extends BaseAdapter implements ListAdapter {

    private final List<Reservation> reservationList;
    private final Client client;
    private final Fragment fragment;
    private final Resources resources;
    private final String packageName;

    private final OnEditReservationListener mCallback;

    public interface OnEditReservationListener {
        void editReservation(Reservation reservation);
    }


    public ReservationRowAdapter(List<Reservation> reservationList, Client client, Fragment fragment, Resources resources, String packageName) {
        assert reservationList != null;
        assert client != null;
        assert fragment != null;
        assert resources != null;
        assert packageName != null;

        this.reservationList = reservationList;
        this.client = client;
        this.fragment = fragment;
        this.resources = resources;
        this.packageName = packageName;

        mCallback = (ReservationRowAdapter.OnEditReservationListener) fragment.getActivity();
    }

    @Override
    public int getCount() {
        if (reservationList == null) {
            return 0;
        } else {
            return reservationList.size();
        }
    }

    @Override
    public Reservation getItem(int position) {
        if (reservationList == null) {
            return null;
        } else {
            return reservationList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        Reservation rsv = getItem(position);
        return rsv.getIdReservation();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = fragment.getLayoutInflater().inflate(R.layout.reservation_row, null);
        }

        TextView shopId = convertView.findViewById(R.id.reservation_shop_title);
        TextView reservationDate = convertView.findViewById(R.id.reservation_date);
        TextView reservationState = convertView.findViewById(R.id.reservation_state);
        TextView reservationRemarks = convertView.findViewById(R.id.reservation_remarks);

        Reservation reservation = getItem(position);

        if (reservation != null) {
            shopId.setText(reservation.getShopName() + " (" + reservation.getnClients() + " " + (reservation.getnClients() == 1 ? resources.getString(R.string.client) : resources.getString(R.string.clients)) + ")");

            Calendar date = reservation.getDate();
            reservationDate.setText(DateUtils.formatDateLong(date));

            reservationState.setText(resources.getIdentifier(reservation.getState(), "string", packageName));

            reservationRemarks.setText(reservation.getRemarks());

            final View view = convertView;
            ImageButton reservation_edit_button = convertView.findViewById(R.id.reservation_edit_button);
            reservation_edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.editReservation(reservation);
                }
            });

            ImageButton reservation_cancel_button = convertView.findViewById(R.id.reservation_cancel_button);
            reservation_cancel_button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showPreCancelDialog(view, reservation);
                }
            });

            switch (reservation.getState()) {
                case "ACTIVE":
                    reservationState.setTextColor(resources.getColor(R.color.semaphore_green, null));
                    break;
                case "WAITING":
                    reservationState.setTextColor(resources.getColor(R.color.semaphore_green, null));
                    reservation_edit_button.setVisibility(View.GONE);
                    break;
                case "COMPLETED":
                    reservation_edit_button.setVisibility(View.GONE);
                    reservation_cancel_button.setVisibility(View.GONE);
                    break;
                case "NOT_APPEAR":
                case "CANCELLED":
                    reservationState.setTextColor(resources.getColor(R.color.semaphore_red, null));
                    reservation_edit_button.setVisibility(View.GONE);
                    reservation_cancel_button.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }

            Animation animation = AnimationUtils.loadAnimation(convertView.getContext(), R.anim.slide_in);
            animation.setDuration(300 + (position * 100));
            convertView.startAnimation(animation);
        }
        return convertView;
    }

    private void showPreCancelDialog(View view, Reservation rsv) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext()).setTitle(R.string.warning).setMessage(R.string.pre_cancel_reservation_message);

        builder.setPositiveButton(R.string.accept, (dialog, which) -> cancelReservation(view, rsv));

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            // dialog cancelled;
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.semaphore_red, null));
            }
        });
        dialog.show();
    }

    private void cancelReservation(View view, Reservation rsv) {

        String url = BackEndEndpoints.RESERVATION_BASE + "/" + rsv.getIdReservation() + "?idGoogleLogin=" + client.getIdGoogleLogin();

        RequestUtils.sendStringRequest(fragment.getContext(), Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                TextView reservationState = view.findViewById(R.id.reservation_state);
                reservationState.setText(resources.getString(R.string.CANCELLED));
                reservationState.setTextColor(resources.getColor(R.color.semaphore_red, null));
                ImageButton reservation_edit_button = view.findViewById(R.id.reservation_edit_button);
                reservation_edit_button.setVisibility(View.GONE);
                ImageButton reservation_cancel_button = view.findViewById(R.id.reservation_cancel_button);
                reservation_cancel_button.setVisibility(View.GONE);

                Snackbar.make(view, resources.getString(R.string.update_data_successfully), Snackbar.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
                if (error instanceof TimeoutError) {
                    Snackbar snackbar = Snackbar.make(view, resources.getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            showPreCancelDialog(view, rsv);
                        }
                    });
                    snackbar.show();
                } else {
                    int responseCode = RequestUtils.getErrorCodeRequest(error);
                    // 404 rsv or client not found (should not happen)
                    // 401 rsv not mutable
                    String message = "";
                    switch (responseCode) {
                        case 401:
                            message = resources.getString(R.string.error_rsv_not_mutable_delete);
                            ImageButton reservation_edit_button = view.findViewById(R.id.reservation_edit_button);
                            reservation_edit_button.setVisibility(View.GONE);
                            ImageButton reservation_cancel_button = view.findViewById(R.id.reservation_cancel_button);
                            reservation_cancel_button.setVisibility(View.GONE);
                            break;
                        default:
                            break;
                    }

                    if (message.isEmpty()) {
                        Snackbar snackbar = Snackbar.make(view, resources.getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                showPreCancelDialog(view, rsv);
                            }
                        });
                        snackbar.show();
                    } else {
                        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
