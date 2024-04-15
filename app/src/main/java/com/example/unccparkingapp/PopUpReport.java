package com.example.unccparkingapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PopUpReport extends Activity {

    @Override
    protected void onCreate(Bundle savedinstanceState) {
        super.onCreate(savedinstanceState);

        setContentView(R.layout.popupwindow);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.7));

        EditText issueEdit = (EditText) findViewById(R.id.issueEditText);
        EditText detailsEdit = (EditText) findViewById(R.id.detailsEditText);
        Button sendBtn = (Button) findViewById(R.id.sendButton);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String issueText = issueEdit.getText().toString().trim();
                String detailsText = detailsEdit.getText().toString().trim();

                // Check if both EditText fields are not empty
                if (!issueText.isEmpty() && !detailsText.isEmpty()) {
                    String email = "sup.unccparking@gmail.com";
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse("mailto:"));
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    i.putExtra(Intent.EXTRA_SUBJECT, "Report From UNCC Parking Application '" + issueText + "'");
                    i.putExtra(Intent.EXTRA_TEXT, "Issue:\n" + issueText + "\n Details:\n" + detailsText);
                    try {
                        startActivity(Intent.createChooser(i, "Please Select Your Email"));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(PopUpReport.this, "There are no Email", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Display a toast message indicating that fields are empty
                    Toast.makeText(PopUpReport.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}