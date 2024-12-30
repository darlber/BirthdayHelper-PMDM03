package com.example.birthdayhelper.Utils;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import android.app.TimePickerDialog;

public class TimePickerFragment extends DialogFragment {

    private TimePickerDialog.OnTimeSetListener onTimeSetListener;

    public void setOnTimeSetListener(TimePickerDialog.OnTimeSetListener listener) {
        this.onTimeSetListener = listener;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int hour = 12;
        int minute = 0;
        return new TimePickerDialog(getActivity(), onTimeSetListener, hour, minute, true);
    }
}
