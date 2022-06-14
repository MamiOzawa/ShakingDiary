package jp.ac.titech.itpro.sdl.shakingdiary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.CountDownTimer;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

public class ShakingActivity extends AppCompatActivity implements SensorEventListener {
    private int R_id, currentScore;
    private String emotion_type;
    private TextView textView, scoreText, timerText;
    private Button start_button, register_button;
    private ImageView imageview;
    private CountDown countdown;
    private SensorManager manager;
    private Sensor sensor;
    private int sensor_value = 0, value_num = 0;
    private int tmp;
    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        tmp = (int) (Math.abs(sensorEvent.values[0]) + Math.abs(sensorEvent.values[1]) + Math.abs(sensorEvent.values[2]));
        sensor_value += tmp; value_num += 1;
        Log.i("onSensorchanged","sensor_value is " + sensor_value + ", value_num is " + value_num);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    class CountDown extends CountDownTimer {
        CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            timerText.setText("計測中..." + (int) (millisUntilFinished/1000));
        }

        @Override
        public void onFinish() {
            manager.unregisterListener(ShakingActivity.this);
            timerText.setText("計測終了！\n");
            currentScore = sensor_value;
            scoreText.setText(currentScore + "pt");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shaking);
        Intent from_Sub_intent = getIntent();

        R_id = from_Sub_intent.getIntExtra("R_id", 0);
        currentScore = from_Sub_intent.getIntExtra("currentscore", 0);

        textView = this.findViewById(R.id.textView_in_Shaking);
        scoreText = this.findViewById(R.id.scoretext);
        start_button = this.findViewById(R.id.start_button);
        register_button = this.findViewById(R.id.register_button_in_shake);
        imageview = this.findViewById(R.id.imageview_shaking);
        timerText = this.findViewById(R.id.timertext);

        manager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        switch (R_id){
            case R.id.image_button_smile:
                emotion_type = "smile";
                textView.setText("楽しさをSHAKEで記録しよう！");
                imageview.setImageResource(R.drawable.smile);
                break;
            case R.id.image_button_sad:
                emotion_type = "sad";
                textView.setText("悲しさをSHAKEで記録しよう！");
                imageview.setImageResource(R.drawable.sad);
                break;
            case R.id.image_button_angry:
                emotion_type = "angry";
                textView.setText("怒りをSHAKEで記録しよう！");
                imageview.setImageResource(R.drawable.angry);
                break;
        }
        scoreText.setText(currentScore + "pt");
        start_button.setOnClickListener(onClick_startbutton);
        register_button.setOnClickListener(onClick_registerbutton);
        countdown = new CountDown((long) 3500, (long) 1000);

    }

    View.OnClickListener onClick_startbutton = new View.OnClickListener() {
        public void onClick(View v) {
            manager.registerListener(ShakingActivity.this, sensor, manager.SENSOR_DELAY_NORMAL);
            timerText.setText("計測中...3");
            countdown.start();
            sensor_value = 0;
            value_num = 0;
        }
    };

    View.OnClickListener onClick_registerbutton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent result = new Intent();
            setResult(RESULT_OK, result);
            result.putExtra("currentscore", currentScore);
            result.putExtra("emotion_type", emotion_type);
            finish();
        }
    };
}