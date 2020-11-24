package es.ewic.clients.adapters;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.ewic.clients.R;
import es.ewic.clients.model.Reservation;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.DateUtils;

public class ReservationRowAdapter extends BaseAdapter implements ListAdapter {

    private final List<Reservation> reservationList;
    private final Fragment fragment;
    private final Resources resources;
    private final String packageName;

    public ReservationRowAdapter(List<Reservation> reservationList, Fragment fragment, Resources resources, String packageName) {
        assert reservationList != null;
        assert fragment != null;
        assert resources != null;
        assert packageName != null;

        this.reservationList = reservationList;
        this.fragment = fragment;
        this.resources = resources;
        this.packageName = packageName;
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
            shopId.setText(reservation.getShopName());

            Calendar date = reservation.getDate();
            reservationDate.setText(DateUtils.formatDateLong(date));

            reservationState.setText(resources.getIdentifier(reservation.getState(), "string", packageName));

            reservationRemarks.setText(reservation.getRemarks());
        }
        return convertView;
    }
}
