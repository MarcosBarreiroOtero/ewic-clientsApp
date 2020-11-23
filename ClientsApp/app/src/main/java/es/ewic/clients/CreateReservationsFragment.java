package es.ewic.clients;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Date;

import es.ewic.clients.model.Client;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.DateUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateReservationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateReservationsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CLIENT = "client_data";
    private static final String ARG_SHOP = "shop_data";


    // TODO: Rename and change types of parameters
    private Client client;
    private Shop shop;

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
    // TODO: Rename and change types and number of parameters
    public static CreateReservationsFragment newInstance(Client clientData, Shop shopData) {
        CreateReservationsFragment fragment = new CreateReservationsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLIENT, clientData);
        args.putSerializable(ARG_SHOP, shopData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.newReservation);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getArguments() != null) {
            client = (Client) getArguments().getSerializable(ARG_CLIENT);
            shop = (Shop) getArguments().getSerializable(ARG_SHOP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_create_reservations, container, false);

        // Shop
        AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
        if (shop != null) {
            String[] shops = new String[]{shop.getName()};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.shop_list_item, shops);
            act_shop.setAdapter(adapter);
            act_shop.setText(shop.getName());
            act_shop.setEnabled(false);
        }

        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, 1);

        //Hour input
        TextInputEditText tiet_hour = parent.findViewById(R.id.reservation_hour_input);
        tiet_hour.setText(DateUtils.formatHour(now));
        tiet_hour.setInputType(InputType.TYPE_NULL);
        tiet_hour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    final Calendar now = Calendar.getInstance();
                    int hour = now.get(Calendar.HOUR_OF_DAY);
                    int minutes = now.get(Calendar.MINUTE);

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
            }
        });

        //Date input
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        tiet_date.setText(DateUtils.formatDate(now));
        tiet_date.setInputType(InputType.TYPE_NULL);
        tiet_date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    final Calendar now = Calendar.getInstance();
                    int year = now.get(Calendar.YEAR);
                    int month = now.get(Calendar.MONTH);
                    int day = now.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            Calendar cal = Calendar.getInstance();
                            cal.set(year, month, dayOfMonth);
                            tiet_date.setText(DateUtils.formatDate(cal));
                        }
                    }, year, month, day);
                    datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
                    datePicker.show();
                }
            }
        });

        Button submitButton = parent.findViewById(R.id.reservation_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewReservationForm(parent);
            }
        });


        return parent;
    }

    private boolean validateShop(AutoCompleteTextView act_shop) {
        String shop_input = act_shop.getText().toString().trim();
        if (shop_input.isEmpty()) {
            act_shop.setError(getString(R.string.error_empty_field));
            return false;
        } else {
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
            Log.e("Fecha", "pasado" + date.getTime().toString());
            tiet_hour.setError(getString(R.string.error_past_reservation));
            return false;
        }

        Log.e("Fecha", "correcto");


        tiet_date.setError(null);
        tiet_hour.setError(null);
        return true;
    }

    private void createNewReservationForm(ConstraintLayout parent) {

        AutoCompleteTextView act_shop = parent.findViewById(R.id.reservation_shop_input);
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        TextInputEditText tiet_hour = parent.findViewById(R.id.reservation_hour_input);

        if (validateShop(act_shop) & validateReservationDate(tiet_date, tiet_hour)) {

        }


    }
}