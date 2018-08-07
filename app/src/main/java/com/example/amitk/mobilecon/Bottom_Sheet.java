package com.example.amitk.mobilecon;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Bottom_Sheet extends BottomSheetDialogFragment {
    TextView service_station_name,service_station_address,service_station_contact;

    String station_name,station_address,station_contact;
    public Bottom_Sheet(){
    }

    @SuppressLint("ValidFragment")
    public Bottom_Sheet(String station_name, String station_contact , String station_address ){
        this.station_name = station_name;
        this.station_contact = station_contact;
        this.station_address = station_address;
    }

    private BottomSheetBehavior.BottomSheetCallback
            mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }

    };


    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View contentView = View.inflate(getContext(), R.layout.fragment_bottom__sheet, null);

        ((Chat)getActivity()).FragmentCallMethod();

        dialog.setContentView(contentView);
    }
}
