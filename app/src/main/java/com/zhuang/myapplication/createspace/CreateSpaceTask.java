package com.zhuang.myapplication.createspace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;

import com.securespaces.android.ssm.SpaceDescriptor;
import com.securespaces.android.ssm.SpaceInfo;
import com.securespaces.android.ssm.SpacesManager;
import com.securespaces.debug.R;


public class CreateSpaceTask extends AsyncTask<Void, String, UserHandle> {
    private static final String TAG = CreateSpaceTask.class.getSimpleName();

    private SpaceDescriptor.Factory mSpaceFactory;
    private boolean mIsControlled;
    private boolean mIsQR;
    private Context mContext;
    private ProgressDialog mProgressDialog;

    public static void createSpace(Activity activityContext, SpaceDescriptor.Factory spaceFactory,
                                   boolean isQR, boolean isControlled) {
        if (activityContext == null) {
            Log.e(TAG, "createSpace - context is null, abort");
            return;
        }
        CreateSpaceTask task = new CreateSpaceTask(activityContext, spaceFactory, isQR, isControlled);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public CreateSpaceTask(Context context, SpaceDescriptor.Factory spaceFactory,
                           boolean isQR, boolean isControlled) {
        mIsControlled = isControlled;
        mIsQR = isQR;
        mSpaceFactory = spaceFactory;
        mContext = context;
        mProgressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setTitle(mContext.getString(R.string.dialog_space_creating_title));
        mProgressDialog.setMessage(mContext.getString(R.string.dialog_space_creating_text));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }


    @Override
    protected void onProgressUpdate(String... values) {
        if (values != null) {
            for (String value : values) {
                Toast.makeText(mContext, value, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected UserHandle doInBackground(Void... params) {
        if (mContext == null) {
            return null;
        }
        SpacesManager spacesManager = new SpacesManager(mContext.getApplicationContext());

        SpaceInfo spaceInfo;
        UserHandle spaceHandle;

        if (mIsControlled) {
            spaceHandle = spacesManager.createAppControlledSpace(mSpaceFactory.getName(),
                    mSpaceFactory.getFlags(),mContext.getPackageName(), null);

            //QR create APP Controlled space
            if (spaceHandle != null && mIsQR) {
                spacesManager.provisionSpace(mSpaceFactory.getRegistrationUri(), spaceHandle);
            }

        } else {
            spaceInfo = spacesManager.createSpace(mSpaceFactory.build());

            if (spaceInfo == null) {
                publishProgress(mContext.getResources().getString(R.string.toast_space_creation_failed));
                return null;
            }
            Log.d(TAG, "Created new space: " + spaceInfo.id);
            spaceHandle = spaceInfo.getUserHandle();
        }

        return spaceHandle;

    }

    @Override
    protected void onPostExecute(UserHandle userHandle) {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (userHandle == null) {
            Toast.makeText(mContext, R.string.toast_space_creation_failed, Toast.LENGTH_LONG).show();
            return;
        }
    }
}
