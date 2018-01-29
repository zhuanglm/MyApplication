package com.zhuang.myapplication.createspace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.securespaces.debug.R;
import com.securespaces.zxing.Intents;


public class FragmentSpacesCreator extends PreferenceFragment implements
        CreateSpaceContract.UserActionsListener,
        CreateSpaceContract.UserInputListener,
        Preference.OnPreferenceClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int CREATE_MODE_CLICK = 2001;
    public static final int CREATE_MODE_QR = 2002;

    private static final String PREF_CREATE_SPACE = "pref_create_space";
    private static final String PREF_QR_SPACE = "pref_QR_space";
    private static final String PREF_EDIT_NAME = "pref_edit_text_name";
    private static final String DEFAULT_SPACE_NAME = "MySpace";

    private EditTextPreference mEditTextSpaceName;
    private CheckBoxPreference mCheckBoxManaged;
    private CheckBoxPreference mCheckBoxEncrypted;
    private CheckBoxPreference mCheckBoxControlled;

    private CreateSpaceContract.UserActionsListener mActionsListener;

    public FragmentSpacesCreator() {
        // Required empty public constructor
    }

    public static FragmentSpacesCreator newInstance() {
        FragmentSpacesCreator fragment = new FragmentSpacesCreator();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_create);
        setHasOptionsMenu(true);

        PreferenceManager manager = getPreferenceManager();
        PreferenceScreen screen = getPreferenceScreen();
        mActionsListener = new CreateSpacePresenter(getActivity(), this);

        mEditTextSpaceName = (EditTextPreference) manager.findPreference("pref_edit_text_name");
        mCheckBoxManaged = (CheckBoxPreference) screen.findPreference("pref_checkbox_managed");
        mCheckBoxEncrypted = (CheckBoxPreference) screen.findPreference("pref_checkbox_encrypted");
        mCheckBoxControlled = (CheckBoxPreference) screen.findPreference("pref_checkbox_APP_Control");

        addOnClickListeners(screen);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((AppCompatActivity)getActivity()).getSupportActionBar()
                        .setDisplayHomeAsUpEnabled(false);
                getActivity().getFragmentManager().popBackStack();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PREF_EDIT_NAME)) {
            initNameSummary();
        }

    }

    private void addOnClickListeners(PreferenceGroup prefGroup) {

        PreferenceManager manager = getPreferenceManager();

        manager.findPreference(PREF_CREATE_SPACE).setOnPreferenceClickListener(this);
        manager.findPreference(PREF_QR_SPACE).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(PREF_CREATE_SPACE)) {
            addSpace(CREATE_MODE_CLICK);
            //Toast.makeText(getActivity(), m_Space.m_Name, Toast.LENGTH_SHORT).show();
        } else if (preference.getKey().equals(PREF_QR_SPACE)) {
            addSpace(CREATE_MODE_QR);
        }
        return true;
    }

    public void initNameSummary() {
        if (mEditTextSpaceName != null) {
            String spaceName = mEditTextSpaceName.getText();
            if (spaceName == null) {
                mEditTextSpaceName.setSummary(R.string.pref_sum_space_name);
            } else {
                if (spaceName.equals(""))
                    mEditTextSpaceName.setSummary(R.string.pref_sum_space_name);
                else
                    mEditTextSpaceName.setSummary(spaceName);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        initNameSummary();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == CreateSpacePresenter.REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            onQrCodeScanned(intent.getStringExtra(Intents.Scan.RESULT));
        }
    }

    @Override
    public void onQrCodeScanned(String uriString) {
        mActionsListener.onQrCodeScanned(uriString);
    }

    @Override
    public void addSpace(int mode) {
        mActionsListener.addSpace(mode);
    }

    @Override
    public String getSpaceName() {
        String name = mEditTextSpaceName.getText();
        if (name == null || name.equals("")) {
            name = DEFAULT_SPACE_NAME;
        }
        return name;
    }

    @Override
    public boolean isManaged() {
        return mCheckBoxManaged.isChecked();
    }

    @Override
    public boolean isEncrypted() {
        return mCheckBoxEncrypted.isChecked();
    }

    @Override
    public boolean isControlled() {
        return mCheckBoxControlled.isChecked();
    }
}
