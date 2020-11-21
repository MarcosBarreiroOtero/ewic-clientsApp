package es.ewic.clients;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

import es.ewic.clients.model.Client;
import es.ewic.clients.model.Shop;

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
        if (getArguments() != null) {
            client = (Client) getArguments().getSerializable(ARG_CLIENT);
            shop = (Shop) getArguments().getSerializable(ARG_SHOP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_create_reservations, container, false);


        //Hour input
        TextInputEditText tiet_hour = parent.findViewById(R.id.reservation_hour_input);
        tiet_hour.setInputType(InputType.TYPE_NULL);
        tiet_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar now = Calendar.getInstance();
                int hour = now.get(Calendar.HOUR_OF_DAY);
                int minutes = now.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tiet_hour.setText(hourOfDay + ":" + minute);
                    }
                }, hour, minutes, true);
                timePicker.show();
            }
        });

        //Date input
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        tiet_date.setInputType(InputType.TYPE_NULL);
        tiet_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR);
                int month = now.get(Calendar.MONTH);
                int day = now.get(Calendar.DAY_OF_MONTH);
                Log.e("Hola", "abrir date picker");
                DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        tiet_date.setText(dayOfMonth + "/" + month + "/" + "/" + year);
                    }
                }, year, month, day);
                datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
                datePicker.show();
            }
        });


        return parent;
    }
}