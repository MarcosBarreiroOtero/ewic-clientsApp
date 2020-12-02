package es.ewic.clients.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import es.ewic.clients.R;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.RequestUtils;

public class DialogFilterShop extends DialogFragment {

    private static final String ARG_SHOP_NAME = "shop_name";
    private static final String ARG_SHOP_TYPE = "shop_type";
    private static final String ARG_USE_LOCATION = "use_location";


    private String shop_name;
    private String shop_type;
    private boolean use_location;

    OnDialogFilterShopListener mCallback;

    public interface OnDialogFilterShopListener {
        public void onFindShopsFiltered(String shopName, String shopType, boolean useLocation);
    }


    public static DialogFilterShop newInstance(String shop_name, String shop_type, boolean use_location) {
        DialogFilterShop fragment = new DialogFilterShop();
        Bundle args = new Bundle();
        args.putString(ARG_SHOP_NAME, shop_name);
        args.putString(ARG_SHOP_TYPE, shop_type);
        args.putBoolean(ARG_USE_LOCATION, use_location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (OnDialogFilterShopListener) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            shop_name = getArguments().getString(ARG_SHOP_NAME);
            shop_type = getArguments().getString(ARG_SHOP_TYPE);
            use_location = getArguments().getBoolean(ARG_USE_LOCATION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle("Hola");
        return inflater.inflate(R.layout.dialog_filter_shops, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (shop_name != null) {
            TextInputEditText til_shop_name = view.findViewById(R.id.filter_shop_name_input);
            til_shop_name.setText(shop_name);
        }

        CheckBox c_use_location = view.findViewById(R.id.filter_shop_use_location);
        c_use_location.setChecked(use_location);

        getShopTypes(view);

        //Cancel button
        Button btn_cancel = view.findViewById(R.id.button_filter_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Positive button
        Button btn_find = view.findViewById(R.id.button_filter_find);
        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputEditText til_shop_name = view.findViewById(R.id.filter_shop_name_input);
                String shop_name = til_shop_name.getText().toString().trim();
                if (shop_name.equals("")) {
                    shop_name = null;
                }
                Spinner s_shop_type = (Spinner) view.findViewById(R.id.filter_shop_type);
                String shop_type = s_shop_type.getSelectedItem().toString().trim();
                if (shop_type.equals("")) {
                    shop_type = null;
                }

                CheckBox c_use_location = view.findViewById(R.id.filter_shop_use_location);
                boolean useLocation = c_use_location.isChecked();

                mCallback.onFindShopsFiltered(shop_name, shop_type, useLocation);
                dismiss();
            }
        });

    }

    private void getShopTypes(View v) {

        String url = BackEndEndpoints.SHOP_TYPES;

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                List<String> shop_types = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    shop_types.add(response.optString(i));
                }
                String[] types = shop_types.toArray(new String[shop_types.size()]);
                Spinner spinner = (Spinner) v.findViewById(R.id.filter_shop_type);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.shop_list_item, types);
                spinner.setAdapter(adapter);

                if (shop_type != null) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
            }
        });

    }

}
