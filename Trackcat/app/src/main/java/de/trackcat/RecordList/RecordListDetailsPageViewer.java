package de.trackcat.RecordList;

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

import de.trackcat.MainActivity;
import de.trackcat.R;
import de.trackcat.Recording.Recording_UI.CurrentPageIndicator;

import java.util.ArrayList;
import java.util.List;

public class RecordListDetailsPageViewer extends Fragment {


    private List<Fragment> listFragments = new ArrayList<>();

    public RecordListDetailsPageViewer() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate layout */
        View view = inflater.inflate(R.layout.fragment_page_viewer_charts, container, false);

        /* Show general information */
        int id = getArguments().getInt("id");
        String locationsAsString = getArguments().getString("locations");
        boolean temp = getArguments().getBoolean("temp");
        Bundle bundleInformation = new Bundle();
        bundleInformation.putInt("id", id);
        bundleInformation.putString("locations", locationsAsString);
        bundleInformation.putBoolean("temp", temp);

        RecordDetailsInformationFragment recordDetailsInformationFragment = new RecordDetailsInformationFragment();
        recordDetailsInformationFragment.setArguments(bundleInformation);
        listFragments.add(recordDetailsInformationFragment);

        /* Show charts */
        double[] speedValues = getArguments().getDoubleArray("speedArray");
        double[] altitudeValues = getArguments().getDoubleArray("altitudeArray");

        Bundle bundleCharts = new Bundle();
        bundleCharts.putDoubleArray("speedArray", speedValues);
        bundleCharts.putDoubleArray("altitudeArray", altitudeValues);
        RecordDetailsChartsFragment recordDetailsCharts = new RecordDetailsChartsFragment();
        recordDetailsCharts.setArguments(bundleCharts);
        listFragments.add(recordDetailsCharts);

        /* Instantiate ViewPagers */
        ViewPager mPager = view.findViewById(R.id.pager);
        PagerAdapter mPagerAdapter = new RecordListDetailsPageViewer.ScreenSlidePagerAdapter(MainActivity.getInstance().getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        LinearLayout mLinearLayout = view.findViewById(R.id.indicator);

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

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /* Called on Swipe */
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
