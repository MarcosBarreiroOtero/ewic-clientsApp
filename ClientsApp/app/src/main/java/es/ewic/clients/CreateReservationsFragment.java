package es.ewic.clients;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.ewic.clients.adapters.DialogFilterShop;
import es.ewic.clients.adapters.HourAutocompleteAdapter;
import es.ewic.clients.model.Client;
import es.ewic.clients.model.Reservation;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.ConfigurationNames;
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

    public static final String INTENT_SHOP_NAME = "shop_name";
    public static final String INTENT_SHOP_TYPE = "shop_type";

    private static final String ARG_CLIENT = "client_data";
    private static final String ARG_SHOP = "shop_data";
    private static final String ARG_RSV = "reservation_data";

    public static final int DIALOG_FRAGMENT = 1;

    private ConstraintLayout parent;
    private Client client;
    private Shop shop;
    private Reservation reservation;
    private JSONArray shopNames;
    private JSONArray timetable;

    private int minutesBetweenReservation = 15;
    private int minutesAfterOpeningMorning = 0;
    private int minutesBeforeClosingMorning = 0;
    private int minutesAfterOpeningAfternoon = 0;
    private int minutesBeforeClosingAfternoon = 0;

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
        parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_create_reservations, container, false);


        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, 1);

        //Hour input
        AutoCompleteTextView act_hour = parent.findViewById(R.id.reservation_hour_input);
        if (shop != null) {
            timetable = new JSONArray();
            try {
                timetable = new JSONArray(shop.getTimetable());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            boolean closed = true;
            while (closed) {
                now.add(Calendar.DATE, 1);
                int weekDay = now.get(Calendar.DAY_OF_WEEK);
                if (weekDay == 1) {
                    weekDay = 6;
                } else {
                    weekDay -= 2;
                }
                for (int i = 0; i < timetable.length(); i++) {
                    JSONObject weekDayTimetable = timetable.optJSONObject(i);
                    if (weekDay == weekDayTimetable.optInt("weekDay")) {
                        closed = false;
                        break;
                    }
                }
            }
            getReservationParams(parent, now, timetable, shop.getIdShop());
        }

        if (reservation != null) {
            getShopTimetable(reservation.getDate());
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

        //Nclients
        TextInputEditText tiet_nClients = parent.findViewById(R.id.reservation_nClients_input);
        if (reservation != null) {
            tiet_nClients.setText(Integer.toString(reservation.getnClients()));
        } else {
            tiet_nClients.setText("1");
        }

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
            TextInputLayout til_shop = parent.findViewById(R.id.reservation_shop_label);
            til_shop.setStartIconDrawable(R.drawable.ic_filter_24dp);
            til_shop.setStartIconContentDescription(R.string.filter_shops);
            Fragment targetFragment = this;
            til_shop.setStartIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = DialogFilterShop.newInstance(null, null, false, false);
                    newFragment.setTargetFragment(targetFragment, DIALOG_FRAGMENT);
                    newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            });

            getShopNames(null, null);

            act_shop.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        if (validateShop()) {
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
                                    String minutesBetweenReservationsText = shopName.optString("minutesBetweenReservations");
                                    minutesBetweenReservation = minutesBetweenReservationsText.isEmpty() ? 15 : Integer.parseInt(minutesBetweenReservationsText);
                                    String minutesAfterOpeningMorningText = shopName.optString("minutesAfterOpeningMorning");
                                    minutesAfterOpeningMorning = minutesAfterOpeningMorningText.isEmpty() ? 0 : Integer.parseInt(minutesAfterOpeningMorningText);
                                    String minutesBeforeClosingMorningText = shopName.optString("minutesBeforeClosingMorning");
                                    minutesBeforeClosingMorning = minutesBeforeClosingMorningText.isEmpty() ? 0 : Integer.parseInt(minutesBeforeClosingMorningText);
                                    String minutesAfterOpeningAfternoonText = shopName.optString("minutesAfterOpeningAfternoon");
                                    minutesAfterOpeningAfternoon = minutesAfterOpeningAfternoonText.isEmpty() ? 0 : Integer.parseInt(minutesAfterOpeningAfternoonText);
                                    String minutesBeforeClosingAfternoonText = shopName.optString("minutesBeforeClosingAfternoon");
                                    minutesBeforeClosingAfternoon = minutesBeforeClosingAfternoonText.isEmpty() ? 0 : Integer.parseInt(minutesBeforeClosingAfternoonText);

                                }
                            }
                        } else {
                            timetable = new JSONArray();
                            minutesBetweenReservation = 15;
                            minutesAfterOpeningMorning = 0;
                            minutesBeforeClosingMorning = 0;
                            minutesAfterOpeningAfternoon = 0;
                            minutesBeforeClosingAfternoon = 0;
                        }
                        Calendar date = DateUtils.parseDateDate(tiet_date.getText().toString().trim());
                        if (timetable.length() != 0) {
                            boolean closed = true;
                            while (closed) {
                                int weekDay = date.get(Calendar.DAY_OF_WEEK);
                                if (weekDay == 1) {
                                    weekDay = 6;
                                } else {
                                    weekDay -= 2;
                                }
                                for (int i = 0; i < timetable.length(); i++) {
                                    JSONObject weekDayTimetable = timetable.optJSONObject(i);
                                    if (weekDay == weekDayTimetable.optInt("weekDay")) {
                                        closed = false;
                                        break;
                                    }
                                }
                                if (closed) {
                                    date.add(Calendar.DATE, 1);
                                }
                            }
                        }
                        tiet_date.setText(DateUtils.formatDate(date));
                        setAdapterHourInput(parent, date, timetable);
                        act_hour.setText("");
                        tiet_nClients.setText("1");
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
                    editReservationForm();
                } else {
                    createNewReservationForm();
                }
            }
        });


        return parent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    String shop_Name = data.getStringExtra(INTENT_SHOP_NAME);
                    String shop_type = data.getStringExtra(INTENT_SHOP_TYPE);
                    getShopNames(shop_Name, shop_type);
                }
                break;
        }
    }

    private List<String> getHoursBetweenRanges(Calendar start, Calendar end) {
        List<String> hours = new ArrayList<>();
        while (start.before(end)) {
            hours.add(DateUtils.formatHour(start));
            start.add(Calendar.MINUTE, minutesBetweenReservation);
        }

        return hours;

    }

    private void getReservationParams(ConstraintLayout parent, Calendar date, JSONArray timetable, int idShop) {

        String url = BackEndEndpoints.CONFIGURATION_RESERVATION(idShop);

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject param = response.getJSONObject(i);
                        String value = param.getString("value");
                        if (!value.isEmpty()) {
                            if (param.getString("name").equals(ConfigurationNames.MINUTES_BETWEEN_RESERVATIONS)) {
                                minutesBetweenReservation = Integer.parseInt(param.getString("value"));
                            }
                            if (param.getString("name").equals(ConfigurationNames.MINUTES_AFTER_OPENING_MORNING)) {
                                minutesAfterOpeningMorning = Integer.parseInt(param.getString("value"));
                            }
                            if (param.getString("name").equals(ConfigurationNames.MINUTES_BEFORE_CLOSING_MORNING)) {
                                minutesBeforeClosingMorning = Integer.parseInt(param.getString("value"));
                            }
                            if (param.getString("name").equals(ConfigurationNames.MINUTES_AFTER_OPENING_AFTERNOON)) {
                                minutesAfterOpeningAfternoon = Integer.parseInt(param.getString("value"));
                            }
                            if (param.getString("name").equals(ConfigurationNames.MINUTES_BEFORE_CLOSING_AFTERNOON)) {
                                minutesBeforeClosingAfternoon = Integer.parseInt(param.getString("value"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                setAdapterHourInput(parent, date, timetable);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error reservation params");
            }
        });
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
                        startMorning.add(Calendar.MINUTE, minutesAfterOpeningMorning);
                        Calendar endMorning = DateUtils.parseDateHour(weekDayTimetable.getString("endMorning"));
                        endMorning.add(Calendar.MINUTE, -minutesBeforeClosingMorning);
                        hours.addAll(getHoursBetweenRanges(startMorning, endMorning));
                    } catch (JSONException e) {
                        // no timetable morning
                    }

                    try {
                        Calendar startAfternoon = DateUtils.parseDateHour(weekDayTimetable.getString("startAfternoon"));
                        startAfternoon.add(Calendar.MINUTE, minutesAfterOpeningAfternoon);
                        Calendar endAfternoon = DateUtils.parseDateHour(weekDayTimetable.getString("endAfternoon"));
                        endAfternoon.add(Calendar.MINUTE, -minutesBeforeClosingAfternoon);
                        hours.addAll(getHoursBetweenRanges(startAfternoon, endAfternoon));
                    } catch (JSONException e) {
                        // no timetable afternoon
                    }
                }
            }
        }

        String[] hoursValues = hours.toArray(new String[hours.size()]);
        AutoCompleteTextView act_hours = parent.findViewById(R.id.reservation_hour_input);
        HourAutocompleteAdapter adapter = new HourAutocompleteAdapter(hours, CreateReservationsFragment.this);
        act_hours.setAdapter(adapter);
        if (reservation != null) {
            act_hours.setText(DateUtils.formatHour(reservation.getDate()));
        } else {
            act_hours.setText("");
        }

    }

    private void getShopNames(String shopName, String shop_type) {

        String url = BackEndEndpoints.SHOP_NAMES;

        TextInputLayout til_shop = parent.findViewById(R.id.reservation_shop_label);
        if (shopName != null || shop_type != null) {
            til_shop.setHelperTextEnabled(true);
            String helperText = getString(R.string.last_filtering) + ":";
            url += "?";
            if (shopName != null) {
                url += "name=" + shopName + "&";
                helperText += " " + shopName + " (" + getString(R.string.shop_name) + ")";
            }
            if (shop_type != null) {
                url += "shopType=" + shop_type + "&";
                String shopTypeText = getString(R.string.shopType);
                shopTypeText = shopTypeText.substring(0, shopTypeText.length() - 1);
                String type_translation = getString(getResources().getIdentifier(shop_type, "string", getActivity().getPackageName()));
                helperText += " " + type_translation + " (" + shopTypeText + ")";
            }
            til_shop.setHelperText(helperText);
        } else {
            til_shop.setHelperTextEnabled(false);
        }


        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (shopName == null && shop_type == null) {
                    shopNames = response;
                }
                List<String> names = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
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

    private void getShopTimetable(Calendar date) {
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

                    getReservationParams(parent, date, timetable, reservation.getIdShop());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HTTP", "Error");
                }
            });
        }

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
                int weekDay = cal.get(Calendar.DAY_OF_WEEK);
                if (weekDay == 1) {
                    weekDay = 6;
                } else {
                    weekDay -= 2;
                }
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
                } else {
                    tiet_date.setText(DateUtils.formatDate(cal));
                    setAdapterHourInput(parent, cal, timetable);
                }

            }
        }, year, month, day);
        datePicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        datePicker.show();
    }

    private boolean validateShop() {
        TextInputLayout til_shop = parent.findViewById(R.id.reservation_shop_label);
        AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);

        String shop_input = act_shop.getText().toString().trim();
        if (shop_input.isEmpty()) {
            til_shop.setError(getString(R.string.error_empty_field));
            return false;
        } else {
            if (shopNames != null) {
                boolean error = true;
                for (int i = 0; i < shopNames.length(); i++) {
                    JSONObject shopName = shopNames.optJSONObject(i);
                    if (shop_input.equals(shopName.optString("name"))) {
                        error = false;
                        break;
                    }
                }
                if (error) {
                    til_shop.setError(getString(R.string.error_shop_not_found));
                    return false;
                }
            } else {
                ArrayList<String> results = new ArrayList<>();
                ListAdapter adapter = act_shop.getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    results.add((String) adapter.getItem(i));
                }
                if (results.size() == 0 ||
                        results.indexOf(shop_input) == -1) {
                    til_shop.setError(getString(R.string.error_shop_not_found));
                    return false;
                }
            }

        }
        til_shop.setError(null);
        return true;

    }

    private boolean validateReservationDate() {
        TextInputLayout til_date = parent.findViewById(R.id.reservation_date_label);
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        TextInputLayout til_hour = parent.findViewById(R.id.reservation_hour_label);
        AutoCompleteTextView act_hour = parent.findViewById(R.id.reservation_hour_input);

        String dateInput = tiet_date.getText().toString().trim();
        boolean validDate = true;
        boolean validHour = true;
        if (dateInput.isEmpty()) {
            til_date.setError(getString(R.string.error_empty_field));
            validDate = false;
        }

        String hourInput = act_hour.getText().toString().trim();
        if (hourInput.isEmpty()) {
            til_hour.setError(getString(R.string.error_empty_field));
            validHour = false;
        } else {
            ArrayList<String> results = new ArrayList<>();
            ListAdapter adapter = act_hour.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                results.add((String) adapter.getItem(i));
            }
            if (results.size() == 0 ||
                    results.indexOf(hourInput) == -1) {
                til_hour.setError(getString(R.string.error_hour_invalid));
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
            til_hour.setError(getString(R.string.error_bad_format_hour));
            til_date.setError(getString(R.string.error_bad_format_date));
            return false;
        } else if (now.after(date)) {
            til_hour.setError(getString(R.string.error_past_reservation));
            act_hour.setText("");
            act_hour.requestFocus();
            return false;
        }

        til_date.setError(null);
        til_hour.setError(null);
        return true;
    }

    private boolean validateNClients() {
        TextInputLayout til_nClients = parent.findViewById(R.id.reservation_nClients_label);
        TextInputEditText tiet_nClients = parent.findViewById(R.id.reservation_nClients_input);

        if (tiet_nClients.getText().toString().trim().isEmpty()) {
            til_nClients.setError(getString(R.string.error_empty_field));
            return false;
        }

        Integer nClientsValue = Integer.parseInt(tiet_nClients.getText().toString().trim());


        if (nClientsValue < 1) {
            til_nClients.setError(getString(R.string.error_nClients_min));
            return false;
        }

        if (shop != null) {
            if (nClientsValue > shop.getMaxCapacity()) {
                til_nClients.setError(getString(R.string.error_nClient_max) + " (" + shop.getMaxCapacity() + ")");
                return false;
            }
        } else if (reservation == null) {
            AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
            String shop_input = act_shop.getText().toString().trim();
            for (int i = 0; i < shopNames.length(); i++) {
                JSONObject shopName = shopNames.optJSONObject(i);
                if (shop_input.equals(shopName.optString("name"))) {
                    int maxCapacity = shopName.optInt("maxCapacity");
                    if (nClientsValue > maxCapacity) {
                        til_nClients.setError(getString(R.string.error_nClient_max) + " (" + maxCapacity + ")");
                        return false;
                    }
                    break;
                }
            }
        }

        til_nClients.setError(null);
        return true;
    }

    private void createNewReservationForm() {

        if (validateShop() & validateReservationDate() & validateNClients()) {
            ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.updating_data, R.string.please_wait);

            AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
            TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
            AutoCompleteTextView act_hour = parent.findViewById(R.id.reservation_hour_input);
            TextInputEditText tiet_nClients = parent.findViewById(R.id.reservation_nClients_input);
            TextInputEditText tiet_remarks = parent.findViewById(R.id.reservation_remark_input);

            String remarksInput = tiet_remarks.getText().toString().trim();
            String dateInput = tiet_date.getText().toString().trim();
            String hourInput = act_hour.getText().toString().trim();
            String shopInput = act_shop.getText().toString().trim();
            Integer nClientsInput = Integer.parseInt(tiet_nClients.getText().toString().trim());

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
            Reservation rsv = new Reservation(date, remarksInput, nClientsInput, client.getIdGoogleLogin(), idShop);

            String url = BackEndEndpoints.RESERVATION_BASE;
            JSONObject rsvJSON = ModelConverter.reservationToJsonObject(rsv);
            RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.POST, url, rsvJSON, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Snackbar.make(parent, getString(R.string.reservation_create_successfully), Snackbar.LENGTH_LONG)
                            .show();
                    pd.dismiss();
                    mCallback.onRsvCreate(shop);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HTTP", "error");
                    pd.dismiss();
                    if (error instanceof TimeoutError) {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                pd.show();
                                createNewReservationForm();
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
                                    message = getString(R.string.error_reservation_shop_full);
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
                                    createNewReservationForm();
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

    private void editReservationForm() {

        if (validateShop() & validateReservationDate() & validateNClients()) {

            ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.updating_data, R.string.please_wait);

            AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
            TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
            AutoCompleteTextView act_hour = parent.findViewById(R.id.reservation_hour_input);
            TextInputEditText tiet_nClients = parent.findViewById(R.id.reservation_nClients_input);
            TextInputEditText tiet_remarks = parent.findViewById(R.id.reservation_remark_input);

            String remarksInput = tiet_remarks.getText().toString().trim();
            String dateInput = tiet_date.getText().toString().trim();
            String hourInput = act_hour.getText().toString().trim();
            String shopInput = act_shop.getText().toString().trim();
            Integer nClientsInput = Integer.parseInt(tiet_nClients.getText().toString().trim());

            Calendar date = DateUtils.parseDateLong(hourInput + " " + dateInput);
            reservation.setDate(date);
            reservation.setRemarks(remarksInput);
            reservation.setnClients(nClientsInput);

            String url = BackEndEndpoints.RESERVATION_BASE + "/" + reservation.getIdReservation();
            JSONObject rsvJSON = ModelConverter.reservationToJsonObject(reservation);
            RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.PUT, url, rsvJSON, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Snackbar.make(parent, getString(R.string.update_reservation_successfully), Snackbar.LENGTH_LONG)
                            .show();
                    pd.dismiss();
                    mCallback.onRsvUpdate();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HTTP", "error");
                    pd.dismiss();
                    if (error instanceof TimeoutError) {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                pd.show();
                                editReservationForm();
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
                                    message = getString(R.string.error_reservation_shop_full);
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
                                    editReservationForm();
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