package de.trackcat.Profile;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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

import static android.app.Activity.RESULT_OK;


public class EditProfileFragment extends Fragment implements View.OnClickListener {

    /* Variables */
    private static final int READ_REQUEST_CODE = 20;
    DatePickerDialog picker;
    AlertDialog.Builder alert;
    LayoutInflater layoutInflater;
    UserDAO userDAO;
    User currentUser;
    View view, alertView;
    EditText firstName, lastName;
    RadioGroup gender;
    TextView dayOfBirth, size, weight, label_unit_weight, label_unit_size;
    Button btnSave;
    CircleImageView imageUpload;
    RelativeLayout loadEditProfile;

    /* CheckChange Variables */
    boolean imageChanged, changedUser;
    float old_size, old_weight;
    long old_dateOfBirth;
    String old_firstName, old_lastName;
    int old_gender;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        /* Read variables */
        firstName = view.findViewById(R.id.input_firstName);
        lastName = view.findViewById(R.id.input_lastName);
        gender = view.findViewById(R.id.input_gender);
        weight = view.findViewById(R.id.input_weight);
        label_unit_weight = view.findViewById(R.id.label_unit_weight);
        size = view.findViewById(R.id.input_size);
        label_unit_size = view.findViewById(R.id.label_unit_size);
        dayOfBirth = view.findViewById(R.id.input_dayOfBirth);
        btnSave = view.findViewById(R.id.btn_save);
        imageUpload = view.findViewById(R.id.profile_image_upload);
        loadEditProfile = view.findViewById(R.id.loadScreen);

        /* Check changed values */
        imageChanged = false;
        changedUser = false;

        /* Set onClick Listener */
        btnSave.setOnClickListener(this);
        dayOfBirth.setOnClickListener(this);
        size.setOnClickListener(this);
        weight.setOnClickListener(this);
        imageUpload.setOnClickListener(this);

        /* Load user */
        loadUser();

        return view;
    }

    /* Function to load userdata */
    private void loadUser() {

        /* Set button enable= false and show loadscreen */
        setButtonDisable();
        loadEditProfile.setVisibility(View.VISIBLE);

        /* Get current user */
        userDAO = new UserDAO(MainActivity.getInstance());
        currentUser = userDAO.read(MainActivity.getActiveUser());

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
                        MainActivity.getInstance().showNotAuthorizedModal(1);
                    } else {
                        /* Get jsonString from API */
                        String jsonString = response.body().string();

                        /* Parse json */
                        JSONObject userJSON = new JSONObject(jsonString);
                        Log.d(getResources().getString(R.string.app_name) + "-EditProfileInformation", "Edit-Profilinformation erhalten von: " + userJSON.getString("firstName") + " " + userJSON.getString("lastName"));

                        /* Check values an show  */
                        byte[] image = null;

                        try {
                            old_size = (float) userJSON.getDouble("size");
                        } catch (Exception e) {
                            old_size = 0;
                        }

                        try {
                            old_weight = (float) userJSON.getDouble("weight");
                        } catch (Exception e) {
                            old_weight = 0;
                        }

                        try {
                            old_dateOfBirth = userJSON.getLong("dateOfBirth");
                        } catch (Exception e) {
                            old_dateOfBirth = 0;
                        }

                        if (userJSON.getString("image") != "null") {
                            image = GlobalFunctions.getBytesFromBase64(userJSON.getString("image"));
                        }
                        old_firstName = userJSON.getString("firstName");
                        old_lastName = userJSON.getString("lastName");
                        old_gender = userJSON.getInt("gender");

                        setProfileValues(old_firstName, old_lastName, old_dateOfBirth, old_size, old_weight, old_gender, image);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                call.cancel();

                /* Read values from local DB */
                old_firstName = currentUser.getFirstName();
                old_lastName = currentUser.getLastName();
                old_dateOfBirth = currentUser.getDateOfBirth();
                old_size = currentUser.getSize();
                old_weight = currentUser.getWeight();
                old_gender = currentUser.getGender();
                setProfileValues(old_firstName, old_lastName, old_dateOfBirth, old_size, old_weight, old_gender, currentUser.getImage());
                Log.d(getResources().getString(R.string.app_name) + "-EditProfileInformation", "ERROR: " + t.getMessage());
            }
        });
    }

    /* Function to set profile values */
    private void setProfileValues(String user_firstName, String user_lastName, long user_dayOfBirth, float user_size, float user_weight, int user_gender, byte[] user_image) {

        /* Set Drawer Menu */
        MainActivity.getInstance().setDrawerInfromation(user_image, user_firstName, user_lastName, currentUser.getMail());

        /* Set first and lastName */
        firstName.setText(user_firstName);
        lastName.setText(user_lastName);

        /* Set weight */
        if (user_weight != 0) {
            String string1 = ("" + user_weight).replace('.', ',');
            weight.setText("" + string1);
            label_unit_weight.setVisibility(View.VISIBLE);
        } else {
            GlobalFunctions.setNoInformationStyle(weight);
            label_unit_weight.setVisibility(View.GONE);
        }

        /* Set size */
        if (user_size != 0) {
            String string2 = ("" + user_size).replace('.', ',');
            size.setText("" + string2);
            label_unit_size.setVisibility(View.VISIBLE);
        } else {
            GlobalFunctions.setNoInformationStyle(size);
            label_unit_size.setVisibility(View.GONE);
        }

        /* Set gender */
        switch (user_gender) {
            case 0:
                gender.check(R.id.radioFemale);
                break;
            case 1:
                gender.check(R.id.radioMale);
                break;
        }

        /* Set dayOfBirth */
        if (user_dayOfBirth != 0) {
            String curDateString = GlobalFunctions.getDateFromMillis(user_dayOfBirth, "dd.MM.yyyy");
            dayOfBirth.setText(curDateString);
        } else {
            GlobalFunctions.setNoInformationStyle(dayOfBirth);
        }

        /* Set profile image */
        byte[] imgRessource = user_image;
        Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.raw.default_profile);
        if (imgRessource != null && imgRessource.length > 0) {
            bitmap = BitmapFactory.decodeByteArray(imgRessource, 0, imgRessource.length);
        }
        imageUpload.setImageBitmap(bitmap);

        /* Remove loadScreen */
        loadEditProfile.setVisibility(View.GONE);

        /* Set btn enable */
        setButtonEnable();

        /* Show toast if user was changed */
        if (changedUser) {
            /* UI-Message */
            if (MainActivity.getHints()) {
                Toast.makeText(getContext(), getResources().getString(R.string.saveProfileSuccess), Toast.LENGTH_LONG).show();
            }
        }
        changedUser = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                setButtonDisable();

                /* Read inputs */
                String input_firstName = firstName.getText().toString();
                String input_lastName = lastName.getText().toString();
                String input_weight = weight.getText().toString();
                String input_height = size.getText().toString();
                String input_dayOfBirth = dayOfBirth.getText().toString();

                /* Get selected gender */
                int input_gender_id = gender.getCheckedRadioButtonId();
                int gender;
                switch (input_gender_id) {
                    case R.id.radioFemale:
                        gender = 0;
                        break;
                    case R.id.radioMale:
                        gender = 1;
                        break;
                    default:
                        gender = 2;
                        break;
                }

                /* Check if all fields are filled and  validate inputs*/
                boolean validFirstName = GlobalFunctions.validateName(firstName, MainActivity.getInstance());
                boolean validLastName = GlobalFunctions.validateName(lastName, MainActivity.getInstance());
                if (validFirstName && validLastName) {

                    /* Parse values */
                    if (input_height.equals(getResources().getString(R.string.noInformation))) {
                        input_height = "0";
                    } else {
                        input_height = "" + input_height.replace(',', '.');
                    }

                    if (input_weight.equals(getResources().getString(R.string.noInformation))) {
                        input_weight = "0";
                    } else {
                        input_weight = "" + input_weight.replace(',', '.');
                    }

                    long long_dayOfBirth = 0;
                    if (input_dayOfBirth.equals(getResources().getString(R.string.noInformation))) {
                        long_dayOfBirth = 0;
                    } else {
                        try {
                            long_dayOfBirth = GlobalFunctions.getMillisFromString(input_dayOfBirth, "dd.MM.yyyy");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    /* Check if values have changed */
                    boolean changes = false;
                    HashMap<String, String> map = new HashMap<>();
                    String base = currentUser.getMail() + ":" + currentUser.getPassword();
                    String authString = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

                    if (imageChanged) {

                        /* Parse imageView into bytes */
                        ImageView imageView = view.findViewById(R.id.profile_image_upload);
                        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, false);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] imageBytes = stream.toByteArray();

                        String image = GlobalFunctions.getBase64FromBytes(imageBytes);
                        currentUser.setImage(imageBytes);
                        map.put("image", image);
                        changes = true;
                    }

                    if (!old_firstName.equals(input_firstName)) {
                        currentUser.setFirstName(input_firstName);
                        map.put("firstName", input_firstName);
                        changes = true;
                    }

                    if (!old_lastName.equals(input_lastName)) {
                        currentUser.setLastName(input_lastName);
                        map.put("lastName", input_lastName);
                        changes = true;
                    }

                    if (!(old_size == Float.valueOf(input_height))) {
                        currentUser.setSize(Float.valueOf(input_height));
                        map.put("size", input_height);
                        changes = true;
                    }

                    if (!(old_weight == Float.valueOf(input_weight))) {
                        currentUser.setWeight(Float.valueOf(input_weight));
                        map.put("weight", input_weight);
                        changes = true;
                    }

                    if (!(old_gender == gender)) {
                        currentUser.setGender(gender);
                        map.put("gender", "" + gender);
                        changes = true;
                    }
                    if (!(old_dateOfBirth == long_dayOfBirth)) {
                        currentUser.setDateOfBirth(long_dayOfBirth);
                        map.put("dateOfBirth", "" + long_dayOfBirth);
                        changes = true;
                    }

                    if (changes) {

                        /* Change values in local DB */
                        currentUser.setTimeStamp(GlobalFunctions.getTimeStamp());
                        userDAO.update(currentUser.getId(), currentUser);

                        /* Change values in global DB*/
                        map.put("timeStamp", "" + GlobalFunctions.getTimeStamp());

                        Retrofit retrofit = APIConnector.getRetrofit();
                        APIClient apiInterface = retrofit.create(APIClient.class);

                        /* Start a call */
                        Call<ResponseBody> call = apiInterface.updateUser(authString, map);

                        call.enqueue(new Callback<ResponseBody>() {

                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                try {

                                    if (response.code() == 401) {
                                        MainActivity.getInstance().showNotAuthorizedModal(1);
                                    } else {
                                        /* Get jsonString from API */
                                        String jsonString = response.body().string();

                                        /* Parse json */
                                        JSONObject successJSON = new JSONObject(jsonString);

                                        if (successJSON.getString("success").equals("0")) {

                                            /* Save is Synchronized value as true */
                                            userDAO.update(currentUser.getId(), currentUser);

                                            /* Set btn enable */
                                            setButtonEnable();

                                            /* Load user */
                                            loadUser();

                                            /* Set change variable */
                                            changedUser = true;
                                        } else if (successJSON.getString("success").equals("1")) {

                                            /* Set btn enable */
                                            setButtonEnable();

                                            /* Set Toast */
                                            if (MainActivity.getHints()) {
                                                Toast.makeText(getContext(), getResources().getString(R.string.editProfilUnknownError), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                                /* Set btn enable */
                                setButtonEnable();

                                /* Load user */
                                loadUser();

                                /* Set change variable */
                                changedUser = true;

                                call.cancel();
                            }
                        });

                        /* Update drawer */
                        MainActivity.getInstance().setDrawerInfromation(currentUser.getImage(), currentUser.getFirstName(), currentUser.getLastName(), currentUser.getMail());

                    } else {
                        /* UI-Message */
                        if (MainActivity.getHints()) {
                            Toast.makeText(getContext(), getResources().getString(R.string.editProfileNoChanges), Toast.LENGTH_LONG).show();
                        }

                        /* Set btn enable */
                        setButtonEnable();
                    }
                } else {
                    /* Set btn enable */
                    setButtonEnable();
                    if (MainActivity.getHints()) {
                        Toast.makeText(getContext(), getResources().getString(R.string.tFillAllFields), Toast.LENGTH_LONG).show();
                    }
                }

                break;
            case R.id.profile_image_upload:

                /* Create intents */
                Intent pickPhoto = new Intent();
                pickPhoto.setAction(Intent.ACTION_GET_CONTENT);
                pickPhoto.setType("image/*");
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                /* Add them to a list */
                List<Intent> intentList = new ArrayList<>();
                intentList.add(pickPhoto);
                //not working on api >23
                // intentList.add(takePicture);

                /* Create chooser */
                Intent chooserIntent = null;
                chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                        getContext().getString(R.string.selectImage));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));

                /* StartActivityForResult */
                startActivityForResult(chooserIntent, READ_REQUEST_CODE);

                break;
            case R.id.input_dayOfBirth:
                int day, month, year;
                /* If no inputs, choose current date */
                if (dayOfBirth.getText().toString().equals(getResources().getString(R.string.noInformation))) {
                    final Calendar cldr = Calendar.getInstance();
                    day = cldr.get(Calendar.DAY_OF_MONTH);
                    month = cldr.get(Calendar.MONTH);
                    year = cldr.get(Calendar.YEAR);
                } else {
                    /* Get old values */
                    String[] dayOfBirthValues = dayOfBirth.getText().toString().split("\\.");

                    /* Set datePicker an set value in field */
                    day = Integer.parseInt(dayOfBirthValues[0]);
                    month = Integer.parseInt(dayOfBirthValues[1]) - 1;
                    year = Integer.parseInt(dayOfBirthValues[2]);
                }

                picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                /* Get current date */
                                final Calendar cldr = Calendar.getInstance();
                                int currentDay = cldr.get(Calendar.DAY_OF_MONTH);
                                int currentMonth = cldr.get(Calendar.MONTH) + 1;
                                int currentYear = cldr.get(Calendar.YEAR);

                                /* Check if dayOfBirth in future */
                                if (year > currentYear) {
                                    if (MainActivity.getHints()) {
                                        Toast.makeText(getContext(), getResources().getString(R.string.birthdayInFuture), Toast.LENGTH_SHORT).show();
                                    }
                                    return;
                                }
                                if (currentMonth == (monthOfYear + 1) && currentYear == year) {
                                    if (dayOfMonth > currentDay) {
                                        if (MainActivity.getHints()) {
                                            Toast.makeText(getContext(), getResources().getString(R.string.birthdayInFuture), Toast.LENGTH_SHORT).show();
                                        }
                                        return;
                                    }
                                }

                                String month = "" + (monthOfYear + 1);
                                String day = "" + dayOfMonth;
                                if (monthOfYear + 1 < 10) {
                                    month = "0" + month;
                                }
                                if (dayOfMonth < 10) {
                                    day = "0" + day;
                                }
                                dayOfBirth.setText(day + "." + month + "." + year);
                                GlobalFunctions.resetNoInformationStyle(dayOfBirth, label_unit_weight.getCurrentTextColor());
                            }
                        }, year, month, day);
                picker.show();
                break;
            case R.id.input_size:

                /* Create AlertBox */
                alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Größe festlegen");
                layoutInflater = (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                alertView = layoutInflater != null ? layoutInflater.inflate(R.layout.fragment_numberpicker, null, true) : null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String[] heightValues;

                    /* If no inputs, choose 0,0 */
                    if (size.getText().toString().equals(getResources().getString(R.string.noInformation))) {
                        heightValues = new String[]{"0", "0"};
                    } else {
                        /* Get old values */
                        heightValues = size.getText().toString().split(",");
                    }

                    /* Set max, min and unit */
                    alert.setView(alertView);
                    NumberPicker numberPickerInteger = alertView.findViewById(R.id.numberPickerInteger);
                    numberPickerInteger.setMinValue(0);
                    numberPickerInteger.setMaxValue(300);
                    numberPickerInteger.setValue(Integer.parseInt(heightValues[0]));

                    NumberPicker numberPickerDecimal = alertView.findViewById(R.id.numberPickerDecimal);
                    numberPickerDecimal.setMinValue(0);
                    numberPickerDecimal.setMaxValue(10);
                    numberPickerDecimal.setValue(Integer.parseInt(heightValues[1]));

                    TextView unit = alertView.findViewById(R.id.label_unit);
                    unit.setText("cm");
                }

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        NumberPicker numberPickerInteger = alertView.findViewById(R.id.numberPickerInteger);
                        NumberPicker numberPickerDecimal = alertView.findViewById(R.id.numberPickerDecimal);

                        if (numberPickerInteger.getValue() != 0) {
                            size.setText(numberPickerInteger.getValue() + "," + numberPickerDecimal.getValue());
                            GlobalFunctions.resetNoInformationStyle(size, label_unit_size.getCurrentTextColor());
                            label_unit_size.setVisibility(View.VISIBLE);
                        } else {
                            size.setText(getResources().getString(R.string.noInformation));
                            GlobalFunctions.setNoInformationStyle(size);
                            label_unit_size.setVisibility(View.GONE);
                        }
                    }
                });

                alert.setNegativeButton("Verwerfen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alert.show();
                break;

            case R.id.input_weight:

                /* Create AlertBox */
                alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Gewicht festlegen");
                layoutInflater = (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                alertView = layoutInflater != null ? layoutInflater.inflate(R.layout.fragment_numberpicker, null, true) : null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String[] weightValues;

                    /* If no inputs, choose 0,0 */
                    if (weight.getText().toString().equals(getResources().getString(R.string.noInformation))) {
                        weightValues = new String[]{"0", "0"};
                    } else {
                        /* Get old values */
                        weightValues = weight.getText().toString().split(",");
                    }

                    /* Set max, min and unit */
                    alert.setView(alertView);
                    NumberPicker numberPickerInteger = alertView.findViewById(R.id.numberPickerInteger);
                    numberPickerInteger.setMinValue(0);
                    numberPickerInteger.setMaxValue(500);
                    numberPickerInteger.setValue(Integer.parseInt(weightValues[0]));

                    NumberPicker numberPickerDecimal = alertView.findViewById(R.id.numberPickerDecimal);
                    numberPickerDecimal.setMinValue(0);
                    numberPickerDecimal.setMaxValue(10);
                    numberPickerDecimal.setValue(Integer.parseInt(weightValues[1]));

                    TextView unit = alertView.findViewById(R.id.label_unit);
                    unit.setText("kg");
                }

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        NumberPicker numberPickerInteger = alertView.findViewById(R.id.numberPickerInteger);
                        NumberPicker numberPickerDecimal = alertView.findViewById(R.id.numberPickerDecimal);

                        if (numberPickerInteger.getValue() != 0) {
                            weight.setText(numberPickerInteger.getValue() + "," + numberPickerDecimal.getValue());
                            GlobalFunctions.resetNoInformationStyle(weight, label_unit_weight.getCurrentTextColor());
                            label_unit_weight.setVisibility(View.VISIBLE);
                        } else {
                            weight.setText(getResources().getString(R.string.noInformation));
                            GlobalFunctions.setNoInformationStyle(weight);
                            label_unit_weight.setVisibility(View.GONE);
                        }
                    }
                });

                alert.setNegativeButton("Verwerfen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alert.show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap img = null;
            if (resultData != null) {
                imageChanged = true;
                if (MainActivity.getHints()) {
                    Toast.makeText(getContext(), getResources().getString(R.string.ucrop_selectImage), Toast.LENGTH_SHORT).show();
                }

                /* Crop image */
                beginCrop(resultData.getData());

                try {
                    InputStream stream = getContext().getContentResolver().openInputStream(resultData.getData());
                    img = BitmapFactory.decodeStream(stream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {

            /* Set image in imageView */
            imageUpload.setImageURI(null);
            imageUpload.setImageURI(UCrop.getOutput(resultData));
        }
    }

    private void beginCrop(Uri source) {

        /* Set destination */
        Uri destination = Uri.fromFile(new File(getContext().getCacheDir(), "cropped"));

        /* Get Theme colors */
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();

        /* Color Primary */
        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        TypedArray arr = getActivity().obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.colorPrimary});
        int primaryColor = arr.getColor(0, -1);

        /* Color Secondary */
        theme.resolveAttribute(android.R.attr.statusBarColor, typedValue, true);
        TypedArray arr2 = getActivity().obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.statusBarColor});
        int secondaryColor = arr2.getColor(0, -1);

        /* Text color primary */
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr3 =
                getActivity().obtainStyledAttributes(typedValue.data, new int[]{
                        android.R.attr.textColorPrimary});
        int textColorPrimary = arr3.getColor(0, -1);

        /* Set crop options */
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.NONE);
        options.setToolbarColor(primaryColor);
        options.setToolbarWidgetColor(textColorPrimary);
        options.setStatusBarColor(secondaryColor);
        options.setShowCropGrid(true);

        /* Crop image */
        UCrop.of(source, destination)
                .withAspectRatio(1, 1)
                .withOptions(options)

                .withMaxResultSize(1000, 1000)
                .start(getActivity(), this, UCrop.REQUEST_CROP);
    }

    /* Functions to enable/disable button */
    private void setButtonEnable() {
        btnSave.setEnabled(true);
        btnSave.setBackgroundColor(getResources().getColor(R.color.colorGreenAccent));
    }

    private void setButtonDisable() {
        btnSave.setBackgroundColor(getResources().getColor(R.color.colorAccentDisable));
        btnSave.setEnabled(false);
    }
}
