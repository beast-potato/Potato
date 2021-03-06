package com.beastpotato.potato.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.beastpotato.potato.api.net.ApiRequest;
import com.beastpotato.potato.example.JsonStrings.personmodel.PersonModel;
import com.beastpotato.potato.example.getvideosinforesponse.GetVideosInfoApiResponse;

import java.util.List;

public class ExampleActivity extends AppCompatActivity {
    private static String TAG = "ExampleActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        final TextView tv = (TextView) findViewById(R.id.text);

        //Json string to model test
        PersonModel personModel = new PersonModel();
        personModel.firstName = "Oleksiy";
        personModel.lastName = "Martynov";
        personModel.age = 26L;

        //Network test
        GetVideosInfoApiRequest request = new GetVideosInfoApiRequest("https://community-vineapp.p.mashape.com", this);//Generated based on @Endpoint annotation in GetVideosInfo.java
        request.setApiKey("Sh7KOqP6lQmshVcLl2xFAG4BOfr9p1TfANBjsnduXWDjnqjNZY");
        request.setContentType("application/json");
        request.setCategory("popular");
        request.setSortType("desc");

        Log.i(TAG, "sending request...");
        List<GetVideosInfoApiRequest.Fields> invalidFields = request.validateFields();
        for (GetVideosInfoApiRequest.Fields field : invalidFields) {
            Toast.makeText(ExampleActivity.this, "Field " + field.name() + ", with serializable name" + field.getFieldKey() + " failed validation!", Toast.LENGTH_LONG).show();
            switch (field) {
                case category:
                    //handle case if category field failed validation.
                    break;
                case sortType:
                    //handle case if sortType field failed validation.
                    break;
                case apiKey:
                    //handle case if apiKey field failed validation.
                    break;
                case contentType:
                    //handle case if contentType field failed validation.
                    break;
            }
        }
        if (invalidFields.size() == 0) {
            request.send(new ApiRequest.RequestCompletion<GetVideosInfoApiResponse>() {
                @Override
                public void onResponse(GetVideosInfoApiResponse data) {
                    tv.setText("Response records count:" + data.data.count);
                }

                @Override
                public void onError(VolleyError error) {
                    tv.setText(error.toString());
                }
            });
        }
    }
}
