package de.trackcat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.karan.churi.PermissionManager.PermissionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import de.trackcat.Database.DAO.UserDAO;
import de.trackcat.Database.Models.User;
import de.trackcat.LogIn.LogInFragment;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StartActivity extends AppCompatActivity {

    private FragmentTransaction fragTransaction;
    private UserDAO userDAO;
    private static StartActivity instance;
    private int delay = 2000;
    private boolean fastLogIn;
    int userCount;

    public ProgressDialog progressDialog;

    private PermissionManager permissionManager = new PermissionManager() {
    };

    public static StartActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Check permission */
        permissionManager.checkAndRequestPermissions(this);

        setContentView(R.layout.activity_start);

        /* Show loadScreen if necessary */
        try {
            if (getIntent().getExtras().getBoolean("isLogout")) {
                delay = 0;
            }
        } catch (Exception e) {
        }

        /* Set instance */
        instance = this;

        /* Set dao and check if user in db */
        userDAO = new UserDAO(this);
        userCount = userDAO.userInDB();

        /* Get active user */
        if (userCount > 0) {

            List<User> userList = userDAO.readAll();
            User currentUser = userList.get(0);

            /* Read profile values from global db */
            HashMap<String, String> map = new HashMap<>();
            map.put("id", "" + currentUser.getId());

            Retrofit retrofit = APIConnector.getRetrofit();
            APIClient apiInterface = retrofit.create(APIClient.class);

            /* Start a call */
            String base = currentUser.getMail() + ":" + currentUser.getPassword();
            String authString = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);
            Call<ResponseBody> call = apiInterface.getUserById(authString, map);
            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {

                        if (response.code() == 401) {
                            fastLogIn = false;
                        } else {
                            /* Get jsonString from API */
                            String jsonString = response.body().string();

                            /* Parse json */
                            JSONObject userJSON = new JSONObject(jsonString);
                            Log.d(getResources().getString(R.string.app_name) + "-ProfileInformation", "Profilinformation erhalten von: " + userJSON.getString("firstName") + " " + userJSON.getString("lastName"));
                            fastLogIn = true;
                        }
                        showLoginPage();

                    } catch (Exception e) {
                        fastLogIn = true;
                        showLoginPage();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                    fastLogIn = true;
                    showLoginPage();
                }
            });
        } else {
            fastLogIn = false;
            showLoginPage();
        }
    }

    private void showLoginPage() {

        /* LogIn Screen */
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        /* Show login page if no user is logged in */
                        if (userCount == 0 || !fastLogIn) {
                            fragTransaction = getSupportFragmentManager().beginTransaction();
                            fragTransaction.replace(R.id.mainFrame, new LogInFragment(),
                                    getResources().getString(R.string.fLogIn));
                            fragTransaction.commitAllowingStateLoss();

                            /* Delete user */
                            List<User> deletedUsers = userDAO.readAll();
                            for (User user : deletedUsers) {
                                userDAO.delete(user);
                            }

                            /* Logged user in, if one entry in table */
                        } else {
                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                }, delay);
    }

    @Override
    protected void onDestroy() {
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            e.toString();
        }
        super.onDestroy();
    }
}

