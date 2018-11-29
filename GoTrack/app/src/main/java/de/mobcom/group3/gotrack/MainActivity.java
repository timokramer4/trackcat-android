package de.mobcom.group3.gotrack;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.karan.churi.PermissionManager.PermissionManager;

import de.mobcom.group3.gotrack.Dashboard.DashboardFragment;
import de.mobcom.group3.gotrack.Recording.RecordFragment;
import de.mobcom.group3.gotrack.Settings.CustomSpinnerAdapter;
import de.mobcom.group3.gotrack.Settings.SettingsFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Standardwerte für die Abfrage der Berechtigungen
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private PermissionManager permissionManager = new PermissionManager() {};
    final int NOTIFICATION_ID = 100;
    private DrawerLayout mainDrawer;
    private static MainActivity instance;
    private RecordFragment recordFragment;
    private NotificationManagerCompat notificationManager;
    private Spinner spinner;

    private static final String PREF_DARK_THEME = "dark_theme";

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getStringExtra("action");
        if (action != null && action.equalsIgnoreCase("RECORD")) {
            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
            fragTransaction.replace(R.id.mainFrame, recordFragment, "RECORD");
            fragTransaction.commit();
        } else if (action != null && action.equalsIgnoreCase("SETTINGS")) {
            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
            fragTransaction.replace(R.id.mainFrame, new SettingsFragment(), "SETTINGS");
            fragTransaction.commit();
        }
    }

    @Override
    protected void onDestroy() {
        // Entferne die Benachrichtigung, wenn App läuft
        notificationManager.cancel(getNOTIFICATION_ID());
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Fragt nach noch nicht erteilten Permissions
        permissionManager.checkAndRequestPermissions(this);
        
        // Aktuelles Themes aus Einstellungen laden
        setTheme(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_DARK_THEME, false) ? R.style.AppTheme_Dark : R.style.AppTheme);

        // Startseite definieren
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Startseite festlegen - Erster Aufruf
        FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.mainFrame, new DashboardFragment(), "DASHBOARD");
        fragTransaction.commit();

        // Instanz für spätere Objekte speichern
        instance = this;
        recordFragment = new RecordFragment();
        mainDrawer = findViewById(R.id.drawer_layout);

        // Actionbar definieren und MenuListener festlegen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Menu Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mainDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mainDrawer.addDrawerListener(toggle);
        toggle.syncState();

        notificationManager = NotificationManagerCompat.from(this);

        // TODO Profilwechsel
        spinner = navigationView.getHeaderView(0).findViewById(R.id.profile_spinner);
        addItemsToSpinner();

        /*
         + Check if permissions granted for GPS service and storage access
         + toDo: verify why location and storage permissions aren't requested simultaneously
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    // add items into spinner dynamically
    public void addItemsToSpinner() {

        /* Erstellen der Listen */
        final ArrayList<Integer> spinnerAccountIcons = new ArrayList<Integer>();
        spinnerAccountIcons.add(R.raw.default_profile);
        spinnerAccountIcons.add(R.raw.default_profile);
        spinnerAccountIcons.add(R.raw.default_nav_background);

        ArrayList<String> spinnerAccountEmail = new ArrayList<String>();
        spinnerAccountEmail.add("alorma@github.com ");
        spinnerAccountEmail.add("mikapenz@gmail.com");
        spinnerAccountEmail.add("max.mustermann@web.de");

        final ArrayList<String> spinnerAccountNames = new ArrayList<String>();
        spinnerAccountNames.add("Alorma Netz");
        spinnerAccountNames.add("Mika Penz");
        spinnerAccountNames.add("Max Mustermann");


        /* Erstellen des Custom Spinners */
        final CustomSpinnerAdapter spinAdapter = new CustomSpinnerAdapter(
                getApplicationContext(), spinnerAccountIcons, spinnerAccountNames, spinnerAccountEmail);

        /* Setzen des Adapters */
        spinner.setAdapter(spinAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                /* Auslesen des angeklickten Items */
                String item = adapter.getItemAtPosition(position).toString();

                /* Wechseln des Profilbildes */
                int imgResource = spinnerAccountIcons.get(position);
                de.hdodenhof.circleimageview.CircleImageView circleImageView = findViewById(R.id.profile_image);
                circleImageView.setImageResource(imgResource);

                /* Überprüfung, ob Nutzerwechsel oder Nutzer bearbeiten */
                if (position >= spinnerAccountNames.size()) {
                    Toast.makeText(getApplicationContext(), item + " ausgewählt",
                            Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getApplicationContext(), "Ausgewähltes Profil: " + item,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) { }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // Aktion je nach Auswahl des Items
        switch (menuItem.getItemId()) {
            case R.id.nav_record:
                if (getSupportFragmentManager().findFragmentByTag("RECORD") == null) {
                    FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();

                    fragTransaction.replace(R.id.mainFrame, recordFragment, "RECORD");
                    fragTransaction.commit();
                }
                break;
            case R.id.nav_settings:
                if (getSupportFragmentManager().findFragmentByTag("SETTINGS") == null) {
                    FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();

                    fragTransaction.replace(R.id.mainFrame, new SettingsFragment(), "SETTINGS");
                    fragTransaction.commit();
                }
                break;
            case R.id.nav_dashboard:
                if (getSupportFragmentManager().findFragmentByTag("DASHBOARD") == null) {
                    FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();

                    fragTransaction.replace(R.id.mainFrame, new DashboardFragment(), "DASHBOARD");
                    fragTransaction.commit();
                }
                break;
        }
        menuItem.setChecked(true);
        mainDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Stops/pauses Tracking opens App and switch to RecordFragment
    public void stopTracking() {
        recordFragment.stopTracking();
        startActivity(getIntent());
        try {
            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
            fragTransaction.replace(R.id.mainFrame, recordFragment, "RECORD");
            fragTransaction.commit();
        } catch (RuntimeException e) {

        }
    }

    public int getNOTIFICATION_ID() {
        return NOTIFICATION_ID;
    }

    public void startTracking() {
        recordFragment.startTracking();
        startActivity(getIntent());
        try {
            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
            fragTransaction.replace(R.id.mainFrame, recordFragment, "RECORD");
            fragTransaction.commit();
        } catch (RuntimeException e) {

        }
    }

    // Startet RecordFragment nach Ende der Aufzeichnung
    public void endTracking() {
        // TODO switch to Statisitcs page
        recordFragment = new RecordFragment();
    }
}