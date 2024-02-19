package com.codegama.todolistapplication.activity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codegama.todolistapplication.R;
import com.codegama.todolistapplication.WakeLocker;
import com.codegama.todolistapplication.broadcastReceiver.AlarmBroadcastReceiver;
import com.codegama.todolistapplication.database.DatabaseClient;
import com.codegama.todolistapplication.helper.Helper;
import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;

import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlarmActivity extends BaseActivity {

    private static AlarmActivity inst;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.timeAndData)
    TextView timeAndData;
    @BindView(R.id.completeButton)
    SwipeButton completeButton;
    MediaPlayer mediaPlayer;
    int req_id;
    String date,time,period,importance;
    boolean isActive=false;
    AlarmManager alarmManager;
    Intent alarmIntent;
    PendingIntent pendingIntent;

    public static AlarmActivity instance() {
        return inst;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);
        //todo: move this func. to wakelocker class
        //Get the window from the context
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //Unlock
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //Lock device
        //DevicePolicyManager mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        if(getIntent().getExtras() != null) {
            req_id = getIntent().getIntExtra("ID",0);
            title.setText(getIntent().getStringExtra("TITLE"));
            description.setText(getIntent().getStringExtra("DESC"));
            date = getIntent().getStringExtra("DATE");
            time = getIntent().getStringExtra("TIME");
            period = getIntent().getStringExtra("PER");
            timeAndData.setText( date + ", " + time + "  |  " + period);
            importance = getIntent().getStringExtra("IMP");
        }

        startMediaPlayer();

        Glide.with(getApplicationContext()).load(R.drawable.alert).into(imageView);
        //completeButton.setOnClickListener(view -> deleteTask());
        completeButton.setOnStateChangeListener(active -> deleteTask());
        //TODO: if cond necessary may be
        if (!isActive)
            setNextReminderTime(3);
        WakeLocker.release();
    }

    @Override
    protected void onDestroy() {
        mediaPlayer.release();
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
    }



    @Override
    protected void onRestart() {
        super.onRestart();
        if (isActive)
            startMediaPlayer();
        isActive=true;
        setNextReminderTime(3);
    }

    //TODO: think about deleting task and canceling alarm that which class more usefull. maybe task bottom sheet more usefull.
    private void deleteTask() {
        class deleteTaskInBackend extends AsyncTask<Void, Void, Void> {
            int id = -100;
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                id = req_id < 0 ? -req_id : req_id;
                if(id != -100 && period.equals("Once")){
                    DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().dataBaseAction().deleteTaskFromId(id-1);
                }
                else {
                    String nextTime = Helper.setNextTime(period, date,time );
                    DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().dataBaseAction().updateAnExistingTaskTime(id-1,
                           nextTime.split(" ")[0],
                            nextTime.split(" ")[1]
                    );
                    //TODO: test task time then delete this
                    String updated = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().dataBaseAction().getAllTasksList().get(id-1).getDate();

                }
                //TODO: delete if necessary
                //CreateTaskBottomSheetFragment taskBottomSheetFragment = new CreateTaskBottomSheetFragment();
                //taskBottomSheetFragment.cancelAlarm(req_id);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(req_id == id || req_id == (0.-id))
                    Toast.makeText(getApplicationContext(), "Task is completed", Toast.LENGTH_SHORT).show();
                cancelAlarm();
                finish();

            }
        }
        deleteTaskInBackend dt = new deleteTaskInBackend();
        dt.execute();
    }
    //TODO: merge both methods create or repeat alarm (https://stackoverflow.com/questions/3330522/how-to-cancel-this-repeating-alarm) from Ramin Fallahzadeh
    void setNextReminderTime(int timeVal){
        req_id = req_id<0 ? req_id : -req_id;
        Calendar cal = new GregorianCalendar();
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmIntent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
        alarmIntent.putExtra("ID", req_id);
        alarmIntent.putExtra("TITLE", title.getText());
        alarmIntent.putExtra("DESC", description.getText());
        alarmIntent.putExtra("DATE", date );
        alarmIntent.putExtra("TIME", time );
        alarmIntent.putExtra("PER", period );
        alarmIntent.putExtra("IMP", importance );
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), req_id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setAndAllowWhileIdle (AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+timeVal*55*1000,  pendingIntent);
    }



    //TODO: delete if necessary
    void cancelAlarm(){
        alarmManager.cancel(pendingIntent);
        Toast.makeText(getApplicationContext(), req_id+" nd alarm deleted...", Toast.LENGTH_SHORT).show();
    }

    protected void startMediaPlayer(){
        //TODO: edit this if block
        if (importance.equals("High")){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.notification_high);
            mediaPlayer.start();
        }else{
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.notification);
            mediaPlayer.start();
        }
    }
}
