package es.ewic.clients;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import es.ewic.clients.model.Client;
import es.ewic.clients.utils.BackEndEndpoints;
import es.ewic.clients.utils.FormUtils;
import es.ewic.clients.utils.ModelConverter;
import es.ewic.clients.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    OnLogInSuccessListener mCallback;

    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 01;


    public interface OnLogInSuccessListener {
        void onLoadClientData(Client clientData);
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_login,
                container, false);

        SignInButton signInButton = parent.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Checl if user already signed
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account != null) {
            registerClientBackEnd(account, showConectServerDialog());
        }

        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        return parent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (OnLogInSuccessListener) getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private ProgressDialog showConectServerDialog() {
        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);
        return pd;
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            registerClientBackEnd(account, showConectServerDialog());
        } catch (ApiException e) {
            // Log in cancelled
            showGoogleSigInNeededInformation();
        }
    }

    private void showGoogleSigInNeededInformation() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.warning).setMessage(R.string.login_cancelled_message);

        builder.setPositiveButton(R.string.google_login_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dialog cancelled;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void registerClientBackEnd(GoogleSignInAccount account, ProgressDialog pd) {
        try {
            JSONObject clientData = new JSONObject().put("idGoogleLogin", account.getId())
                    .put("firstName", account.getGivenName())
                    .put("lastName", account.getFamilyName())
                    .put("email", account.getEmail());

            RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.POST, BackEndEndpoints.LOGIN_CLIENTS, clientData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Client newClient = ModelConverter.jsonObjectToClient(response);
                    pd.dismiss();
                    mCallback.onLoadClientData(newClient);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pd.dismiss();
                    Snackbar snackbar;
                    if (error instanceof TimeoutError) {
                        snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                    } else {
                        // int responseCode = RequestUtils.getErrorCodeRequest(error);
                        // 400 client duplicate (should not happen)
                        snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                    }
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            pd.show();
                            registerClientBackEnd(account, pd);
                        }
                    });
                    snackbar.show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}