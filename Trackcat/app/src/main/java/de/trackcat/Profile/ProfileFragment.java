package de.trackcat.Profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import de.trackcat.APIClient;
import de.trackcat.APIConnector;
import de.trackcat.Database.DAO.UserDAO;
import de.trackcat.Database.Models.User;
import de.trackcat.GlobalFunctions;
import de.trackcat.MainActivity;
import de.trackcat.R;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileFragment extends Fragment {

    /* Variables */
    TextView name, email, dayOfBirth, gender, weight, size, bmi, lastLogIn, dayOfRegistration, amountRecords, totalTime, totalDistance;
    CircleImageView image, state;
    ImageView birthday, user_gender_image;
    RelativeLayout loadProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        boolean loadMenu = getArguments().getBoolean("loadMenu");

        if (loadMenu) {
            /* Inflate Menu */
            MenuInflater menuInflater = MainActivity.getInstance().getMenuInflater();
            menuInflater.inflate(R.menu.profile_settings, MainActivity.getMenuInstance());
        }

        /* Get current user */
        UserDAO userDAO = new UserDAO(MainActivity.getInstance());
        User currentUser = userDAO.read(MainActivity.getActiveUser());

        /* Get profile fields */
        name = view.findViewById(R.id.user_name);
        email = view.findViewById(R.id.user_email);
        dayOfBirth = view.findViewById(R.id.user_dayOfBirth);
        birthday = view.findViewById(R.id.user_birthday);
        gender = view.findViewById(R.id.user_gender);
        weight = view.findViewById(R.id.user_weight);
        size = view.findViewById(R.id.user_size);
        bmi = view.findViewById(R.id.user_bmi);
        state = view.findViewById(R.id.profile_state);
        amountRecords = view.findViewById(R.id.user_amount_records);
        totalTime = view.findViewById(R.id.user_amount_time_records);
        totalDistance = view.findViewById(R.id.user_amount_distance_records);
        lastLogIn = view.findViewById(R.id.user_lastLogIn);
        dayOfRegistration = view.findViewById(R.id.user_dayOfRegistration);
        image = view.findViewById(R.id.profile_image);
        user_gender_image = view.findViewById(R.id.user_gender_image);
        loadProfile = view.findViewById(R.id.loadScreen);

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
                        MainActivity.getInstance().showNotAuthorizedModal(0);
                    } else {
                        /* Get jsonString from API */
                        String jsonString = response.body().string();

                        /* Parse json */
                        JSONObject userJSON = new JSONObject(jsonString);
                        try {
                            Log.d(getResources().getString(R.string.app_name) + "-ProfileInformation", "Profilinformation erhalten von: " + userJSON.getString("firstName") + " " + userJSON.getString("lastName"));
                        } catch (Exception e) {
                        }
                        /* Check values an show  */
                        float size, weight;
                        long dateOfBirth;
                        byte[] image = null;

                        try {
                            size = (float) userJSON.getDouble("size");
                        } catch (Exception e) {
                            size = 0;
                        }

                        try {
                            weight = (float) userJSON.getDouble("weight");
                        } catch (Exception e) {
                            weight = 0;
                        }

                        try {
                            dateOfBirth = userJSON.getLong("dateOfBirth");
                        } catch (Exception e) {
                            dateOfBirth = 0;
                        }

                        if (userJSON.getString("image") != "null") {
                            image = GlobalFunctions.getBytesFromBase64(userJSON.getString("image"));
                        }

                        /* check if data is newer when localData */
                        if (userJSON.getLong("timeStamp") > currentUser.getTimeStamp()) {
                            userDAO.update(currentUser.getId(), GlobalFunctions.createUser(userJSON, true, false));
                        }

                        setProfileValues(userJSON.getString("firstName"), userJSON.getString("lastName"), userJSON.getString("email"), dateOfBirth, size, weight, userJSON.getInt("gender"), userJSON.getLong("dateOfRegistration"), userJSON.getLong("lastLogin"), userJSON.getLong("amountRecords"), userJSON.getLong("totalDistance"), userJSON.getLong("totalTime"), image);
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    /* read values from local DB */
                    setProfileValues(currentUser.getFirstName(), currentUser.getLastName(), currentUser.getMail(), currentUser.getDateOfBirth(), currentUser.getSize(), currentUser.getWeight(), currentUser.getGender(), currentUser.getDateOfRegistration(), currentUser.getLastLogin(), currentUser.getAmountRecord(), currentUser.getTotalDistance(), currentUser.getTotalTime(), currentUser.getImage());

                } catch (JSONException e) {
                    e.printStackTrace();

                    /* read values from local DB */
                    setProfileValues(currentUser.getFirstName(), currentUser.getLastName(), currentUser.getMail(), currentUser.getDateOfBirth(), currentUser.getSize(), currentUser.getWeight(), currentUser.getGender(), currentUser.getDateOfRegistration(), currentUser.getLastLogin(), currentUser.getAmountRecord(), currentUser.getTotalDistance(), currentUser.getTotalTime(), currentUser.getImage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                call.cancel();

                /* Read values from local DB */
                setProfileValues(currentUser.getFirstName(), currentUser.getLastName(), currentUser.getMail(), currentUser.getDateOfBirth(), currentUser.getSize(), currentUser.getWeight(), currentUser.getGender(), currentUser.getDateOfRegistration(), currentUser.getLastLogin(), currentUser.getAmountRecord(), currentUser.getTotalDistance(), currentUser.getTotalTime(), currentUser.getImage());
                Log.d(getResources().getString(R.string.app_name) + "-ProfileInformation", "ERROR: " + t.getMessage());
            }
        });

        return view;
    }

    /* Function to set profile information in fields */
    private void setProfileValues(String user_firstName, String user_lastName, String user_email, long user_dayOfBirth, float user_size, float user_weight, int user_gender, long user_dateOfRegistration, long user_lastLogin, long user_amountRecords, long user_totalDistance, long user_totalTime, byte[] user_image) {
        int age = 0;

        /* Set name and email */
        name.setText(user_firstName + " " + user_lastName);
        email.setText(user_email);

        /* Set dayOfBirth and calculate age*/
        if (user_dayOfBirth != 0) {
            String curDateString = GlobalFunctions.getDateFromMillis(user_dayOfBirth, "dd.MM.yyyy");
            age = calculateAge(curDateString);
            dayOfBirth.setText(curDateString + " (" + age + " Jahre)");

            /* If user have birthday */
            if (todayDayOfBirth) {
                birthday.setVisibility(View.VISIBLE);
            }
        } else {
            GlobalFunctions.setNoInformationStyle(dayOfBirth);
        }

        /* Set size */
        if (user_size != 0) {
            size.setText("" + user_size + " cm");
        } else {
            GlobalFunctions.setNoInformationStyle(size);
        }

        /* Set weight */
        if (user_weight != 0) {
            weight.setText("" + user_weight + " kg");
        } else {
            GlobalFunctions.setNoInformationStyle(weight);
        }

        /* Set gender */
        if (user_gender != 2) {
            InputStream imageStream;
            if (user_gender == 0) {
                gender.setText(getResources().getString(R.string.genderFemale));
                gender.setTextColor(getResources().getColor(R.color.colorFemale));
                imageStream = this.getResources().openRawResource(R.raw.female);
            } else {
                gender.setText(getResources().getString(R.string.genderMale));
                imageStream = this.getResources().openRawResource(R.raw.male);
                gender.setTextColor(getResources().getColor(R.color.colorMale));
            }
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            user_gender_image.setImageBitmap(bitmap);
            user_gender_image.setVisibility(View.VISIBLE);
        } else {
            GlobalFunctions.setNoInformationStyle(gender);
            user_gender_image.setVisibility(View.GONE);
        }

        /* Calculate bmi */
        if (user_size != 0 && user_weight != 0 && user_gender != 2 && user_dayOfBirth != 0) {

            float userSize = user_size;
            float userWeight = user_weight;
            float x = (userSize / 100) * (userSize / 100);
            double userBmi = Math.round((userWeight / x) * 100) / 100.0;

            String bmiClass = "";
            if (age < 8) {
                bmi.setText("Berechnung nicht möglich");
                GlobalFunctions.setNoInformationStyle(bmi);
            } else {
                /* if user is female */
                if (user_gender == 0) {
                    /*children*/
                    if (age == 8) {
                        if (userBmi < 13.2) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 13.3 && userBmi <= 18.7) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 18.8 && userBmi <= 22.2) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 22.3) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 9) {
                        if (userBmi < 13.7) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 13.8 && userBmi <= 19.7) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 19.8 && userBmi <= 23.3) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 23.4) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 10) {
                        if (userBmi < 14.2) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 14.3 && userBmi <= 20.6) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 20.7 && userBmi <= 23.3) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 23.4) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 11) {
                        if (userBmi < 14.6) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 14.7 && userBmi <= 20.7) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 20.8 && userBmi <= 22.8) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 22.9) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 12) {
                        if (userBmi < 16) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 16.1 && userBmi <= 21.4) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 21.5 && userBmi <= 23.3) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 23.4) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 13) {
                        if (userBmi < 15.6) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 15.7 && userBmi <= 22) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 22.1 && userBmi <= 24.3) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 24.4) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 14) {
                        if (userBmi < 17) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 17.1 && userBmi <= 23.1) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 23.2 && userBmi <= 25.9) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 26) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 15) {
                        if (userBmi < 17.6) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 17.7 && userBmi <= 23.1) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 23.2 && userBmi <= 27.5) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 27.6) {
                            bmiClass = "starkes Übergewicht";
                        }
                    }

                    /* Adults */
                    else if (age == 16) {
                        if (userBmi < 19) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 19 && userBmi <= 24) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 24 && userBmi <= 28) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 28) {
                            bmiClass = "starkes Übergewicht";
                        }
                    } else if (age >= 17 && age <= 24) {
                        if (userBmi < 20) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 20 && userBmi <= 25) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 25 && userBmi <= 29) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 29) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age >= 25 && age <= 34) {
                        if (userBmi < 21) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 21 && userBmi <= 26) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 26 && userBmi <= 30) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 30) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age >= 35 && age <= 44) {
                        if (userBmi < 22) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 22 && userBmi <= 27) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 27 && userBmi <= 31) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 31) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age >= 45 && age <= 54) {
                        if (userBmi < 23) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 23 && userBmi <= 28) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 28 && userBmi <= 32) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 32) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age >= 55 && age <= 64) {
                        if (userBmi < 24) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 24 && userBmi <= 29) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 29 && userBmi <= 33) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 33) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age > 65) {
                        if (userBmi < 25) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 25 && userBmi <= 30) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 30 && userBmi <= 34) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 34) {
                            bmiClass = "starkes Übergewicht";
                        }
                    }

                    /* If user is male */
                } else {

                    /* Children */
                    if (age == 8) {
                        if (userBmi < 14.2) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 14.3 && userBmi <= 19.2) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 19.3 && userBmi <= 22.5) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 22.6) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 9) {
                        if (userBmi < 13.7) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 13.8 && userBmi <= 19.3) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 19.4 && userBmi <= 21.5) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 21.6) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 10) {
                        if (userBmi < 14.6) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 14.7 && userBmi <= 21.3) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 21.4 && userBmi <= 24.9) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 25) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 11) {
                        if (userBmi < 14.3) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 14.4 && userBmi <= 21.1) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 21.2 && userBmi <= 23.1) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 23.1) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 12) {
                        if (userBmi < 14.8) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 14.9 && userBmi <= 21.9) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 22 && userBmi <= 24.7) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 24.8) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 13) {
                        if (userBmi < 16.2) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 16.3 && userBmi <= 21.6) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 21.7 && userBmi <= 24.4) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 24.5) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 14) {
                        if (userBmi < 16.7) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 16.8 && userBmi <= 22.5) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 22.6 && userBmi <= 25.6) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 25.7) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age == 15) {
                        if (userBmi < 18.5) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 18.6 && userBmi <= 23.6) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi >= 23.7 && userBmi <= 25.9) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 26) {
                            bmiClass = "starkes Übergewicht";
                        }
                    }

                    /* Adults */
                    else if (age >= 16 && age <= 24) {
                        if (userBmi < 19) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 19 && userBmi <= 24) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 24 && userBmi <= 28) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 28) {
                            bmiClass = "starkes Übergewicht";
                        }
                    } else if (age >= 25 && age <= 34) {
                        if (userBmi < 20) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 20 && userBmi <= 25) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 25 && userBmi <= 29) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 29) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age >= 35 && age <= 44) {
                        if (userBmi < 21) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 21 && userBmi <= 26) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 26 && userBmi <= 30) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 30) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age >= 45 && age <= 54) {
                        if (userBmi < 22) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 22 && userBmi <= 27) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 27 && userBmi <= 31) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 31) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age >= 55 && age <= 64) {
                        if (userBmi < 23) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 23 && userBmi <= 28) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 28 && userBmi <= 32) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 32) {
                            bmiClass = "starkes Übergewicht";
                        }

                    } else if (age > 65) {
                        if (userBmi < 24) {
                            bmiClass = "Untergewicht";
                        } else if (userBmi >= 24 && userBmi <= 29) {
                            bmiClass = "Normalgewicht";
                        } else if (userBmi > 29 && userBmi <= 33) {
                            bmiClass = "leichtes Übergewicht";
                        } else if (userBmi > 33) {
                            bmiClass = "starkes Übergewicht";
                        }
                    }
                }
                bmi.setText(userBmi + " (" + bmiClass + ")");
            }
        } else {
            GlobalFunctions.setNoInformationStyle(bmi);
        }

        /* Set dateOfRegistration*/
        String curdayIfRegistrationString = GlobalFunctions.getDateWithTimeFromMillis(user_dateOfRegistration, "dd.MM.yyyy HH:mm");
        dayOfRegistration.setText(curdayIfRegistrationString);

        /* Set lastLogin*/
        String curLastLoginString = GlobalFunctions.getDateWithTimeFromMillis(user_lastLogin, "dd.MM.yyyy HH:mm");
        lastLogIn.setText(curLastLoginString);

        /*Set amount records*/
        amountRecords.setText("" + user_amountRecords);

        /* Set total distance */
        double distance = Math.round(user_totalDistance);
        double levelDistance;
        if (distance >= 1000) {
            String d = "" + Math.round((distance / 1000L) * 100) / 100.0;
            totalDistance.setText(d.replace('.', ',') + " km");
            levelDistance = distance / 1000L;
        } else {
            levelDistance = distance / 1000;
            totalDistance.setText((int) distance + " m");
        }

        /* Set total time */
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        TimeZone tz = TimeZone.getTimeZone("UTC");
        df.setTimeZone(tz);
        String time = df.format(new Date(user_totalTime * 1000));
        totalTime.setText(time);

        /* Set state */
        state.setImageBitmap(GlobalFunctions.findLevel(levelDistance));

        /* Set profile image */
        byte[] imgRessource = user_image;
        Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.raw.default_profile);
        if (imgRessource != null && imgRessource.length > 0) {
            bitmap = BitmapFactory.decodeByteArray(imgRessource, 0, imgRessource.length);
        }
        image.setImageBitmap(bitmap);

        /* Remove loadScreen */
        loadProfile.setVisibility(View.GONE);
    }

    boolean todayDayOfBirth = false;

    /* Function to calculate age */
    private int calculateAge(String dayOfBirth) {

        String[] dobValues = dayOfBirth.split("\\.");
        Calendar dob = Calendar.getInstance();
        dob.set(Integer.parseInt(dobValues[2]), Integer.parseInt(dobValues[1]), Integer.parseInt(dobValues[0]));
        Calendar currentTime = Calendar.getInstance();

        /* Calculate age */
        int age = currentTime.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        int day1 = currentTime.get(Calendar.DAY_OF_MONTH);
        int day2 = dob.get(Calendar.DAY_OF_MONTH);

        if ((currentTime.get(Calendar.MONTH) + 1) < dob.get(Calendar.MONTH)) {
            age--;
        } else if ((currentTime.get(Calendar.MONTH) + 1) == dob.get(Calendar.MONTH)) {

            if (day2 > day1) {
                age--;
            }
        }

        if (day2 == day1 && (currentTime.get(Calendar.MONTH) + 1) == dob.get(Calendar.MONTH)) {
            todayDayOfBirth = true;
        }

        return new Integer(age);
    }
}
