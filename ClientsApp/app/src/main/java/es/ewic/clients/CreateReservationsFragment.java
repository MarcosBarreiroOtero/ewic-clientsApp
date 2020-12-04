package es.ewic.clients;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.ewic.clients.model.Client;
import es.ewic.clients.model.Reservation;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.DateUtils;
import es.ewic.clients.utils.FormUtils;
import es.ewic.clients.utils.ModelConverter;
import es.ewic.clients.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateReservationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateReservationsFragment extends Fragment {

    private static final String ARG_CLIENT = "client_data";
    private static final String ARG_SHOP = "shop_data";
    private static final String ARG_RSV = "reservation_data";

    private Client client;
    private Shop shop;
    private Reservation reservation;
    private JSONArray shopNames;

    OnCreateReservationListener mCallback;

    public interface OnCreateReservationListener {
        void onRsvCreate(Shop shop);

        void onRsvUpdate();
    }

    public CreateReservationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param shopData   shop data.
     * @param clientData client data.
     * @return A new instance of fragment CreateReservationsFragment.
     */
    public static CreateReservationsFragment newInstance(Client clientData, Shop shopData, Reservation reservation) {
        CreateReservationsFragment fragment = new CreateReservationsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLIENT, clientData);
        args.putSerializable(ARG_SHOP, shopData);
        args.putSerializable(ARG_RSV, reservation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (CreateReservationsFragment.OnCreateReservationListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getArguments() != null) {
            client = (Client) getArguments().getSerializable(ARG_CLIENT);
            shop = (Shop) getArguments().getSerializable(ARG_SHOP);
            reservation = (Reservation) getArguments().getSerializable(ARG_RSV);
        }

        if (reservation != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.update_reservation);
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.newReservation);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_create_reservations, container, false);

        // Shop
        AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
        if (reservation != null) {
            String[] shops = new String[]{reservation.getShopName()};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.shop_list_item, shops);
            act_shop.setAdapter(adapter);
            act_shop.setText(reservation.getShopName());
            act_shop.setEnabled(false);
        } else if (shop != null) {
            String[] shops = new String[]{shop.getName()};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.shop_list_item, shops);
            act_shop.setAdapter(adapter);
            act_shop.setText(shop.getName());
            act_shop.setEnabled(false);
        } else {
            getShopNames(parent);
        }

        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, 1);

        //Hour input
        TextInputEditText tiet_hour = parent.findViewById(R.id.reservation_hour_input);
        if (reservation != null) {
            tiet_hour.setText(DateUtils.formatHour(reservation.getDate()));
        } else {
            tiet_hour.setText(DateUtils.formatHour(now));
        }
        tiet_hour.setInputType(InputType.TYPE_NULL);
        tiet_hour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showHourPickerListener(tiet_hour);
                }
            }
        });
        tiet_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHourPickerListener(tiet_hour);
            }
        });

        //Date input
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        if (reservation != null) {
            tiet_date.setText(DateUtils.formatDate(reservation.getDate()));
        } else {
            tiet_date.setText(DateUtils.formatDate(now));
        }
        tiet_date.setInputType(InputType.TYPE_NULL);
        tiet_date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerListener(tiet_date);
                }
            }
        });
        tiet_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerListener(tiet_date);
            }
        });

        //Remarks
        if (reservation != null) {
            TextInputEditText tiet_remarks = parent.findViewById(R.id.reservation_remark_input);
            tiet_remarks.setText(reservation.getRemarks());
        }

        Button submitButton = parent.findViewById(R.id.reservation_button);
        if (reservation != null) {
            submitButton.setText(getString(R.string.update_reservation));
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reservation != null) {
                    editReservationForm(parent);
                } else {
                    createNewReservationForm(parent);
                }
            }
        });


        return parent;
    }

    private void getShopNames(ConstraintLayout parent) {

        String url = BackEndEndpoints.SHOP_NAMES;

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                shopNames = response;
                List<String> names = new ArrayList<>();
                for (int i = 0; i < shopNames.length(); i++) {
                    names.add(response.optJSONObject(i).optString("name"));
                }
                String[] shops = names.toArray(new String[names.size()]);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.shop_list_item, shops);
                AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
                act_shop.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "Error");
            }
        });

    }

    private void showHourPickerListener(TextInputEditText tiet_hour) {
        final Calendar date = DateUtils.parseDateHour(tiet_hour.getText().toString().trim());
        int hour = date.get(Calendar.HOUR_OF_DAY);
        int minutes = date.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar cal = Calendar.getInstance();
                cal.set(0, 0, 0, hourOfDay, minute);
                tiet_hour.setText(DateUtils.formatHour(cal));
            }
        }, hour, minutes, true);
        timePicker.show();
    }

    private void showDatePickerListener(TextInputEditText tiet_date) {
        final Calendar date = DateUtils.parseDateDate(tiet_date.getText().toString().trim());
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                tiet_date.setText(DateUtils.formatDate(cal));
            }
        }, year, month, day);
        datePicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        datePicker.show();
    }

    private boolean validateShop(AutoCompleteTextView act_shop) {
        String shop_input = act_shop.getText().toString().trim();
        if (shop_input.isEmpty()) {
            act_shop.setError(getString(R.string.error_empty_field));
            return false;
        } else {
            if (shopNames != null) {
                for (int i = 0; i < shopNames.length(); i++) {
                    JSONObject shopName = shopNames.optJSONObject(i);
                    if (shop_input.equals(shopName.optString("name"))) {
                        act_shop.setError(null);
                        return true;
                    }
                }
                act_shop.setError(getString(R.string.error_shop_not_found));
                return false;
            }
            act_shop.setError(null);
            return true;
        }
    }

    private boolean validateReservationDate(TextInputEditText tiet_date, TextInputEditText tiet_hour) {
        String dateInput = tiet_date.getText().toString().trim();
        boolean validDate = true;
        boolean validHour = true;
        if (dateInput.isEmpty()) {
            tiet_date.setError(getString(R.string.error_empty_field));
            validDate = false;
        }

        String hourInput = tiet_hour.getText().toString().trim();
        if (hourInput.isEmpty()) {
            tiet_hour.setError(getString(R.string.error_empty_field));
            validHour = false;
        }

        if (!validHour && !validDate) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        Calendar date = DateUtils.parseDateLong(hourInput + " " + dateInput);
        if (date == null) {
            tiet_hour.setError(getString(R.string.error_bad_format_hour));
            tiet_date.setError(getString(R.string.error_bad_format_date));
            return false;
        } else if (now.after(date)) {
            tiet_hour.setError(getString(R.string.error_past_reservation));
            return false;
        }

        tiet_date.setError(null);
        tiet_hour.setError(null);
        return true;
    }

    private void createNewReservationForm(ConstraintLayout parent) {

        AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        TextInputEditText tiet_hour = parent.findViewById(R.id.reservation_hour_input);

        if (validateShop(act_shop) & validateReservationDate(tiet_date, tiet_hour)) {

            ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.updating_data, R.string.please_wait);

            TextInputEditText tiet_remarks = parent.findViewById(R.id.reservation_remark_input);

            String remarksInput = tiet_remarks.getText().toString().trim();
            String dateInput = tiet_date.getText().toString().trim();
            String hourInput = tiet_hour.getText().toString().trim();
            String shopInput = act_shop.getText().toString().trim();
            Calendar date = DateUtils.parseDateLong(hourInput + " " + dateInput);
            int idShop = 0;
            if (shop != null) {
                idShop = shop.getIdShop();
            } else {
                for (int i = 0; i < shopNames.length(); i++) {
                    JSONObject shopName = shopNames.optJSONObject(i);
                    if (shopInput.equals(shopName.optString("name"))) {
                        idShop = shopName.optInt("idShop");
                    }
                }
            }
            Reservation rsv = new Reservation(date, remarksInput, client.getIdGoogleLogin(), idShop);

            String url = BackEndEndpoints.RESERVATION_BASE;
            JSONObject rsvJSON = ModelConverter.reservationToJsonObject(rsv);
            RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.POST, url, rsvJSON, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Snackbar.make(parent, getString(R.string.reservation_create_successfully), Snackbar.LENGTH_LONG)
                            .show();
                    pd.hide();
                    mCallback.onRsvCreate(shop);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HTTP", "error");
                    pd.hide();
                    Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            pd.show();
                            createNewReservationForm(parent);
                        }
                    });
                    snackbar.show();
                }
            });
        }
    }

    private void editReservationForm(ConstraintLayout parent) {

        AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        TextInputEditText tiet_hour = parent.findViewById(R.id.reservation_hour_input);

        if (validateShop(act_shop) & validateReservationDate(tiet_date, tiet_hour)) {

            ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.updating_data, R.string.please_wait);

            TextInputEditText tiet_remarks = parent.findViewById(R.id.reservation_remark_input);

            String remarksInput = tiet_remarks.getText().toString().trim();
            String dateInput = tiet_date.getText().toString().trim();
            String hourInput = tiet_hour.getText().toString().trim();

            Calendar date = DateUtils.parseDateLong(hourInput + " " + dateInput);
            reservation.setDate(date);
            reservation.setRemarks(remarksInput);

            String url = BackEndEndpoints.RESERVATION_BASE + "/" + reservation.getIdReservation();
            JSONObject rsvJSON = ModelConverter.reservationToJsonObject(reservation);
            RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.PUT, url, rsvJSON, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Snackbar.make(parent, getString(R.string.update_reservation_successfully), Snackbar.LENGTH_LONG)
                            .show();
                    pd.hide();
                    mCallback.onRsvUpdate();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HTTP", "error");
                    pd.hide();
                    Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            pd.show();
                            editReservationForm(parent);
                        }
                    });
                    snackbar.show();
                }
            });
        }

    }
}