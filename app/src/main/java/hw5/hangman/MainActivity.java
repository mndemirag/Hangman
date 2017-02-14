package hw5.hangman;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MainActivity extends AppCompatActivity {
    private TextView main_score;
    private TextView main_attempt;
    private TextView main_currentword;
    private TextView main_guessed;
    private EditText inputGuessObj;

    private String msg;
    private String serverAddressInput;
    private String serverPortInput;
    private int serverPortInt;
    String[] AfterSplit;
    public BlockingQueue queue = new ArrayBlockingQueue(256);
    String score, newscore, word, availableAttempt, gameStatus, playerInput, currentGuess;
    int scoreInt, newscoreInt, attemptInt;
    String guessHistory = "";
    Boolean isStillPlaying = true, true1 = true, false1 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_score = (TextView)findViewById(R.id.main_score);
        main_attempt = (TextView)findViewById(R.id.main_attempt);
        main_currentword = (TextView)findViewById(R.id.main_currentword);
        main_guessed = (TextView)findViewById(R.id.main_guessed);
        inputGuessObj = (EditText) findViewById(R.id.input_guess);
    //}
    //public void ClientMain(View view) {
        /*
        TextView AAA = new TextView(this);
        AAA.setText(message1+message2);
        setContentView(AAA);
        */
        Intent intentWelcome = getIntent();
        serverAddressInput = intentWelcome.getStringExtra(WelcomeActivity.EXTRA_IP);
        serverPortInput = intentWelcome.getStringExtra(WelcomeActivity.EXTRA_PORT);
        serverPortInt = Integer.parseInt(serverPortInput);

        System.err.println("MainActivity start");
        //Initialize socket thread
        InitializeSocket(); //Just start thread and open connection to server
        /*
        String connResult = null;
        connResult = ReceiveFromSocket();
        System.err.println("connResult : "+connResult);
        if (connResult=="CONNFAIL"){
            System.err.println("INSIDE IF");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Connection fail. Check IP and port then try again.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.err.println("INSIDE onClick");
                            Intent intentWelcome2 = new Intent(MainActivity.this, WelcomeActivity.class);
                            startActivity(intentWelcome2);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        Sleep(5000);
        */
        //First time. Socket receive message, put to queue
        //then Main take it from queue
        String msga = null;
        msga = ReceiveFromSocket();


        //Split message and assign to specified variable
        CustomSplitter(msga);
        word = AfterSplit[0];
        availableAttempt = AfterSplit[1];
        score = AfterSplit[2];

        word = SpacingAdder(word);

        //SETUP THE GUI
        main_score.setText("Score : "+score);
        main_attempt.setText("Attempt(s) left : "+availableAttempt);
        main_currentword.setText(word);
        main_guessed.setText("HAHAHA");

    }

    private void InitializeSocket(){
        //Connect to server by start a thread
        (new Thread(new ClientSocket(serverAddressInput,serverPortInt,queue))).start();

    }
    private void CustomSplitter(String rawString){
        //SPLIT INTO SEVERAL STRINGS LIMITED BY SPACE
        AfterSplit = rawString.split("#");
    }
    private void Sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String SpacingAdder(String input){
        String result = input.replace("", " ").trim();
        return result;
    }
    private void SendToSocket(String msg){
        //Put data into queue
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    private String ReceiveFromSocket(){
        //TAKE DATA FROM SOCKET (QUEUE)
        try {
            msg = (String) queue.take();
            queue.clear();
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
        return(msg);
    }
    private Boolean isEnd(){
        //int scoreInt = Integer.parseInt(score.trim());
        //int newscoreInt = Integer.parseInt(newscore.trim());
        Boolean result = null;
        //CHECK IF THE GAME REACH END OR NOT
        //Win
        if (newscoreInt != scoreInt) result= true;
        else if (newscoreInt == scoreInt) result= false;
        return result;
    }

    private Boolean isWin(){
        //int scoreInt = Integer.parseInt(score.trim());
        //int newscoreInt = Integer.parseInt(newscore.trim());
        Boolean result = null;
        //CHECK IF THE GAME REACH END OR NOT
        //Win
        System.out.println("Newscore : "+newscoreInt+" Score : "+scoreInt);
        if (newscoreInt > scoreInt) result= true;
        else if (newscoreInt < scoreInt) result= false;
        return result;
    }
    private void WinAskReplay(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("You Win! Replay?");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                SendToSocket("NEWGAME");
                Sleep(100);
                scoreInt++;
                guessHistory ="";
                ReceiveUpdateGui();
            }
        });
        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SendToSocket("ENDGAME"); //Socket will detect this word and close connection
                System.exit(0);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void LoseAskReplay(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("You Lose! Replay?");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                SendToSocket("NEWGAME");
                Sleep(100);
                scoreInt--;
                guessHistory ="";
                ReceiveUpdateGui();
            }
        });
        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SendToSocket("ENDGAME"); //Socket will detect this word and close connection
                System.exit(0);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void SendGuess(){
        //Apply space and append to history to be shown
        guessHistory = guessHistory+" "+currentGuess;
        //Send to Server
        SendToSocket(currentGuess);
        Sleep(100);//Sleep to give socket time to take the data
    }

    private void ReceiveUpdateGui(){
        //Receive back update from server
        String msgb = ReceiveFromSocket();

        //Do the rest
        CustomSplitter(msgb);
        word = AfterSplit[0];
        availableAttempt = AfterSplit[1];
        newscore = AfterSplit[2];
        newscoreInt = Integer.parseInt(newscore.trim());
        word = SpacingAdder(word);

        //Update
        main_score.setText("Score : "+newscore);
        main_attempt.setText("Attempt(s) left : "+availableAttempt);
        main_currentword.setText(word);
        main_guessed.setText(guessHistory);
        inputGuessObj.setText("");
    }

    public void SendUpdateGUI(View v) {
        //First, get text field and convert all to uppercase

        playerInput = inputGuessObj.getText().toString();
        currentGuess = playerInput.toUpperCase();

        //Check first if the string is empty
        if (!currentGuess.isEmpty()) {
            SendGuess();
            ReceiveUpdateGui();

            attemptInt = Integer.parseInt(availableAttempt.trim());
            if (isEnd()){
                if (isWin()) WinAskReplay();
                else if(!isWin()) LoseAskReplay();
            }
        } else if (currentGuess.isEmpty()){
            //JOptionPane.showMessageDialog(null, "Please guess a letter or word.");
        }
    }

    public void resetAll(View view) {
        Intent intentWelcome = new Intent(this, WelcomeActivity.class);
        SendToSocket("ENDGAME"); //Socket will detect this word and close connection
        startActivity(intentWelcome);
    }

}
