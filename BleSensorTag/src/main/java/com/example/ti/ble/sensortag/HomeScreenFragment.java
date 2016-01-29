package com.example.ti.ble.sensortag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeScreenFragment extends Fragment implements View.OnClickListener{
    View rootView;
    public HomeScreenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    //    return inflater.inflate(R.layout.activity_home_screen, container, false);

        rootView = inflater.inflate(R.layout.fragment_home_screen, container, false);
        Button btn= (Button)rootView.findViewById(R.id.register);
        Button btn2= (Button)rootView.findViewById(R.id.SignIn);

        btn.setOnClickListener(this);
        btn2.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.register:
      //          startActivity(new Intent(v.getContext(), RegistrationActivity.class));
                break;
            case R.id.SignIn:
     //          startActivity(new Intent(v.getContext(), Connection.class));
     //           startActivity(new Intent(v.getContext(), Connection.class));
     //           startActivity(new Intent(v.getContext(),HClient.class));
                Log.i("Android", " 111111111111111111111111111111111111111");
                startActivity(new Intent(v.getContext(),LoginActivity.class));
              // startActivity(new Intent(v.getContext(),SensorsPosition.class));
              // startActivity(new Intent(v.getContext(),HtpSensor.class));


                break;

        }

    }
}
