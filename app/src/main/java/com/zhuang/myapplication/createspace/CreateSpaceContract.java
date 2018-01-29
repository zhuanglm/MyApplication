package com.zhuang.myapplication.createspace;

/**
 * Created by raymond on 10/11/16.
 */

public interface CreateSpaceContract {

    interface UserInputListener {
        String getSpaceName();

        boolean isManaged();

        boolean isEncrypted();

        boolean isControlled();


    }

    interface UserActionsListener {
        //void scanQrCode();

        void onQrCodeScanned(String uriString);

        void addSpace(int mode);
    }
}
