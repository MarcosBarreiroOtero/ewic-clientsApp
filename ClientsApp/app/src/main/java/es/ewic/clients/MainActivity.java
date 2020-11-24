package es.ewic.clients;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import es.ewic.clients.model.Client;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.FragmentUtils;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLogInSuccessListener,
        MyDataFragment.OnMyDataListener,
        ShopInformationFragment.OnShopInformationListener,
        CreateReservationsFragment.OnCreateReservationListener {


    private Client clientData;

    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clientData = null;
        //Hide toolbar
        findViewById(R.id.my_toolbar).setVisibility(View.GONE);
        FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), LoginFragment.newInstance(), false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    // Location operations
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
        if (shouldProvideRationale) {
            Snackbar.make(findViewById(R.id.mainActivityLayout), getString(R.string.location_needed_message), Snackbar.LENGTH_SHORT)
                    .setAction(R.string.request,
                            v -> startLocationPermissionRequest())
                    .show();
        } else {
            startLocationPermissionRequest();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_my_data:
                if (clientData != null) {
                    FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyDataFragment.newInstance(clientData), true);
                }
                return true;
            case R.id.action_my_reservations:
                if (clientData != null) {
                    FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyReservationsFragment.newInstance(clientData), true);
                }

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.e("BACK", getSupportFragmentManager().getBackStackEntryCount() + " back");
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.w("BACK", "BACK");
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fm.popBackStack();
        } else {
            Log.i("MainActivity", "nothing on backstack, calling super");
            super.onBackPressed();
        }
//        System.exit(1);
    }

    @Override
    public void onLoadClientData(Client clientData) {
        this.clientData = clientData;

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(myToolbar);
        FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), ShopListFragment.newInstance(), false);
    }

    @Override
    public void onUpdateClientAccount(Client newClientData) {
        this.clientData = newClientData;
    }

    @Override
    public void onDeleteClientAccount() {
        this.clientData = null;
        //Hide toolbar
        findViewById(R.id.my_toolbar).setVisibility(View.GONE);
        FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), LoginFragment.newInstance(), false);
    }


    @Override
    public void onBookShopEntry(Shop shopData) {
        if (clientData != null) {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), CreateReservationsFragment.newInstance(clientData, shopData), false);
        }
    }

    @Override
    public void onRsvCreate(Shop shop) {
        if (shop != null) {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), ShopInformationFragment.newInstance(shop), false);
        } else {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), ShopListFragment.newInstance(), true);
        }
    }
}