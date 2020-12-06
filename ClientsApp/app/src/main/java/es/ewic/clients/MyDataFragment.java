package es.ewic.clients;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import es.ewic.clients.model.Client;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.FormUtils;
import es.ewic.clients.utils.ModelConverter;
import es.ewic.clients.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyDataFragment extends Fragment {

    private static final String ARG_CLIENT_DATA = "clientData";

    private Client clientData;

    OnMyDataListener mCallback;

    public interface OnMyDataListener {
        void onUpdateClientAccount(Client newClientData);

        void onDeleteClientAccount();
    }

    public MyDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param client clientData.
     * @return A new instance of fragment MyDataFragment.
     */
    public static MyDataFragment newInstance(Client client) {
        MyDataFragment fragment = new MyDataFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLIENT_DATA, client);
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

        if (getArguments() != null) {
            clientData = (Client) getArguments().getSerializable(ARG_CLIENT_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_my_data,
                container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.toolbar_menu_my_data);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);


        TextInputEditText til_name = parent.findViewById(R.id.my_data_name_input);
        til_name.setText(clientData.getFirstName());

        TextInputEditText til_last_name = parent.findViewById(R.id.my_data_lastname_input);
        til_last_name.setText(clientData.getLastName());

        TextInputEditText til_email = parent.findViewById(R.id.my_data_email_input);
        til_email.setText(clientData.getEmail());

        Button update_button = parent.findViewById(R.id.button_update_data);
        update_button.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            updateClientData(parent);
        });

        Button delete_button = parent.findViewById(R.id.button_delete_account);
        delete_button.setOnClickListener(v -> {
            showPreDeleteDialog(parent);
        });
        return parent;
    }

    private void updateClientData(ConstraintLayout parent) {

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.updating_data, R.string.please_wait);

        TextInputEditText til_name = parent.findViewById(R.id.my_data_name_input);
        til_name.clearFocus();
        TextInputEditText til_last_name = parent.findViewById(R.id.my_data_lastname_input);
        til_last_name.clearFocus();
        TextInputEditText til_email = parent.findViewById(R.id.my_data_email_input);
        til_email.clearFocus();

        TextInputLayout til_email_label = parent.findViewById(R.id.my_data_email_label);
        if (!FormUtils.isValidEmail(til_email.getText())) {
            til_email_label.setError(getString(R.string.email_invalid_format));
            pd.hide();
            return;
        } else {
            til_email_label.setError(null);
        }

        clientData.setFirstName(til_name.getText().toString());
        clientData.setLastName(til_last_name.getText().toString());
        clientData.setEmail(til_email.getText().toString());

        String url = BackEndEndpoints.UPDATE_DELETE_CLIENT + "/" + clientData.getIdGoogleLogin();
        JSONObject clientJSON = ModelConverter.clientToJsonObject(clientData);
        RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.PUT, url, clientJSON, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                clientData = ModelConverter.jsonObjectToClient(response);
                mCallback.onUpdateClientAccount(clientData);
                pd.hide();
                Snackbar.make(parent, getString(R.string.update_data_successfully), Snackbar.LENGTH_LONG)
                        .show();

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
                            updateClientData(parent);
                        }
                    });
                    snackbar.show();
                } else {
                    int responseCode = RequestUtils.getErrorCodeRequest(error);
                    //404 client not found: should not happen
                    //400 client duplicate: should not happen
                    Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            pd.show();
                            updateClientData(parent);
                        }
                    });
                    snackbar.show();
                }

            }
        });
    }

    private void showPreDeleteDialog(ConstraintLayout parent) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.warning).setMessage(R.string.pre_delete_message);

        builder.setPositiveButton(R.string.delete, (dialog, which) -> deleteClientAccount(parent));

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            // dialog cancelled;
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.semaphore_red));
            }
        });
        dialog.show();
    }

    private void deleteClientAccount(ConstraintLayout parent) {
        String url = BackEndEndpoints.UPDATE_DELETE_CLIENT + "/" + clientData.getIdClient() + "/" + clientData.getIdGoogleLogin();

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

            if (error instanceof TimeoutError) {
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        deleteClientAccount(parent);
                    }
                });
                snackbar.show();
            } else {
                int responseCode = RequestUtils.getErrorCodeRequest(error);
                //404 client not found: should not happen
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        deleteClientAccount(parent);
                    }
                });
                snackbar.show();
            }


        });
    }
}