package com.zhuang.myapplication.createspace;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.securespaces.android.ssm.SpaceDescriptor;
import com.securespaces.android.ssm.SystemUtils;
import com.securespaces.debug.R;
import com.securespaces.debug.utilites.SettingUtils;

import static com.securespaces.debug.createspace.FragmentSpacesCreator.CREATE_MODE_CLICK;
import static com.securespaces.debug.createspace.FragmentSpacesCreator.CREATE_MODE_QR;


public class CreateSpacePresenter implements
        CreateSpaceContract.UserActionsListener {

    private static final String TAG = CreateSpaceContract.class.getSimpleName();
    public static final int REQUEST_CODE = 0xd903;
    public static final String URI_HOST = "securespaces.com";
    public static final String URI_PATH = "/registerSpace";
    public static final String BRANDED_URI_PATH = "/generateSpace";
    public static final String PARAM_TOKEN = "token";
    public static final String PARAM_SERVER_URL = "server";

    private Activity mActivity;
    private FragmentSpacesCreator mFragment;
    private CreateSpaceContract.UserInputListener mUserInputView;

    private SpaceDescriptor.Factory mSpaceFactory = new SpaceDescriptor.Factory(mActivity);

    public CreateSpacePresenter(Activity activity, FragmentSpacesCreator fragment) {
        mActivity = activity;
        mUserInputView = fragment;
        mFragment = fragment;
    }

    public CreateSpacePresenter(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void addSpace(int mode) {

        if(mUserInputView.isManaged())
            mSpaceFactory.setManaged();
        if(mUserInputView.isEncrypted())
            mSpaceFactory.setEncrypted();

        switch (mode) {
            case CREATE_MODE_CLICK:
                mSpaceFactory.setName(mUserInputView.getSpaceName());
                CreateSpaceTask.createSpace(mActivity, mSpaceFactory, false, mUserInputView.isControlled());
                break;

            case CREATE_MODE_QR:
                scanQrCode();
                break;
        }


    }

    public void scanQrCode() {
        if (!SettingUtils.isNetworkAvailable(mActivity)) {
            Toast.makeText(mActivity, R.string.toast_no_network,
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = SystemUtils.getSpaceQRCodeIntent(mActivity, mActivity.getResources()
                .getString(R.string.qr_code_message));
        if (intent != null) {
            mFragment.startActivityForResult(intent, REQUEST_CODE);
        } else {
            Toast.makeText(mActivity, R.string.toast_no_qr_scanner, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onQrCodeScanned(String uriString) {
        uriString = uriString + "&internal=true";
        Log.d(TAG, "uri = " + uriString);
        Uri uri = Uri.parse(uriString);
        Log.d(TAG, "path = " + uri.getPath());

        // Test to see if the URI is the correct scheme and host format.
        if (uri.getScheme() == null
                || (!(uri.getScheme().equalsIgnoreCase("http")) && !(uri.getScheme()
                .equalsIgnoreCase("https")))) {
            Log.w(TAG, "Missing or invalid URI scheme: " + uri.getScheme());
            String message = mActivity.getResources().getString(R.string.uri_invalid);

            Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
            return;
        }
        if (uri.getHost() == null || !(uri.getHost().equalsIgnoreCase(URI_HOST))) {
            Log.w(TAG, "Missing or invalid host: " + uri.getHost());
            String message = mActivity.getResources().getString(R.string.uri_invalid);

            Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
            return;
        }
        if (uri.getPath() == null ||
                !(uri.getPath().equalsIgnoreCase(URI_PATH) ||
                        uri.getPath().equalsIgnoreCase(BRANDED_URI_PATH))) {
            Log.w(TAG, "Missing or invalid path: " + uri.getPath());
            String message = mActivity.getResources().getString(R.string.uri_invalid);

            Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
            return;
        }
        String token = uri.getQueryParameter(PARAM_TOKEN);
        if (TextUtils.isEmpty(token)) {
            String message = mActivity.getResources().getString(R.string.uri_missing_registration_token);
            Log.w(TAG, message);
            Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
            return;
        }
        //boolean internal = uri.getBooleanQueryParameter("internal", false);
        //boolean branded = uri.getPath().equalsIgnoreCase(BRANDED_URI_PATH);

        // FIXME
        onAccept(uriString);
    }

    public void onAccept(String uriString) {
        Uri uri = Uri.parse(uriString);
        String server = uri.getQueryParameter(PARAM_SERVER_URL);
        if (TextUtils.isEmpty(server) ||
                (!URLUtil.isHttpUrl(server) && !URLUtil.isHttpsUrl(server))) {
            String message = mActivity.getResources().getString(R.string.uri_invalid_server_url);
            Log.w(TAG, message);

            return;
        }

        mSpaceFactory.setRegistrationUri(uri);
        if (mUserInputView != null)
            CreateSpaceTask.createSpace(mActivity, mSpaceFactory, true, mUserInputView.isControlled());
        else
            CreateSpaceTask.createSpace(mActivity, mSpaceFactory, true, false);
    }

}
