package aforg.remotecamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.URL;

/**
 * This activity is responsible for setting the onclick listener for the take photo button.
 * When clicked the activity makes an HTTP request to the raspberry pi server which is running
 * flask.
 */
public class MainActivity extends AppCompatActivity {

    private Button takePhotoBtn;
    private TextView responseTextView;
    private ImageView photoTakenImageView;

    private RequestQueue queue;
    private MyRetryPolicy policyWithOverwrittenTimeout;
    private OnResponseFromPi responseFromPi;
    private OnErrorFromPi errorFromPi;

    private static final String RASPBERRY_PI_IP_ADDRESS = "192.168.1.148";
    private static final String RASPBERRY_PI_PORT = "5000";
    private static final String RASPBERRY_PI_SERVER_URL = "http://" + RASPBERRY_PI_IP_ADDRESS + ":" + RASPBERRY_PI_PORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        responseFromPi = new OnResponseFromPi();
        errorFromPi = new OnErrorFromPi();
        queue = Volley.newRequestQueue(this);
        policyWithOverwrittenTimeout = new MyRetryPolicy();
        takePhotoBtn = (Button) findViewById(R.id.takePhotoBtn);
        responseTextView = (TextView) findViewById(R.id.responseTextView);
        photoTakenImageView = (ImageView) findViewById(R.id.photoTakenImageView);
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                takePhoto();
            }
        });
    }

    private void takePhoto() {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, RASPBERRY_PI_SERVER_URL, null, responseFromPi, errorFromPi);
        jsonObjectRequest.setRetryPolicy(policyWithOverwrittenTimeout);
        queue.add(jsonObjectRequest);
    }

    private class MyRetryPolicy implements RetryPolicy {
        /**
         * Decided to set this retry policy so we could overwrite this timeout as to not
         * cause a volleyTimeoutError.
         *
         * @return
         */
        @Override
        public int getCurrentTimeout() {
            return 20000;
        }

        @Override
        public int getCurrentRetryCount() {
            return 1;
        }

        @Override
        public void retry(VolleyError error) throws VolleyError {}
    }

    private class OnResponseFromPi implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {
            Bitmap bmp = null;
            try {
                final String imageName = response.getString("imageName");
                final URL imageURL = new URL(RASPBERRY_PI_SERVER_URL + "/static/" + imageName);
                bmp = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
            } catch (Exception e) {
                // in the event of an error, place on the text view.
                responseTextView.setText("Error: " + e.getMessage());
            }
            if(bmp != null) {
                photoTakenImageView.setImageBitmap(bmp);
            }
        }
    }

    private class OnErrorFromPi implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            responseTextView.setText("Error: " + error.getMessage());
        }
    }

}
