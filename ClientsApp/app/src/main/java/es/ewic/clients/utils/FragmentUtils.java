package es.ewic.clients.utils;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import es.ewic.clients.R;


public class FragmentUtils {

    private static final FragmentUtils fragmentUtils = new FragmentUtils();
    private static final String TAG = FragmentManager.class.getSimpleName();

    public static FragmentUtils getInstance() {
        return fragmentUtils;
    }

    /**
     * addBackStack a null para poder regresar al fragment anterior usando el botón de atras del
     * teléfono.
     * Solo lo se pone a null si no es la pantalla de logIn.
     *
     * @param fragmentManager the fragment manager
     * @param fragment        the fragment to instantiate
     */
    public static void replaceFragment(FragmentManager fragmentManager, Fragment fragment, boolean addToBackStack) {
        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction().replace(R.id.mainActivityLayout, fragment,
                    fragment.getClass().getName());
            if (addToBackStack) {
                transaction.setReorderingAllowed(true);
                transaction.addToBackStack(null);
            }
            transaction.commit();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }
}
