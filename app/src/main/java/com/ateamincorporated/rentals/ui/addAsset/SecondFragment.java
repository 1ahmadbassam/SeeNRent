package com.ateamincorporated.rentals.ui.addAsset;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.ateamincorporated.rentals.R;

import java.util.Calendar;

public class SecondFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final int START_DATE_PICK = 0;
    private static final int END_DATE_PICK = 1;
    private static final String TIME_PREFIX_BASE = "Available ";
    private static final String TIME_PREFIX_START = "from ";
    private static final String TIME_PREFIX_END = "until ";
    private static final String STARTING_DATE_MISSING_ERROR = "You have to set the starting date and time first!";
    private static final String ENDING_DATE_MISSING_ERROR = "You have to set the ending date and time first!";
    private static final String[] PRICE_UNITS = new String[]{
            "Hour", "Day", "Week", "Month"
    };
    private final Calendar mStartCalendar = Calendar.getInstance();
    private final Calendar mEndCalendar = Calendar.getInstance();
    private CalendarView mCalendarView;
    private SecondViewModel mViewModel;

    private int dateTimePickMode = 0;
    private boolean hasStartDate = false;
    private boolean hasEndDate = false;
    private boolean comingFromFourthFragment = false;
    private String priceUnit;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(SecondViewModel.class);
        View root = inflater.inflate(R.layout.fragment_add_asset_second, container, false);
        final EditText priceField = root.findViewById(R.id.price_field);

        final TextView priceErrorView = root.findViewById(R.id.error_price);

        // Check if we already have a category, so we are coming from the review view
        Bundle bundle = getArguments();
        if (bundle == null) {
            throw new IllegalStateException();
        }
        if (bundle.containsKey("category")) {
            mStartCalendar.setTimeInMillis(Long.parseLong(bundle.getString("startDate")));
            mEndCalendar.setTimeInMillis(Long.parseLong(bundle.getString("endDate")));
            priceField.setText(bundle.getString("price"));
            hasStartDate = true;
            mViewModel.setStartDate(TIME_PREFIX_BASE + TIME_PREFIX_START + java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(mStartCalendar.getTime()));
            hasEndDate = true;
            mViewModel.setEndDate(TIME_PREFIX_BASE + TIME_PREFIX_END + java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(mEndCalendar.getTime()));
            comingFromFourthFragment = true;
        }

        mCalendarView = root.findViewById(R.id.date_view);
        mCalendarView.setMinDate(System.currentTimeMillis() - 1000);
        mCalendarView.setDate(mStartCalendar.getTimeInMillis(), false, true);

        TextView mStartTimeDisplay = root.findViewById(R.id.start_time_display);
        mStartTimeDisplay.setOnClickListener(v -> mCalendarView.setDate(mStartCalendar.getTimeInMillis()));
        mStartTimeDisplay.setPaintFlags(mStartTimeDisplay.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        TextView mEndTimeDisplay = root.findViewById(R.id.end_time_display);
        mEndTimeDisplay.setOnClickListener(v -> mCalendarView.setDate(mEndCalendar.getTimeInMillis()));
        mEndTimeDisplay.setPaintFlags(mEndTimeDisplay.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        final Button startDateTime = root.findViewById(R.id.set_start_date_time);
        startDateTime.setOnClickListener(v -> {
            // Set date picker
            dateTimePickMode = START_DATE_PICK;
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), SecondFragment.this, mStartCalendar.get(Calendar.YEAR), mStartCalendar.get(Calendar.MONTH), mStartCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
        Button mEndDateTime = root.findViewById(R.id.set_end_date_time);
        mEndDateTime.setOnClickListener(v -> {
            if (!hasStartDate) {
                Toast.makeText(getActivity(), STARTING_DATE_MISSING_ERROR, Toast.LENGTH_SHORT).show();
            } else {
                // Set date picker
                dateTimePickMode = END_DATE_PICK;
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), SecondFragment.this, mStartCalendar.get(Calendar.YEAR), mStartCalendar.get(Calendar.MONTH), mStartCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(mStartCalendar.getTimeInMillis() - 1000);
                datePickerDialog.show();
            }
        });

        // Handle price management per unit time
        final Spinner priceUnitsSpinner = root.findViewById(R.id.price_unit);
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, PRICE_UNITS);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        priceUnitsSpinner.setAdapter(spinnerAdapter);
        if (comingFromFourthFragment) {
            priceUnit = bundle.getString("priceUnit");
            priceUnitsSpinner.setSelection(spinnerAdapter.getPosition(priceUnit));
        } else {
            priceUnit = PRICE_UNITS[0];
            priceUnitsSpinner.setSelection(0, false);
        }
        priceUnitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                priceUnit = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Button next = root.findViewById(R.id.next);
        next.setOnClickListener(v -> {
            final String priceString = priceField.getText().toString();
            long price;
            if (TextUtils.isEmpty(priceString)) {
                price = 0;
            } else {
                price = Long.parseLong(priceString);
            }
            if (!hasStartDate) {
                Toast.makeText(getActivity(), STARTING_DATE_MISSING_ERROR, Toast.LENGTH_SHORT).show();
            } else if (!hasEndDate) {
                Toast.makeText(getActivity(), ENDING_DATE_MISSING_ERROR, Toast.LENGTH_SHORT).show();
            } else if (price == 0 || !(price % 250 == 0) || !(priceString.matches("[0-9]+") && priceString.length() > 2)) {
                unHideView(priceErrorView);
                final TextWatcher watcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        hideView(priceErrorView);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                };
                priceField.addTextChangedListener(watcher);
            } else if (bundle.containsKey("category")) {
                bundle.putString("startDate", String.valueOf(mStartCalendar.getTimeInMillis()));
                bundle.putString("endDate", String.valueOf(mEndCalendar.getTimeInMillis()));
                bundle.putString("priceUnit", priceUnit);
                bundle.putString("price", priceString);
                Navigation.findNavController(v).navigate(R.id.second_to_fourth, bundle);
            } else {
                bundle.putString("startDate", String.valueOf(mStartCalendar.getTimeInMillis()));
                bundle.putString("endDate", String.valueOf(mEndCalendar.getTimeInMillis()));
                bundle.putString("priceUnit", priceUnit);
                bundle.putString("price", priceString);
                Navigation.findNavController(v).navigate(R.id.second_to_third, bundle);
            }
        });

        mViewModel.getStartDate().observe(getViewLifecycleOwner(), mStartTimeDisplay::setText);
        mViewModel.getEndDate().observe(getViewLifecycleOwner(), mEndTimeDisplay::setText);

        return root;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Start time picker
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), SecondFragment.this, mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.show();
        // Parse start and end dates
        if (dateTimePickMode == START_DATE_PICK) {
            mStartCalendar.set(year, month, dayOfMonth);
            mCalendarView.setDate(mStartCalendar.getTimeInMillis());
        } else if (dateTimePickMode == END_DATE_PICK) {
            mEndCalendar.set(year, month, dayOfMonth);
            mCalendarView.setDate(mEndCalendar.getTimeInMillis());
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (dateTimePickMode == START_DATE_PICK) {
            Calendar isValidCalendar = Calendar.getInstance();
            isValidCalendar.set(mStartCalendar.get(Calendar.YEAR), mStartCalendar.get(Calendar.MONTH), mStartCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
            if (isValidCalendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() + 3600000) {
                mStartCalendar.setTimeInMillis(mStartCalendar.getTimeInMillis() + 86400000);
                mCalendarView.setDate(mStartCalendar.getTimeInMillis());
//                hourOfDay = mStartCalendar.get(Calendar.HOUR_OF_DAY);
//                minute = mStartCalendar.get(Calendar.MINUTE);
            }
            mStartCalendar.set(mStartCalendar.get(Calendar.YEAR), mStartCalendar.get(Calendar.MONTH), mStartCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, 0);
            mViewModel.setStartDate(TIME_PREFIX_BASE + TIME_PREFIX_START + java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(mStartCalendar.getTime()));
            if (hasEndDate && (mEndCalendar.getTimeInMillis() < mStartCalendar.getTimeInMillis() + 3600000)) {
                mEndCalendar.setTimeInMillis(mStartCalendar.getTimeInMillis() + 86400000);
                mViewModel.setEndDate(TIME_PREFIX_BASE + TIME_PREFIX_END + java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(mEndCalendar.getTime()));
            }

            hasStartDate = true;
        } else if (dateTimePickMode == END_DATE_PICK) {
            Calendar isValidCalendar = Calendar.getInstance();
            isValidCalendar.set(mEndCalendar.get(Calendar.YEAR), mEndCalendar.get(Calendar.MONTH), mEndCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
            if ((isValidCalendar.getTimeInMillis() < mStartCalendar.getTimeInMillis() + 3600000)) {
                mEndCalendar.setTimeInMillis(mEndCalendar.getTimeInMillis() + 86400000);
                mCalendarView.setDate(mEndCalendar.getTimeInMillis());
//                hourOfDay = mEndCalendar.get(Calendar.HOUR_OF_DAY);
//                minute = mEndCalendar.get(Calendar.MINUTE);
            }
            mEndCalendar.set(mEndCalendar.get(Calendar.YEAR), mEndCalendar.get(Calendar.MONTH), mEndCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, 0);
            mViewModel.setEndDate(TIME_PREFIX_BASE + TIME_PREFIX_END + java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(mEndCalendar.getTime()));

            hasEndDate = true;
        }
    }


    private static void unHideView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    private static void hideView(View v) {
        v.setVisibility(View.INVISIBLE);
    }
}