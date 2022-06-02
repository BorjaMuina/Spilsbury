package com.borja.spilsbury.logica;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.borja.spilsbury.R;

public class PreferenciasFragment extends PreferenceFragmentCompat {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferencias);
    }
}
