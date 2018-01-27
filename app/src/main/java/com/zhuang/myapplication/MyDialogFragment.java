package com.zhuang.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.Toast;

/**
 * Created by Raymond on 2018-01-26.
 */

public class MyDialogFragment extends DialogFragment {
    private DialogInterface.OnClickListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mListener = (DialogInterface.OnClickListener) context;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if(args!=null) {
            String arguments = args.getString("arg1");
            Toast.makeText(getContext(),"arguments:"+arguments,Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("title")
                .setMessage("what would you like to do")
                .setPositiveButton("Yes", mListener)
                .setNegativeButton("No",mListener)
                .setCancelable(false);

        return builder.create();
    }

}
