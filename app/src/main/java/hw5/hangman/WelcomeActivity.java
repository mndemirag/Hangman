package hw5.hangman;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class WelcomeActivity extends AppCompatActivity {

    public final static String EXTRA_IP = "hw5.hangman.ipAddress";
    public final static String EXTRA_PORT = "hw5.hangman.port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }


    public void connectToServer(View view) {
        System.err.println("WelcomeActivity start");
        Intent intentMain = new Intent(this, MainActivity.class);

        EditText inputAddressObject = (EditText) findViewById(R.id.input_address);
        EditText inputPortObject = (EditText) findViewById(R.id.input_port);

        String inputAddress = inputAddressObject.getText().toString();
        String inputPort = inputPortObject.getText().toString();

        intentMain.putExtra(EXTRA_IP, inputAddress);
        intentMain.putExtra(EXTRA_PORT, inputPort);






        startActivity(intentMain);

    }
}
