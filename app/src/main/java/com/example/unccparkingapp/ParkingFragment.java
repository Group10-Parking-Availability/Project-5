package com.example.unccparkingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.unccparkingapp.databinding.FragmentParkingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ParkingFragment extends Fragment {

    public ParkingFragment() {
        // Required empty public constructor
    }

    FragmentParkingBinding binding;
    EditText editText1, editText2;
    TextView textView1, textView2;

    //FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParkingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editText1 = view.findViewById(R.id.editTextText2);
        editText2 = view.findViewById(R.id.editTextText);
        textView1 = view.findViewById(R.id.textView);
        textView2 = view.findViewById(R.id.textView2);


        db.collection("parking_data")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String data1 = document.getString("location");
                            String data2 = document.getString("parking_available");

                            // Check if data is not null before setting it to TextViews
                            if (data1 != null && data2 != null) {
                                textView1.setText("Data 1: " + data1);
                                textView2.setText("Data 2: " + data2);
                            } else {
                                // Handle the case where data is null
                                textView1.setText("Data 1: N/A");
                                textView2.setText("Data 2: N/A");
                            }
                        }
                    } else {
                        // Handle errors
                        textView1.setText("Error fetching data");
                        textView2.setText("Error fetching data");
                    }
                });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = db.collection("parking_data").document();
                String inputData1 = editText1.getText().toString();
                String inputData2 = editText2.getText().toString();

                // Write data to the Firestore database
                Map<String, Object> data = new HashMap<>();
                data.put("location", inputData2);
                data.put("parking_available", inputData1);

                docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Data saved successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
