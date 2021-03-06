package de.trackcat.SignIn;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import de.trackcat.GlobalFunctions;
import de.trackcat.LogIn.LogInFragment;
import de.trackcat.R;
import de.trackcat.StartActivity;

public class SignInFragment_1 extends Fragment implements View.OnClickListener {

    private FragmentTransaction fragTransaction;

    /* UI references */
    EditText firstName, lastName;
    ImageView btnNext;
    TextView logInInLink;
    String first_Name, last_Name, email, password1, password2, dayOfBirth;
    int gender;
    Boolean generalTerm, dataProtection;
    RadioGroup gender_group;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signin_1, container, false);

        /* Get references */
        btnNext = view.findViewById(R.id.btn_next);
        logInInLink = view.findViewById(R.id.link_login);
        firstName = view.findViewById(R.id.input_firstName);
        lastName = view.findViewById(R.id.input_lastName);
        gender_group = view.findViewById(R.id.input_gender);
        generalTerm = false;
        dataProtection = false;

        /* Get bundle */
        if (getArguments() != null) {
            first_Name = getArguments().getString("firstName");
            firstName.setText(first_Name);
            last_Name = getArguments().getString("lastName");
            lastName.setText(last_Name);

            /* Set gender */
            switch (getArguments().getInt("gender")) {
                case 0:
                    gender_group.check(R.id.radioFemale);
                    break;
                case 1:
                    gender_group.check(R.id.radioMale);
                    break;
            }
            email = getArguments().getString("email");
            dayOfBirth = getArguments().getString("dayOfBirth");
            password1 = getArguments().getString("password1");
            password2 = getArguments().getString("password2");
            generalTerm = getArguments().getBoolean("generalTerms");
            dataProtection = getArguments().getBoolean("dataProtection");
        }

        /* Set on click-Listener */
        btnNext.setOnClickListener(this);
        logInInLink.setOnClickListener(this);

        return view;
    }

    /* OnClick Listener */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:

                /* Read inputs */
                String input_firstName = firstName.getText().toString();
                String input_lastName = lastName.getText().toString();

                /* Get selected gender */
                int input_gender_id = gender_group.getCheckedRadioButtonId();
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

                /* If input are valid */
                boolean validFirstName = GlobalFunctions.validateName(firstName, StartActivity.getInstance());
                boolean validLastName = GlobalFunctions.validateName(lastName, StartActivity.getInstance());
                if (validFirstName && validLastName) {
                    /* Create bundle */
                    Bundle bundleSignIn_1 = new Bundle();
                    bundleSignIn_1.putString("firstName", input_firstName);
                    bundleSignIn_1.putString("lastName", input_lastName);
                    bundleSignIn_1.putInt("gender", gender);
                    bundleSignIn_1.putString("email", email);
                    bundleSignIn_1.putString("dayOfBirth", dayOfBirth);
                    bundleSignIn_1.putString("password1", password1);
                    bundleSignIn_1.putString("password2", password2);
                    bundleSignIn_1.putBoolean("generalTerms", generalTerm);
                    bundleSignIn_1.putBoolean("dataProtection", dataProtection);

                    SignInFragment_2 signInFragment_2 = new SignInFragment_2();
                    signInFragment_2.setArguments(bundleSignIn_1);

                    /* Show next page */
                    fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.mainFrame, signInFragment_2,
                            getResources().getString(R.string.fSignIn_2));
                    fragTransaction.commit();
                }

                break;
            case R.id.link_login:
                fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.mainFrame, new LogInFragment(),
                        getResources().getString(R.string.fLogIn));
                fragTransaction.commit();
                break;
        }
    }
}
