package es.ewic.clients;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.FormUtils;
import es.ewic.clients.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyDataFragment extends Fragment {

    private static final String ARG_CLIENT_DATA = "clientData";

    private JSONObject clientData;

    OnMyDataListener mCallback;

    public interface OnMyDataListener {
        void onUpdateClientAccount(JSONObject newClientData);

        void onDeleteClientAccount();
    }

    public MyDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param clientData clientData.
     * @return A new instance of fragment MyDataFragment.
     */
    public static MyDataFragment newInstance(JSONObject clientData) {
        MyDataFragment fragment = new MyDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLIENT_DATA, clientData.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (MyDataFragment.OnMyDataListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.toolbar_menu_my_data);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getArguments() != null) {
            try {
                clientData = new JSONObject(getArguments().getString(ARG_CLIENT_DATA));
            } catch (JSONException e) {
                clientData = null;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_my_data,
                container, false);

        TextInputEditText til_name = parent.findViewById(R.id.my_data_name_input);
        TextInputEditText til_last_name = parent.findViewById(R.id.my_data_lastname_input);
        TextInputEditText til_email = parent.findViewById(R.id.my_data_email_input);
        try {
            til_name.setText(clientData.getString("firstName"));
            til_last_name.setText(clientData.getString("lastName"));
            til_email.setText(clientData.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button update_button = parent.findViewById(R.id.button_update_data);
        update_button.setOnClickListener(v -> {
            updateClientData(parent);
        });

        Button delete_button = parent.findViewById(R.id.button_delete_account);
        delete_button.setOnClickListener(v -> {
            showPreDeleteDialog(parent);
        });
        return parent;
    }

    private void updateClientData(ConstraintLayout parent) {

        RelativeLayout loadingPanel = getActivity().findViewById(R.id.loadingPanel);
        FormUtils.showLoadingPanel(getActivity().getWindow(), loadingPanel);

        TextInputEditText til_name = parent.findViewById(R.id.my_data_name_input);
        til_name.clearFocus();
        TextInputEditText til_last_name = parent.findViewById(R.id.my_data_lastname_input);
        til_last_name.clearFocus();
        TextInputEditText til_email = parent.findViewById(R.id.my_data_email_input);
        til_email.clearFocus();

        TextInputLayout til_email_label = parent.findViewById(R.id.my_data_email_label);
        if (!FormUtils.isValidEmail(til_email.getText())) {
            FormUtils.hideLoadingPanel(getActivity().getWindow(), loadingPanel);
            til_email_label.setError(getString(R.string.email_invalid_format));
            return;
        } else {
            til_email_label.setError(null);
        }

        try {
            clientData.put("firstName", til_name.getText());
            clientData.put("lastName", til_last_name.getText());
            clientData.put("email", til_email.getText());

            String url = BackEndEndpoints.UPDATE_DELETE_CLIENT + "/" + clientData.get("idGoogleLogin");

            RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.PUT, url, clientData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    mCallback.onUpdateClientAccount(response);
                    FormUtils.hideLoadingPanel(getActivity().getWindow(), loadingPanel);
                    Snackbar.make(parent, getString(R.string.update_data_successfully), Snackbar.LENGTH_LONG)
                            .show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HTTP", "error");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showPreDeleteDialog(ConstraintLayout parent) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.warning).setMessage(R.string.pre_delete_message);

        builder.setPositiveButton(R.string.delete, (dialog, which) -> deleteClientAccount(parent));

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            // dialog cancelled;
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void deleteClientAccount(ConstraintLayout parent) {
        try {
            String url = BackEndEndpoints.UPDATE_DELETE_CLIENT + "/" + clientData.get("idClient") + "/" + clientData.get("idGoogleLogin");

            RequestUtils.sendStringRequest(getContext(), Request.Method.DELETE, url, response -> {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

                mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                    Snackbar.make(parent, getString(R.string.account_delete_successfully), Snackbar.LENGTH_SHORT)
                            .show();
                    mCallback.onDeleteClientAccount();
                });
            }, error -> {
                Log.e("HTTP", "error");
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}