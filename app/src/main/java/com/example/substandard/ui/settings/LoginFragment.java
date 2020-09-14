package com.example.substandard.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.substandard.R;
import com.example.substandard.service.LoginIntentService;
import com.example.substandard.service.LoginResultReceiver;
import com.google.android.material.snackbar.Snackbar;

/**
 * A simple {@link Fragment} subclass, used for logging in. The user should not be able to load
 * any other fragments until they have logged in here.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = LoginFragment.class.getSimpleName();

    private LoginResultReceiver loginReceiver = new LoginResultReceiver(new Handler());

    private EditText serverText;
    private EditText userText;
    private EditText passwordText;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        loginReceiver.setReceiver((resultCode, resultData) -> {
            if (resultCode == LoginIntentService.STATUS_SUCCESS) {
                Log.d(TAG, "login successful");
                NavController navController = Navigation.findNavController(rootView);
                navController.navigateUp();
            } else {
                Snackbar snackbar = Snackbar.make(rootView,
                                                    getString(R.string.login_failed),
                                                    Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(getResources().getColor(R.color.alertColor));
                snackbar.show();
            }
        });

        serverText = rootView.findViewById(R.id.server_edit_text);
        userText = rootView.findViewById(R.id.username_edit_text);
        passwordText = rootView.findViewById(R.id.password_edit_text);

        Button loginButton = rootView.findViewById(R.id.login_submit_button);
        loginButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        String serverAddr = serverText.getText().toString();
        String username =  userText.getText().toString();
        String password = passwordText.getText().toString();

        Intent loginIntent = new Intent(getContext(), LoginIntentService.class);
        loginIntent.putExtra(LoginIntentService.SERVER_EXTRA_KEY, serverAddr);
        loginIntent.putExtra(LoginIntentService.USERNAME_EXTRA_KEY, username);
        loginIntent.putExtra(LoginIntentService.PASSWORD_EXTRA_KEY, password);
        loginIntent.putExtra(LoginIntentService.RECEIVER_EXTRA_KEY, loginReceiver);
        requireContext().startService(loginIntent);
    }
}