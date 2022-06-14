package jp.ac.titech.itpro.sdl.shakingdiary;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class SubActivity extends AppCompatActivity {
    int year, month, date;
//    int smile_currentScore = 0, sad_currentScore = 0, angry_currentScore = 0;
    TextView textView, smilescore_text, sadscore_text, angryscore_text, textCount;
    EditText editText;
    ImageButton image_button_smile, image_button_sad, image_button_angry;
    Button tweetButton, registerButton, deleteButton;
    String tweet_toast;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        Intent from_Main_intent = getIntent();
        year = from_Main_intent.getIntExtra("Year", 0);
        month = from_Main_intent.getIntExtra("Month",0);
        date = from_Main_intent.getIntExtra("Date",0);


        setContentView(R.layout.activity_sub);

        textView = (TextView)this.findViewById(R.id.textView_in_Sub);
        textView.setText(year+"年"+month+"月"+date+"日の日記");

        textCount = this.findViewById(R.id.textcount);

        editText = findViewById(R.id.edit_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int textColor = Color.GRAY;
                int txtLength = charSequence.length();
                textCount.setText(Integer.toString(txtLength) + "/100");
                if (txtLength > 100) textColor = Color.RED;
                textCount.setTextColor(textColor);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        editText.setText(from_Main_intent.getStringExtra("text"),TextView.BufferType.EDITABLE);

        image_button_smile = this.findViewById(R.id.image_button_smile);
        image_button_smile.getDrawable().setLevel(from_Main_intent.getIntExtra("smile_score",0));
        image_button_smile.setOnClickListener(onClickSetImage);

        image_button_sad = this.findViewById(R.id.image_button_sad);
        image_button_sad.getDrawable().setLevel(from_Main_intent.getIntExtra("sad_score",0));
        image_button_sad.setOnClickListener(onClickSetImage);


        image_button_angry = this.findViewById(R.id.image_button_angry);
        image_button_angry.getDrawable().setLevel(from_Main_intent.getIntExtra("angry_score",0));
        image_button_angry.setOnClickListener(onClickSetImage);

        smilescore_text = this.findViewById(R.id.smilescore_text);
        smilescore_text.setText(image_button_smile.getDrawable().getLevel() + "");

        sadscore_text = this.findViewById(R.id.sadscore_text);
        sadscore_text.setText(image_button_sad.getDrawable().getLevel() + "");

        angryscore_text = this.findViewById(R.id.angryscore_text);
        angryscore_text.setText(image_button_angry.getDrawable().getLevel() + "");

        tweetButton = this.findViewById(R.id.tweet_button);
        tweetButton.setOnClickListener(onClick_Tweetbutton);

        registerButton = this.findViewById(R.id.register_button_in_sub);
        registerButton.setOnClickListener(onClick_Registerbutton);

        deleteButton = this.findViewById(R.id.delete_button_in_sub);
        deleteButton.setOnClickListener(onClick_Deletebutton);

    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent result_intent = result.getData();
            if(!(result_intent == null)){
                int score = Math.min(result_intent.getIntExtra("currentscore", -1), 100);
                switch (result_intent.getStringExtra("emotion_type")) {
                    case "smile":
                        image_button_smile.getDrawable().setLevel(score);
                        smilescore_text.setText(score + "");
                        break;
                    case "sad":
                        image_button_sad.getDrawable().setLevel(score);
                        sadscore_text.setText(score + "");
                        break;
                    case "angry":
                        image_button_angry.getDrawable().setLevel(score);
                        angryscore_text.setText(score + "");
                        break;
                }
            }
        }
    });

    View.OnClickListener onClickSetImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // ViewからDrawableを取得
            Drawable drawable = ((ImageView) v).getDrawable();

            Intent shaking_intent = new Intent(SubActivity.this, ShakingActivity.class);
            shaking_intent.putExtra("R_id", v.getId());
            shaking_intent.putExtra("currentscore", drawable.getLevel());
            launcher.launch(shaking_intent);
        }
    };

    private class Task extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... params) {
            String txt = editText.getText().toString() + "\n";
            String date_is = year+"年"+month+"月"+date+"日の日記\n";
            String status_txt  = ""
                    + "楽しさ：" + smilescore_text.getText().toString()
                    + "\n悲しさ：" + sadscore_text.getText().toString()
                    + "\n怒り：" + angryscore_text.getText().toString();
            Twitter twitter = TwitterFactory.getSingleton();

            try {
                twitter.updateStatus(date_is + txt + status_txt);
                tweet_toast = "Tweetに成功しました！";
            } catch (TwitterException e) {
                e.printStackTrace();
                tweet_toast = "Tweetに失敗しました";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getApplicationContext(), tweet_toast, Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener onClick_Tweetbutton = new View.OnClickListener() {
        public void onClick(View v) {
            new Task().execute();
        }};

    View.OnClickListener onClick_Registerbutton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //中身が書き込まれたものだけintentにのせて返す
            Intent result = getIntent();
            setResult(RESULT_OK, result);
            result.putExtra("text", editText.getText().toString());
            result.putExtra("smile_score", Integer.valueOf(smilescore_text.getText().toString()));
            result.putExtra("sad_score", Integer.valueOf(sadscore_text.getText().toString()));
            result.putExtra("angry_score", Integer.valueOf(angryscore_text.getText().toString()));
            Log.i("SubActivity", "result key[0] は " + result.getExtras().keySet().toArray()[0]);
            Log.i("SubActivity", "angryscore_text.getText().toString()  は " + angryscore_text.getText().toString());
            finish();
        }
    };

    View.OnClickListener onClick_Deletebutton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //インテントに空だという情報を載せてメインアクティビティに返す
            Intent result = getIntent();
            setResult(RESULT_OK, result);
            result.putExtra("text", "");
            result.putExtra("smile_score", 0);
            result.putExtra("sad_score", 0);
            result.putExtra("angry_score", 0);
            finish();
        }
    };
}