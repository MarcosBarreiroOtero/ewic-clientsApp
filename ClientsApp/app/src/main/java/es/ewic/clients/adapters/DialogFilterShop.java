package es.ewic.clients.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.ewic.clients.CreateReservationsFragment;
import es.ewic.clients.R;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.RequestUtils;

public class DialogFilterShop extends DialogFragment {

    private static final String ARG_SHOP_NAME = "shop_name";
    private static final String ARG_SHOP_TYPE = "shop_type";
    private static final String ARG_USE_LOCATION = "use_location";
    private static final String ARG_SHOW_LOCATION = "show_location";


    private String shop_name;
    private String shop_type;
    private boolean use_location;

    private boolean show_location = true;

    private JSONArray shop_types_translations;

    OnDialogFilterShopListener mCallback;

    public interface OnDialogFilterShopListener {
        public void onFindShopsFiltered(String shopName, String shopType, boolean useLocation);
    }


    public static DialogFilterShop newInstance(String shop_name, String shop_type, boolean use_location, boolean show_location) {
        DialogFilterShop fragment = new DialogFilterShop();
        Bundle args = new Bundle();
        args.putString(ARG_SHOP_NAME, shop_name);
        args.putString(ARG_SHOP_TYPE, shop_type);
        args.putBoolean(ARG_USE_LOCATION, use_location);
        args.putBoolean(ARG_SHOW_LOCATION, show_location);
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
            show_location = getArguments().getBoolean(ARG_SHOW_LOCATION);
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

        if (!show_location) {
            CheckBox filter_user_location = view.findViewById(R.id.filter_shop_use_location);
            filter_user_location.setVisibility(View.GONE);
        }

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
                String shop_type_translation = s_shop_type.getSelectedItem().toString().trim();
                if (shop_type_translation.equals(getString(R.string.All))) {
                    shop_type = null;
                } else {
                    for (int i = 0; i < shop_types_translations.length(); i++) {
                        JSONObject jsonObject = shop_types_translations.optJSONObject(i);
                        if (jsonObject.optString("translation").equals(shop_type_translation)) {
                            shop_type = jsonObject.optString("type");
                            break;
                        }
                    }
                }

                CheckBox c_use_location = view.findViewById(R.id.filter_shop_use_location);
                boolean useLocation = c_use_location.isChecked();

                if (show_location) {
                    mCallback.onFindShopsFiltered(shop_name, shop_type, useLocation);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(CreateReservationsFragment.INTENT_SHOP_NAME, shop_name);
                    intent.putExtra(CreateReservationsFragment.INTENT_SHOP_TYPE, shop_type);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                }

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
                String selected = "";
                shop_types_translations = new JSONArray();
                for (int i = 0; i < response.length(); i++) {
                    String type = response.optString(i);
                    String type_translation = getString(getResources().getIdentifier(type, "string", getActivity().getPackageName()));
                    if (type.equals(shop_type)) {
                        selected = type_translation;
                    }
                    shop_types.add(type_translation);
                    try {
                        shop_types_translations.put(new JSONObject().put("type", type).put("translation", type_translation));
                    } catch (JSONException e) {
                        // should not happen
                    }
                }
                Collections.sort(shop_types);
                shop_types.add(0, getString(R.string.All));
                String[] types = shop_types.toArray(new String[shop_types.size()]);
                Spinner spinner = (Spinner) v.findViewById(R.id.filter_shop_type);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.shop_list_item, types);
                spinner.setAdapter(adapter);
                if (selected.isEmpty()) {
                    spinner.setSelection(0);
                } else {
                    for (int i = 1; i < types.length; i++) {
                        if (types[i].equals(selected)) {
                            spinner.setSelection(i);
                            break;
                        }
                    }
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
