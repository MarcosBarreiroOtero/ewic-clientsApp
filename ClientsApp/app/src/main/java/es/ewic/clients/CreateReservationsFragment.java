package es.ewic.clients;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
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
    private JSONArray timetable;

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
        timetable = new JSONArray();
    }

    @Override
    public void onStart() {
        super.onStart();
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


        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, 1);

        //Hour input
        AutoCompleteTextView act_hour = parent.findViewById(R.id.reservation_hour_input);
        if (reservation != null) {
            act_hour.setText(DateUtils.formatHour(reservation.getDate()));
        } else {
            if (shop != null) {
                timetable = new JSONArray();
                try {
                    timetable = new JSONArray(shop.getTimetable());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setAdapterHourInput(parent, now, timetable);
        }


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
                    showDatePickerListener(parent, tiet_date);
                }
            }
        });
        tiet_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerListener(parent, tiet_date);
            }
        });

        // Shop
        AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
        if (reservation != null) {
            String[] shops = new String[]{reservation.getShopName()};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.shop_list_item, shops);
            act_shop.setAdapter(adapter);
            act_shop.setText(reservation.getShopName());
            act_shop.setEnabled(false);

            getShopCalendar(parent, reservation.getDate());
        } else if (shop != null) {
            String[] shops = new String[]{shop.getName()};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.shop_list_item, shops);
            act_shop.setAdapter(adapter);
            act_shop.setText(shop.getName());
            act_shop.setEnabled(false);
        } else {
            getShopNames(parent);
            act_shop.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        if (validateShop(act_shop)) {
                            String shopInput = act_shop.getText().toString().trim();
                            for (int i = 0; i < shopNames.length(); i++) {
                                JSONObject shopName = shopNames.optJSONObject(i);
                                if (shopInput.equals(shopName.optString("name"))) {
                                    String timetableString = shopName.optString("timetable");
                                    if (timetableString != null && !timetableString.equals("null")) {
                                        try {
                                            timetable = new JSONArray(timetableString);
                                        } catch (JSONException e) {
                                            timetable = new JSONArray();
                                        }
                                    } else {
                                        timetable = new JSONArray();
                                    }
                                }
                            }
                        } else {
                            timetable = new JSONArray();
                        }
                        setAdapterHourInput(parent, DateUtils.parseDateDate(tiet_date.getText().toString().trim()), timetable);
                        act_hour.setText("");
                    }
                }
            });
        }

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

    private List<String> getHoursBetweenRanges(Calendar start, Calendar end) {
        List<String> hours = new ArrayList<>();
        while (start.before(end)) {
            hours.add(DateUtils.formatHour(start));
            start.add(Calendar.MINUTE, 15);
        }

        return hours;

    }

    private void setAdapterHourInput(ConstraintLayout parent, Calendar date, JSONArray timetable) {
        List<String> hours = new ArrayList<>();
        Log.e("TIMETABLE", timetable.toString());
        if (timetable.length() == 0) {
            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DATE, 1);

            hours = getHoursBetweenRanges(start, end);

        } else {
            int weekDay = date.get(Calendar.DAY_OF_WEEK);
            if (weekDay == 1) {
                weekDay = 6;
            } else {
                weekDay -= 2;
            }
            for (int i = 0; i < timetable.length(); i++) {
                JSONObject weekDayTimetable = timetable.optJSONObject(i);
                if (weekDay == weekDayTimetable.optInt("weekDay")) {
                    try {
                        Calendar startMorning = DateUtils.parseDateHour(weekDayTimetable.getString("startMorning"));
                        Calendar endMorning = DateUtils.parseDateHour(weekDayTimetable.getString("endMorning"));
                        hours.addAll(getHoursBetweenRanges(startMorning, endMorning));
                    } catch (JSONException e) {
                        // no timetable morning
                    }

                    try {
                        Calendar startAfternoon = DateUtils.parseDateHour(weekDayTimetable.getString("startAfternoon"));
                        Calendar endAfternoon = DateUtils.parseDateHour(weekDayTimetable.getString("endAfternoon"));
                        hours.addAll(getHoursBetweenRanges(startAfternoon, endAfternoon));
                    } catch (JSONException e) {
                        // no timetable afternoon
                    }
                }
            }
        }

        String[] hoursValues = hours.toArray(new String[hours.size()]);
        AutoCompleteTextView act_hours = parent.findViewById(R.id.reservation_hour_input);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.shop_list_item, hours);
        act_hours.setAdapter(adapter);
        if (reservation != null) {
            act_hours.setText(DateUtils.formatHour(reservation.getDate()));
        } else {
            act_hours.setText("");
        }

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

    private void getShopCalendar(ConstraintLayout parent, Calendar date) {
        if (reservation != null) {
            String url = BackEndEndpoints.SHOP_TIMETABLE + "/" + reservation.getIdShop();
            RequestUtils.sendStringRequest(getContext(), Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response != null && !response.equals("null")) {
                        try {
                            timetable = new JSONArray(response);
                        } catch (JSONException e) {
                            timetable = new JSONArray();
                        }
                    } else {
                        timetable = new JSONArray();
                    }

                    setAdapterHourInput(parent, date, timetable);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HTTP", "Error");
                }
            });
        }

    }

    private void showDatePickerListener(ConstraintLayout parent, TextInputEditText tiet_date) {
        final Calendar date = DateUtils.parseDateDate(tiet_date.getText().toString().trim());
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                int weekDay = cal.get(Calendar.DAY_OF_WEEK);
                if (weekDay == 1) {
                    weekDay = 6;
                } else {
                    weekDay -= 2;
                }
                if (timetable.length() != 0) {
                    boolean closed = true;
                    for (int i = 0; i < timetable.length(); i++) {
                        JSONObject weekDayTimetable = timetable.optJSONObject(i);
                        if (weekDay == weekDayTimetable.optInt("weekDay")) {
                            closed = false;
                        }
                    }
                    if (closed) {
                        Snackbar.make(parent, getString(R.string.error_shop_closed_weekDay), Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }
                }
                tiet_date.setText(DateUtils.formatDate(cal));
                setAdapterHourInput(parent, cal, timetable);
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
            ArrayList<String> results = new ArrayList<>();
            ListAdapter adapter = act_shop.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                results.add((String) adapter.getItem(i));
            }
            if (results.size() == 0 ||
                    results.indexOf(shop_input) == -1) {
                act_shop.setError(getString(R.string.error_shop_not_found));
                return false;
            }
        }
        act_shop.setError(null);
        return true;

    }

    private boolean validateReservationDate(TextInputEditText tiet_date, AutoCompleteTextView act_hour) {
        String dateInput = tiet_date.getText().toString().trim();
        boolean validDate = true;
        boolean validHour = true;
        if (dateInput.isEmpty()) {
            tiet_date.setError(getString(R.string.error_empty_field));
            validDate = false;
        }

        String hourInput = act_hour.getText().toString().trim();
        if (hourInput.isEmpty()) {
            act_hour.setError(getString(R.string.error_empty_field));
            validHour = false;
        } else {
            ArrayList<String> results = new ArrayList<>();
            ListAdapter adapter = act_hour.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                results.add((String) adapter.getItem(i));
            }
            if (results.size() == 0 ||
                    results.indexOf(hourInput) == -1) {
                act_hour.setError(getString(R.string.error_hour_invalid));
                act_hour.setText("");
                act_hour.requestFocus();
                validHour = false;
            }
        }
        if (!validHour && !validDate) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        Calendar date = DateUtils.parseDateLong(hourInput + " " + dateInput);
        if (date == null) {
            act_hour.setError(getString(R.string.error_bad_format_hour));
            tiet_date.setError(getString(R.string.error_bad_format_date));
            return false;
        } else if (now.after(date)) {
            act_hour.setError(getString(R.string.error_past_reservation));
            act_hour.setText("");
            act_hour.requestFocus();
            return false;
        }

        tiet_date.setError(null);
        act_hour.setError(null);
        return true;
    }

    private void createNewReservationForm(ConstraintLayout parent) {

        AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        AutoCompleteTextView act_hour = parent.findViewById(R.id.reservation_hour_input);

        if (validateShop(act_shop) & validateReservationDate(tiet_date, act_hour)) {

            ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.updating_data, R.string.please_wait);

            TextInputEditText tiet_remarks = parent.findViewById(R.id.reservation_remark_input);

            String remarksInput = tiet_remarks.getText().toString().trim();
            String dateInput = tiet_date.getText().toString().trim();
            String hourInput = act_hour.getText().toString().trim();
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
                    if (error instanceof TimeoutError) {
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
                    } else {
                        int responseCode = RequestUtils.getErrorCodeRequest(error);
                        // 400 rsv duplicate
                        // 404 client, shop not found: should not happen
                        // 401 rsv unathorized: rsv in past or shop full
                        String message = "";
                        String errorMessage = RequestUtils.getErrorMessageRequest(error);
                        switch (responseCode) {
                            case 400:
                                message = getString(R.string.error_rsv_duplicate);
                                break;
                            case 401:
                                if (errorMessage.contains(RequestUtils.RESERVATION_WHEN_SHOP_FULL)) {
                                    message = getString(R.string.error_past_reservation);
                                } else {
                                    message = getString(R.string.error_past_reservation);
                                }
                                break;
                            default:
                                break;
                        }
                        if (message.isEmpty()) {
                            Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                    pd.show();
                                    createNewReservationForm(parent);
                                }
                            });
                            snackbar.show();
                        } else {
                            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
                        }
                    }

                }
            });
        }
    }

    private void editReservationForm(ConstraintLayout parent) {

        AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        AutoCompleteTextView act_hour = parent.findViewById(R.id.reservation_hour_input);

        if (validateShop(act_shop) & validateReservationDate(tiet_date, act_hour)) {

            ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.updating_data, R.string.please_wait);

            TextInputEditText tiet_remarks = parent.findViewById(R.id.reservation_remark_input);

            String remarksInput = tiet_remarks.getText().toString().trim();
            String dateInput = tiet_date.getText().toString().trim();
            String hourInput = act_hour.getText().toString().trim();

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
                    if (error instanceof TimeoutError) {
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
                    } else {
                        int responseCode = RequestUtils.getErrorCodeRequest(error);
                        // 400 rsv duplicate
                        // 404 client, shop not found: should not happen
                        // 401 rsv unathorized: rsv in past or shop full
                        String message = "";
                        String errorMessage = RequestUtils.getErrorMessageRequest(error);
                        switch (responseCode) {
                            case 400:
                                message = getString(R.string.error_rsv_duplicate);
                                break;
                            case 401:
                                if (errorMessage.contains(RequestUtils.RESERVATION_WHEN_SHOP_FULL)) {
                                    message = getString(R.string.error_past_reservation);
                                } else {
                                    message = getString(R.string.error_past_reservation);
                                }
                                break;
                            default:
                                break;
                        }
                        if (message.isEmpty()) {
                            Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                    pd.show();
                                    editReservationForm(parent);
                                }
                            });
                            snackbar.show();
                        } else {
                            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

    }
}