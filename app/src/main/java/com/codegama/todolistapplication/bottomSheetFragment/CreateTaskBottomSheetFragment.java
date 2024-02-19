package com.codegama.todolistapplication.bottomSheetFragment;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.codegama.todolistapplication.R;
import com.codegama.todolistapplication.activity.MainActivity;
import com.codegama.todolistapplication.broadcastReceiver.AlarmBroadcastReceiver;
import com.codegama.todolistapplication.database.DatabaseClient;
import com.codegama.todolistapplication.helper.Helper;
import com.codegama.todolistapplication.model.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.ALARM_SERVICE;

public class CreateTaskBottomSheetFragment extends BottomSheetDialogFragment {

    Unbinder unbinder;
    @BindView(R.id.addTaskTitle)
    EditText addTaskTitle;
    @BindView(R.id.addTaskDescription)
    EditText addTaskDescription;
    @BindView(R.id.taskDate)
    EditText taskDate;
    @BindView(R.id.taskTime)
    EditText taskTime;
    @BindView(R.id.priority)
    Spinner priorityEvent;
    @BindView(R.id.addTask)
    Button addTask;
    @BindView(R.id.spinner)
    Spinner spinner;
    int taskId,repeatPeriod;
    boolean isEdit;
    Task task;
    int mYear, mMonth, mDay;
    int mHour, mMinute;
    setRefreshListener setRefreshListener;
    AlarmManager alarmManager;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    MainActivity activity;
    //public static int count = 0;
    int req_id ;
    Intent alarmIntent;
    PendingIntent pendingIntent;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    public void setTaskId(int taskId, boolean isEdit, setRefreshListener setRefreshListener, MainActivity activity) {
        this.taskId = taskId;
        this.isEdit = isEdit;
        this.activity = activity;
        this.setRefreshListener = setRefreshListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_create_task, null);
        unbinder = ButterKnife.bind(this, contentView);
        dialog.setContentView(contentView);
        alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        addTask.setOnClickListener(view -> {
            if(validateFields())
            createTask();
        });
        if (isEdit) {
            showTaskFromId();
        }

        setSpinnerView();

        taskDate.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(getActivity(),
                        (view1, year, monthOfYear, dayOfMonth) -> {
                            taskDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            datePickerDialog.dismiss();
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
            return true;
        });

        taskTime.setOnTouchListener((view, motionEvent) -> {

            // Get Current Time Format
            boolean is24HourView = DateFormat.is24HourFormat(getContext());

            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                timePickerDialog = new TimePickerDialog(getActivity(),
                        (view24, hourOfDay, minute) -> {
                            taskTime.setText(hourOfDay + ":" + minute);
                            timePickerDialog.dismiss();
                        }, mHour, mMinute, is24HourView);
                timePickerDialog.show();
            }
            return true;
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               repeatPeriod =  Helper.getRepeatPeriodIdx (spinner.getSelectedItem().toString());
                //Toast.makeText(getContext(), Helper.setNextTime(spinner.getSelectedItem().toString(), taskDate.getText().toString(), taskTime.getText().toString()) ,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    public boolean validateFields() {
        if(addTaskTitle.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(activity, "Please enter a valid title", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(addTaskDescription.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(activity, "Please enter a valid description", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(taskDate.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(activity, "Please enter date", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(taskTime.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(activity, "Please enter time", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void createTask() {
        class saveTaskInBackend extends AsyncTask<Void, Void, Void> {
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                Task createTask = new Task();
                createTask.setTaskTitle(addTaskTitle.getText().toString());
                createTask.setTaskDescrption(addTaskDescription.getText().toString());
                createTask.setDate(taskDate.getText().toString());
                createTask.setLastAlarm(taskTime.getText().toString());
                createTask.setTaskPriority(priorityEvent.getSelectedItemPosition());
                createTask.setRepeatPeriod(repeatPeriod);

                if (!isEdit)
                    DatabaseClient.getInstance(getActivity()).getAppDatabase()
                            .dataBaseAction()
                            .insertDataIntoTaskList(createTask);
                else
                    DatabaseClient.getInstance(getActivity()).getAppDatabase()
                            .dataBaseAction()
                            .updateAnExistingRow(taskId, addTaskTitle.getText().toString(),
                                    addTaskDescription.getText().toString(),
                                    taskDate.getText().toString(),
                                    taskTime.getText().toString(),
                                    priorityEvent.getSelectedItemPosition(),
                                    repeatPeriod );

                //TODO: Delete tis log body
                req_id = DatabaseClient.getInstance(getActivity()).getAppDatabase()
                        .dataBaseAction()
                        .getLastItemId();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    createAnAlarm();
                }
                setRefreshListener.refresh();
                Toast.makeText(getActivity(), "Your event is been added", Toast.LENGTH_SHORT).show();
                dismiss();

            }
        }
        saveTaskInBackend st = new saveTaskInBackend();
        st.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createAnAlarm() {
        //TODO: Delete tis log body
        //Log.i("alarming", taskId + " - " +addTaskTitle.getText() + " - "+taskDate.getText()+ " - "+taskTime.getText());
        try {
            String[] items1 = taskDate.getText().toString().split("-");
            String dd = items1[0];
            String month = items1[1];
            String year = items1[2];

            String[] itemTime = taskTime.getText().toString().split(":");
            String hour = itemTime[0];
            String min = itemTime[1];

            //Calendar cur_cal = new GregorianCalendar();
            //cur_cal.setTimeInMillis(System.currentTimeMillis());

            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
            cal.set(Calendar.MINUTE, Integer.parseInt(min));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.DATE, Integer.parseInt(dd));

            alarmIntent = new Intent(activity, AlarmBroadcastReceiver.class);
            alarmIntent.putExtra("ID", req_id);
            alarmIntent.putExtra("TITLE", addTaskTitle.getText().toString());
            alarmIntent.putExtra("DESC", addTaskDescription.getText().toString());
            alarmIntent.putExtra("DATE", taskDate.getText().toString());
            alarmIntent.putExtra("TIME", taskTime.getText().toString());
            alarmIntent.putExtra("PER", spinner.getSelectedItem().toString());
            alarmIntent.putExtra("IMP", priorityEvent.getSelectedItem().toString());
            pendingIntent = PendingIntent.getBroadcast(activity, req_id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //TODO: Delete tis log body
            //Log.i("alarming***", this.taskId + "- SDK : " + Build.VERSION.SDK_INT + " - " + cal.getTimeInMillis()+ " - req_id: " +req_id+ " - period: " +spinner.getSelectedItem().toString());


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (spinner.getSelectedItemPosition() == 0)
                    alarmManager.setExact (AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()-10000,  pendingIntent);
                else
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()-10000, Helper.getRepeatPeriod(spinner.getSelectedItemPosition()), pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showTaskFromId() {
        class showTaskFromId extends AsyncTask<Void, Void, Void> {
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                task = DatabaseClient.getInstance(getActivity()).getAppDatabase()
                        .dataBaseAction().selectDataFromAnId(taskId);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setDataInUI();
            }
        }
        showTaskFromId st = new showTaskFromId();
        st.execute();
    }

    private void setDataInUI() {
        addTaskTitle.setText(task.getTaskTitle());
        addTaskDescription.setText(task.getTaskDescrption());
        taskDate.setText(task.getDate());
        taskTime.setText(task.getLastAlarm());
        priorityEvent.setSelection(task.getTaskPriority());
        spinner.setSelection(task.getRepeatPeriod());
    }

    public interface setRefreshListener {
        void refresh();
    }

    void setSpinnerView(){
        String[] timeVal = {"Once","Fifteen minutes","Half hour","Hourly","Half day","Daily","Weekly"};
        ArrayAdapter<CharSequence> periodAdapter = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_text, timeVal );
        periodAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        spinner.setAdapter(periodAdapter);

        String[] priority = {"Low","Medium","High"};
        ArrayAdapter<CharSequence> priorityAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_text, priority);
        priorityAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        priorityEvent.setAdapter(priorityAdapter);
    }
}
