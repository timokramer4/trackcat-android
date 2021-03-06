package de.trackcat.Dashboard;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import de.trackcat.MainActivity;
import de.trackcat.R;

public class DashboardFragment extends Fragment implements View.OnClickListener {
    private FragmentTransaction fragTransaction;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragTransaction = getChildFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.summaryContainer, new SummaryListFragment(), getResources().getString(R.string.fDashboardSummary));
        fragTransaction.replace(R.id.chartContainer, new PageViewerCharts(), getResources().getString(R.string.fPageViewer));
        fragTransaction.commit();

        /* Mark current menu point */
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_dashboard).setChecked(true);

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        FloatingActionButton fabButton = view.findViewById(R.id.fabButton);
        fabButton.setOnClickListener(this);

        /* Function behind the shortcut button */
        if (Build.VERSION.SDK_INT <= 21) {
            fabButton.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabButton:

                /* Load record */
                if (MainActivity.getHints()) {
                    Toast.makeText(getContext(), MainActivity.getInstance().getResources().getString(R.string.changeToRecording), Toast.LENGTH_LONG).show();
                }
                MainActivity.getInstance().loadRecord();

                /* Set menu checked */
                NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                Menu menu = navigationView.getMenu();
                menu.findItem(R.id.nav_record).setChecked(true);
                break;
        }
    }
}
