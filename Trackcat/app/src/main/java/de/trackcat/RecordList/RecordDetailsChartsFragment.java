package de.trackcat.RecordList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.trackcat.Charts.LineChartFragment;
import de.trackcat.R;

public class RecordDetailsChartsFragment extends Fragment {

    private FragmentTransaction fragTransaction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Read data from bundle */
        double[] speedValues = getArguments().getDoubleArray("speedArray");
        double[] altitudeValues = getArguments().getDoubleArray("altitudeArray");

        /* Altimeterchart */
        Bundle bundleAltitude = new Bundle();
        bundleAltitude.putDoubleArray("array", altitudeValues);
        bundleAltitude.putString("title", "Höhenmeter");
        bundleAltitude.putString("rangeTitle", "m");

        LineChartFragment lineFragAltitude = new LineChartFragment();
        lineFragAltitude.setArguments(bundleAltitude);

        /* Speedchart */
        Bundle bundleSpeed = new Bundle();
        bundleSpeed.putDoubleArray("array", speedValues);
        bundleSpeed.putString("title", "Geschwindigkeit");
        bundleSpeed.putString("rangeTitle", "km/h");

        LineChartFragment lineFragSpeed = new LineChartFragment();
        lineFragSpeed.setArguments(bundleSpeed);

        /* Add charts to container */
        fragTransaction = getChildFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.speedContainer, lineFragSpeed, getResources().getString(R.string.fRecordDetailsChartSpeed));
        fragTransaction.replace(R.id.altitudeContainer, lineFragAltitude, getResources().getString(R.string.fRecordDetailsChartAltitude));
        fragTransaction.commit();

        View view = inflater.inflate(R.layout.fragment_record_details_charts, container, false);
        return view;
    }
}
