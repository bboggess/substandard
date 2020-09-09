package com.example.substandard.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.substandard.R;
import com.example.substandard.service.LoginIntentService;
import com.example.substandard.service.LoginResultReceiver;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private LoginResultReceiver loginReceiver = new LoginResultReceiver(new Handler());

    private Button loginButton;
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
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        return fragment;
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
                Navigation.findNavController(rootView).navigateUp();
            } else {
                //TODO popup error message
            }
        });

        serverText = rootView.findViewById(R.id.server_edit_text);
        userText = rootView.findViewById(R.id.username_edit_text);
        passwordText = rootView.findViewById(R.id.password_edit_text);

        loginButton = rootView.findViewById(R.id.login_submit_button);
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
        getContext().startService(loginIntent);
    }
}