package com.example.checkers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LaunchActivity extends AppCompatActivity {
    private String gameId;
    private Intent intent;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        url = "http://ec2-18-218-224-138.us-east-2.compute.amazonaws.com:8765/";

        Button createButton = findViewById(R.id.createButton);
        Button joinButton = findViewById(R.id.joinButton);
        final TextView createIdBox = findViewById(R.id.createId);
        final TextView joinIdBox = findViewById(R.id.joinId);
        intent = new Intent(this, MainActivity.class);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameId = createIdBox.getText().toString();
                post(gameId, "created");
                intent.putExtra("userID", 2);
                intent.putExtra("gameID", gameId);
                startActivity(intent);
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameId = joinIdBox.getText().toString();
                post(gameId, "joined");
                intent.putExtra("userID", 1);
                intent.putExtra("gameID", gameId);
                startActivity(intent);
            }
        });
    }

    public void post(final String game, final String data) {
        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, url + "post", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("ID", game);
                        params.put("data", data);
                        return params;
                    }
                };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
