package es.ewic.clients;

import android.Manifest;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;

import es.ewic.clients.adapters.DialogFilterShop;
import es.ewic.clients.adapters.ReservationRowAdapter;
import es.ewic.clients.model.Client;
import es.ewic.clients.model.Reservation;
import es.ewic.clients.model.Shop;
import es.ewic.clients.utils.FragmentUtils;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLogInSuccessListener,
        MyDataFragment.OnMyDataListener,
        ShopListFragment.OnShopListListener,
        ShopInformationFragment.OnShopInformationListener,
        CreateReservationsFragment.OnCreateReservationListener,
        MyReservationsFragment.OnMyReservationsListener,
        ReservationRowAdapter.OnEditReservationListener, DialogFilterShop.OnDialogFilterShopListener {


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
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public void onLoadClientData(Client clientData) {
        this.clientData = clientData;

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(myToolbar);
        FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), ShopListFragment.newInstance(null, null, true), false);
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
    public void onClickShop(Shop shop) {
        if (clientData != null) {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), ShopInformationFragment.newInstance(shop), true);
        }
    }


    @Override
    public void onBookShopEntry(Shop shopData) {
        if (clientData != null) {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), CreateReservationsFragment.newInstance(clientData, shopData, null), true);
        }
    }

    @Override
    public void onRsvCreate(Shop shop) {
        if (shop != null) {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), ShopInformationFragment.newInstance(shop), false);
        } else {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyReservationsFragment.newInstance(clientData), false);
        }
    }

    @Override
    public void onRsvUpdate() {
        if (clientData != null) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStackImmediate();
            } else {
                FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyReservationsFragment.newInstance(clientData), false);
            }
        }
    }

    @Override
    public void onCreateNewRsv() {
        if (clientData != null) {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), CreateReservationsFragment.newInstance(clientData, null, null), true);
        }
    }


    @Override
    public void editReservation(Reservation reservation) {
        if (clientData != null) {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), CreateReservationsFragment.newInstance(clientData, null, reservation), true);
        }
    }

    @Override
    public void onFindShopsFiltered(String shopName, String shopType, boolean useLocation) {
        if (clientData != null) {
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), ShopListFragment.newInstance(shopName, shopType, useLocation), false);
        }
    }
}