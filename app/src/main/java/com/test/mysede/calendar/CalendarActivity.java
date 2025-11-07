package com.test.mysede.calendar;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.test.mysede.R;
import com.test.mysede.calendar.CalendarFragment;

public class CalendarActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_MODE = CalendarFragment.ARG_INITIAL_MODE;
    public static final String MODE_WEEK = CalendarFragment.MODE_WEEK;
    public static final String MODE_MONTH = CalendarFragment.MODE_MONTH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        if (savedInstanceState == null) {
            String initialMode = getIntent().getStringExtra(EXTRA_INITIAL_MODE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.calendar_host_container, CalendarFragment.newInstance(initialMode))
                    .commit();
        }
    }
}
