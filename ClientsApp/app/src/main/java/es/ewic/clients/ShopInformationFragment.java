package es.ewic.clients;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ramijemli.percentagechartview.PercentageChartView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.DateUtils;
import es.ewic.clients.utils.FormUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopInformationFragment extends Fragment implements OnMapReadyCallback {


    private static final String ARG_SHOP_INFORMATION = "shopInformation";

    OnShopInformationListener mCallback;

    private Shop shopInformation;
    private MapView mMapView;

    private Dialog timetableDialog;

    private PercentageChartView percentageChartView;
    private float percentage;

    public interface OnShopInformationListener {
        void onBookShopEntry(Shop shopData);

        void saveStatus(Shop shopInformation);
    }

    public ShopInformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param shop shop Information;
     * @return A new instance of fragment ShopInformation.
     */
    public static ShopInformationFragment newInstance(Shop shop) {
        ShopInformationFragment fragment = new ShopInformationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SHOP_INFORMATION, shop);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (ShopInformationFragment.OnShopInformationListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            shopInformation = (Shop) savedInstanceState.getSerializable(ARG_SHOP_INFORMATION);
        } else {
            if (getArguments() != null) {
                shopInformation = (Shop) getArguments().getSerializable(ARG_SHOP_INFORMATION);
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(shopInformation.getName());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (percentageChartView != null) {
            FormUtils.configureSemaphorePercentageChartView(getResources(), percentageChartView, percentage);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (shopInformation != null) {
            savedInstanceState.putSerializable(ARG_SHOP_INFORMATION, shopInformation);
            mCallback.saveStatus(shopInformation);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_shop_information, container, false);

        TextView shop_type = parent.findViewById(R.id.shop_information_type);
        shop_type.setText(getString(getResources().getIdentifier(shopInformation.getType(), "string", getActivity().getPackageName())));

        TextView shop_location = parent.findViewById(R.id.shop_information_location);
        shop_location.setText(shopInformation.getLocation());

        TextView shop_open = parent.findViewById(R.id.shop_information_open);
        if (!shopInformation.isAllowEntries()) {
            shop_open.setTextColor(getResources().getColor(R.color.semaphore_red, null));
            shop_open.setText(getString(R.string.close));
        } else {
            shop_open.setTextColor(getResources().getColor(R.color.semaphore_green, null));
            shop_open.setText(getString(R.string.open));

            if (shopInformation.getTimetable().length() != 0) {
                TextView shop_current_timetable = parent.findViewById(R.id.shop_information_current_timetable);
                getCurrentTimetable(parent, shop_current_timetable);
            }
        }

        ImageView iv_timetable = parent.findViewById(R.id.timetable_icon);
        timetableDialog = createTimetableDialog();
        iv_timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timetableDialog.show();
            }
        });

        percentageChartView = parent.findViewById(R.id.shop_information_percentage);
        percentage = ((float) shopInformation.getActualCapacity() / shopInformation.getMaxCapacity()) * 100;
        FormUtils.configureSemaphorePercentageChartView(getResources(), percentageChartView, percentage);

        TextView shop_capacity = parent.findViewById(R.id.shop_information_capacity);
        shop_capacity.setText(shopInformation.getActualCapacity() + "/" + shopInformation.getMaxCapacity());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        Button shop_information_book_entry = parent.findViewById(R.id.shop_information_book_entry);
        shop_information_book_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onBookShopEntry(shopInformation);
            }
        });
        return parent;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = new LatLng(shopInformation.getLatitude(), shopInformation.getLongitude());

        googleMap.addMarker(new MarkerOptions().position(location).title(shopInformation.getName()));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        googleMap.setMinZoomPreference(15);
        UiSettings settings = googleMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setCompassEnabled(true);

        mapEnableMyPosition(googleMap);
    }

    @SuppressLint("MissingPermission")
    private void mapEnableMyPosition(GoogleMap googleMap) {

        googleMap.setMyLocationEnabled(true);
    }

    private Dialog createTimetableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.timetable));
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        String message = "";
        JSONArray timetable = new JSONArray();
        try {
            timetable = new JSONArray(shopInformation.getTimetable());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (timetable.length() == 0) {
            message = getString(R.string.shop_timetable_not_defined);
        } else {
            for (int i = 0; i < timetable.length(); i++) {
                JSONObject weekDayTimetable = timetable.optJSONObject(i);
                message += "<strong>";
                int weekDay = weekDayTimetable.optInt("weekDay");

                switch (weekDay) {
                    case 0:
                        message += getString(R.string.monday);
                        break;
                    case 1:
                        message += getString(R.string.tuesday);
                        break;
                    case 2:
                        message += getString(R.string.wednesday);
                        break;
                    case 3:
                        message += getString(R.string.thursday);
                        break;
                    case 4:
                        message += getString(R.string.friday);
                        break;
                    case 5:
                        message += getString(R.string.saturday);
                        break;
                    case 6:
                        message += getString(R.string.sunday);
                        break;
                }

                message += ":</strong> ";
                boolean haveMorning = false;
                try {
                    message += weekDayTimetable.getString("startMorning") + "-" + weekDayTimetable.getString("endMorning");
                    haveMorning = true;
                } catch (JSONException e) {
                    // not morning timetable
                }

                try {
                    String timetableAfternoon = weekDayTimetable.getString("startAfternoon") + "-" + weekDayTimetable.getString("endAfternoon");
                    if (haveMorning) {
                        message += " / ";
                    }
                    message += timetableAfternoon;
                } catch (JSONException e) {
                    // not afternoon timetable
                }

                if (i != (timetable.length() - 1)) {
                    message += " <br/><br/>";
                }
            }
        }

        builder.setMessage(Html.fromHtml(message));
        return builder.create();
    }


    private void getCurrentTimetable(ConstraintLayout parent, TextView shop_current_timetable) {
        Calendar now = Calendar.getInstance();
        int weekDay = now.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1) {
            weekDay = 6;
        } else {
            weekDay -= 2;
        }

        JSONArray timetable = new JSONArray();
        try {
            timetable = new JSONArray(shopInformation.getTimetable());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < timetable.length(); i++) {
            JSONObject weekDayTimetable = timetable.optJSONObject(i);
            if (weekDay == weekDayTimetable.optInt("weekDay")) {
                try {
                    Calendar startMorning = DateUtils.parseDateHour(weekDayTimetable.getString("startMorning"));
                    startMorning.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                    Calendar endMorning = DateUtils.parseDateHour(weekDayTimetable.getString("endMorning"));
                    endMorning.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                    if (startMorning.compareTo(now) <= 0 && endMorning.compareTo(now) >= 0) {
                        shop_current_timetable.setText(weekDayTimetable.optString("startMorning") + " - " + weekDayTimetable.optString("endMorning"));
                        break;
                    }
                } catch (JSONException e) {
                    // not morning timetable
                }
                try {
                    Calendar startAfternoon = DateUtils.parseDateHour(weekDayTimetable.getString("startAfternoon"));
                    startAfternoon.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                    Calendar endAfternoon = DateUtils.parseDateHour(weekDayTimetable.getString("endAfternoon"));
                    endAfternoon.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                    if (startAfternoon.compareTo(now) <= 0 && endAfternoon.compareTo(now) >= 0) {
                        shop_current_timetable.setText(weekDayTimetable.optString("startAfternoon") + " - " + weekDayTimetable.optString("endAfternoon"));
                    }
                } catch (JSONException e) {
                    // not afternoon timetable
                }
                break;
            }
        }
    }
}