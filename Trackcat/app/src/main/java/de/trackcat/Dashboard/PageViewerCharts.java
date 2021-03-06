package de.trackcat.Dashboard;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.trackcat.Charts.BarChartFragment;
import de.trackcat.Database.DAO.RouteDAO;
import de.trackcat.Database.Models.Route;
import de.trackcat.MainActivity;
import de.trackcat.R;
import de.trackcat.Recording.Recording_UI.CurrentPageIndicator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PageViewerCharts extends Fragment {
    private int colorAccent;
    private List<Fragment> listFragments = new ArrayList<>();


    public PageViewerCharts() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_page_viewer_charts, container, false);

        /* Get AccentColor for current Theme */
        if (MainActivity.getDarkTheme()) {
            colorAccent = getResources().getColor(R.color.colorGreyAccent);
        } else {
            colorAccent = getResources().getColor(R.color.colorGreenAccent);
        }

        /* Read Last seven Days from DB and init various Variables */
        RouteDAO dao = new RouteDAO(MainActivity.getInstance());
        List<Route> records = dao.readLastSevenDays();
        double[] distanceArrayKm = {0, 0, 0, 0, 0, 0, 0, 0, 0},
                timeArray = {0, 0, 0, 0, 0, 0, 0, 0, 0},
                timeArrayMinutes = {0, 0, 0, 0, 0, 0, 0, 0, 0},
                timeArrayHours = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        double distance = 0,
                time = 0,
                maxDistanceKm = 0,
                maxTime = 0,
                maxTimeHours = 0;
        int prevDay = 0;
        int curDay = getWeekDay(System.currentTimeMillis());

        /* If the list has routes, this will be handled. If not, the Graph will be empty */
        if (records.size() > 0) {
            for (int i = 0; i < records.size(); i++) {
                long curDate = records.get(i).getDate();
                double curDistanceKm = records.get(i).getDistance() / 1000;
                double curTime = records.get(i).getTime();
                int dayOfWeek = getWeekDay(curDate);

                /* Depending on the Current day of Week,
                   this will shift the data in the array to the correct index for the corresponding Labels */
                if (curDay == 1) {
                    dayOfWeek = dayOfWeek + 6;
                } else if (curDay == 2) {
                    dayOfWeek = dayOfWeek + 5;
                } else if (curDay == 3) {
                    dayOfWeek = dayOfWeek + 4;
                } else if (curDay == 4) {
                    dayOfWeek = dayOfWeek + 3;
                } else if (curDay == 5) {
                    dayOfWeek = dayOfWeek + 2;
                } else if (curDay == 6) {
                    dayOfWeek = dayOfWeek + 1;
                }
                /* if a value exceeds 7, it will substract 7 to start from 0 with the exceeding value */
                if (dayOfWeek > 7) {
                    dayOfWeek = dayOfWeek - 7;
                }

                /* If the weekDay is equal to previous Date, the variables add up */
                if (dayOfWeek == prevDay) {
                    distance = distance + curDistanceKm;
                    time += curTime;
                    /* If the Weekday is not equal, the variables will be reset with new values */
                } else {
                    prevDay = dayOfWeek;
                    distance = curDistanceKm;
                    time = curTime;
                }
                /* If a distance or time is greater than befores maxVal, this variable will be overwritten */
                if (maxDistanceKm < distance) {
                    maxDistanceKm = distance;
                }
                if (maxTimeHours < time) {
                    maxTime = time;
                }

                /* Each time the loop iterates, the current time and distance are written to these arrays on dayOfWeek position */
                distanceArrayKm[dayOfWeek] = distance;
                timeArray[dayOfWeek] = time;
                timeArrayMinutes[dayOfWeek] = time / 60;
                timeArrayHours[dayOfWeek] = time / 3600;
            }
        }
        /* Bundle for the distance Graph */
        Bundle bundleDistance = new Bundle();
        bundleDistance.putString("title", "Distanz der Woche");
        bundleDistance.putInt("color", colorAccent);
        bundleDistance.putDoubleArray("array", distanceArrayKm);
        bundleDistance.putString("rangeTitle", "Km");
        bundleDistance.putDouble("stepsY", (maxDistanceKm) / 5);
        bundleDistance.putInt("curDay", curDay);

        BarChartFragment barFragDistance = new BarChartFragment();
        barFragDistance.setArguments(bundleDistance);

        /* Bundle for the time Graph */
        Bundle bundleTime = new Bundle();
        bundleTime.putString("title", "Laufzeit der Woche");
        bundleTime.putInt("color", colorAccent);

        /* Determines if seconds, minutes or hours should be displayed. Prevents too long decimals */
        if (maxTime < 60) {
            bundleTime.putDouble("stepsY", maxTime / 5);
            bundleTime.putString("rangeTitle", "Sekunden");
            bundleTime.putDoubleArray("array", timeArray);
        } else if (maxTime < 3600) {
            bundleTime.putDouble("stepsY", (maxTime / 60) / 5);
            bundleTime.putString("rangeTitle", "Minuten");
            bundleTime.putDoubleArray("array", timeArrayMinutes);
        } else if (maxTime >= 3600) {
            bundleTime.putDouble("stepsY", (maxTime / 3600) / 5);
            bundleTime.putString("rangeTitle", "Stunden");
            bundleTime.putDoubleArray("array", timeArrayHours);
        }

        bundleTime.putInt("curDay", curDay);

        BarChartFragment barFragTime = new BarChartFragment();
        barFragTime.setArguments(bundleTime);

        listFragments.add(barFragDistance);
        listFragments.add(barFragTime);

        /* Instantiate a ViewPager and a PagerAdapter. */
        ViewPager mPager = view.findViewById(R.id.pager);
        PagerAdapter mPagerAdapter = new PageViewerCharts.ScreenSlidePagerAdapter(MainActivity.getInstance().getSupportFragmentManager());

        mPager.setAdapter(mPagerAdapter);

        LinearLayout mLinearLayout = view.findViewById(R.id.indicator);

        /* create Indicator (little buttons) */
        if (Build.VERSION.SDK_INT > 21) {
            CurrentPageIndicator mIndicator = new CurrentPageIndicator(MainActivity.getInstance(), mLinearLayout, mPager, R.drawable.indicator_circle_theme_color);
            mIndicator.setPageCount(listFragments.size());
            mIndicator.show();
        } else {
            CurrentPageIndicator mIndicator = new CurrentPageIndicator(MainActivity.getInstance(), mLinearLayout, mPager, R.drawable.indicator_circle_v21);
            mIndicator.setPageCount(listFragments.size());
            mIndicator.show();
        }
        return view;
    }

    /* The weekDay of a date in millis will be returned as int (1 / Sunday to 7 / Saturday */
    /* Date can't be before 1. January 1970 */
    private int getWeekDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return dayOfWeek;
    }

    /* Returns the mod of every given number */
    private int mod(int x, int mod) {
        int result = x % mod;
        return result < 0 ? result + mod : result;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /* called on Swipe */
        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return listFragments.get(position);
        }

        @Override
        public int getCount() {
            return listFragments.size();
        }
    }
}
