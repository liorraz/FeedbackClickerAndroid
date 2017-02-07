package com.clicker.clicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {


    public static final int TIME_DEFAULT_SEC = 30;
    public static final int TIME_ADDITION_MILLI = TIME_DEFAULT_SEC * 1000;
    public static final int MAX_TIME_MINUTES = 10;
    private static final int REQUEST_ENABLE_BT = 3;

    //private static final String TAG = "ClickerAPP"; // for logs

    // GUI
    // currently static components are to be used by enum, to be fixed later
    static private View[] classComponents;
    static private View[] timeComponents;
    static private View[] questionComponents;
    static private Button[] mainButtons; // create, right , left
    static private View[] afterQuestionSentComponents;
    static private TextView[] questionLabelComponent; // update question label

    private TextView answerYesLabel; // for counting answers
    private TextView answerNoLabel; // for counting answers
    private EditText numberOfStudentsText; // for counting answers
    private PieGraphView pieGraphView; // for counting answers

    private NumberPicker timeMinutesPicker; // for counting time
    private NumberPicker timeSecondsPicker; // for counting tim

    private EditText questionText; // for deleting

    // End GUI

    private State state = State.CLASS_DEFINITION;

    private CountDownTimer countDownTimer;

    // pie chart
    private int yesNumber = 0;
    private int noNumber = 0;
    private int numberOfStudents = 0;

    private final static Random random = new Random();    // for testing only


    private enum State {
        CLASS_DEFINITION {
            @Override
            State turnTo(State state) {
                if (state == QUESTION_DEFINITION) {
                    makeEnabled(false, classComponents);
                    makeVisible(false, mainButtons[0]);// remove "create" button
                    makeVisible(true, questionComponents);
                    makeEnabled(true, questionComponents);
                    makeVisible(true, timeComponents);
                    makeEnabled(true, timeComponents);
                    questionLabelComponent[0].setText(questionPrefix + " " + questionNumber);
                    makeVisible(true, mainButtons[1]); // add "question" button
                    mainButtons[1].setText(R.string.sendQuestionStr);
                    return state;
                }
                return this;
            }
        }, QUESTION_DEFINITION {
            @Override
            State turnTo(State state) {
                if (state == QUESTION_SENDING) {
                    makeEnabled(false, questionComponents);
                    makeEnabled(false, timeComponents);
                    makeVisible(true, afterQuestionSentComponents);
                    mainButtons[1].setText(R.string.stopTimeStr);
                    makeVisible(true, mainButtons[2]);
                    makeEnabled(true, mainButtons[2]);
                    return state;
                }
                return this;
            }
        }, QUESTION_SENDING {
            @Override
            State turnTo(State state) {
                if (state == QUESTION_STOPPING) {
                    mainButtons[1].setText(R.string.newQuestionStr);
                    makeEnabled(false, mainButtons[2]);
                    return state;
                }
                return this;
            }
        }, QUESTION_STOPPING {
            @Override
            State turnTo(State state) {
                if (state == QUESTION_DEFINITION) {
                    makeVisible(false, afterQuestionSentComponents);
                    makeEnabled(true, timeComponents);
                    makeVisible(false, mainButtons[2]);
                    mainButtons[1].setText(R.string.sendQuestionStr);
                    makeEnabled(true, questionComponents);
                    makeEnabled(true, timeComponents);
                    questionLabelComponent[0].setText(questionPrefix + " " + questionNumber);
                    return state;
                }

                return this;
            }
        };

        private static void makeEnabled(boolean enabled, View... components) {
            for (View component : components) {
                component.setEnabled(enabled);
            }
        }

        private static void makeVisible(boolean visible, View... components) {
            int state = visible ? View.VISIBLE : View.INVISIBLE;
            for (View component : components) {
                component.setVisibility(state);
            }
        }

        abstract State turnTo(State state);
    }

    private static /* final after onCreate */ String questionPrefix;

    private static int questionNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storeUI();
    }

    private void storeUI() {
        EditText classNameText = (EditText) findViewById(R.id.classNameText);
        TextView classNameLabel = (TextView) findViewById(R.id.classNameLabel);
        timeMinutesPicker = (NumberPicker) findViewById(R.id.timeMinPick);
        timeSecondsPicker = (NumberPicker) findViewById(R.id.timeSecPic);
        timeSecondsPicker.setMinValue(0);
        timeSecondsPicker.setMaxValue(59);
        timeMinutesPicker.setMinValue(0);
        timeMinutesPicker.setMaxValue(MAX_TIME_MINUTES);
        timeSecondsPicker.setValue(TIME_DEFAULT_SEC);
        TextView questionLabel = (TextView) findViewById(R.id.questionLabel);
        questionPrefix = questionLabel.getText().toString();
        questionLabel.setText(questionPrefix + " " + questionNumber); // locale not good! (hebrew)
        questionText = (EditText) findViewById(R.id.questionText);
        Button eraseQuestionButton = (Button) findViewById(R.id.eraseQuestionButton);
        TextView timeSeparatorLabel = (TextView) findViewById(R.id.timeSeperatorLabel);
        TextView timeLabel = (TextView) findViewById(R.id.timeLabel);
        Button createClassButton = (Button) findViewById(R.id.createClassButton);
        Button leftButton = (Button) findViewById(R.id.buttonLeft);
        Button rightButton = (Button) findViewById(R.id.buttonRight);

        numberOfStudentsText = (EditText) findViewById(R.id.numberOfStudentsText);
        answerYesLabel = (TextView) findViewById(R.id.answerYesLabel);
        answerNoLabel = (TextView) findViewById(R.id.answerNoLabel);

        classComponents = new View[]{classNameText, classNameLabel, createClassButton, numberOfStudentsText};
        timeComponents = new View[]{timeLabel, timeSecondsPicker, timeSeparatorLabel, timeMinutesPicker};
        questionComponents = new View[]{questionLabel, questionText, eraseQuestionButton};
        questionLabelComponent = new TextView[]{questionLabel};
        mainButtons = new Button[]{createClassButton, rightButton, leftButton};

        LinearLayout pieChartContainer = (LinearLayout) findViewById(R.id.pieChartGrid);
        // calculate in re-write data
        float[] angleValues = rawDataToAngles(new float[]{0, 0, 1});
        pieGraphView = new PieGraphView(this, angleValues);
        pieGraphView.setVisibility(View.INVISIBLE);
        pieChartContainer.addView(pieGraphView);

        afterQuestionSentComponents = new View[]{answerYesLabel, answerNoLabel, pieGraphView};
    }


    private float[] rawDataToAngles(float[] data) {
        float total = 0;
        for (float value : data) {
            total += value;
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = 360 * (data[i] / total);
        }
        return data;

    }


    private void countDown(long millisecondsInFuture) {
        countDownTimer = new CountDownTimer(millisecondsInFuture, 1000 /* each 1 sec */) {

            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;
                int seconds = (int) (secondsUntilFinished % 60);
                int minutes = (int) (secondsUntilFinished / 60);
                timeMinutesPicker.setValue(minutes);
                timeSecondsPicker.setValue(seconds);
                randomizePeople();
            }

            private void randomizePeople() {
                int currentYes = random.nextInt(3);
                int currentNo = 2 - currentYes;
                int tillNow = yesNumber + noNumber;
                if (tillNow + currentYes <= numberOfStudents) {
                    yesNumber += currentYes;
                    tillNow += currentYes;
                }
                if (tillNow + currentNo <= numberOfStudents) {
                    noNumber += currentNo;
                    tillNow += currentNo;
                }

                if (tillNow == numberOfStudents) {
                    stopTimer();
                }

                updateChartValues(yesNumber, noNumber, numberOfStudents - tillNow);
            }

            public void onFinish() {
                timeMinutesPicker.setValue(0);
                timeSecondsPicker.setValue(0);
                countDownEnded();
            }


        };
        countDownTimer.start();

    }

    private void countDownEnded() {
        state = state.turnTo(State.QUESTION_STOPPING);
    }

    private void updateChartValues(final int answeredYes, final int answeredNo, final int noAnswered) {
        final float[] values = rawDataToAngles(new float[]{answeredYes, answeredNo, noAnswered});

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                answerYesLabel.setText(getString(R.string.answerYes) + " " + answeredYes);
                answerNoLabel.setText(getString(R.string.answerNo) + " " + answeredNo);
                pieGraphView.setPieDegreesValues(values);
            }
        });
    }


    public void createClassButtonOnClick(View view) {
        numberOfStudents = Integer.parseInt(numberOfStudentsText.getText().toString());
        state = state.turnTo(State.QUESTION_DEFINITION);
    }


    public void rightButtonOnClick(View view) {
        if (state == State.QUESTION_DEFINITION) {
            long timeMilliseconds = getTimeFieldsMilliseconds();
            if (timeMilliseconds == 0) {
                // wait till time > 0 , need to tell user
                return;
            }

            questionNumber++;

            countDown(timeMilliseconds);

            // change start
            state = state.turnTo(State.QUESTION_SENDING);

        } else if (state == State.QUESTION_SENDING) {
            stopTimer();
        } else if (state == State.QUESTION_STOPPING) {
            state = state.turnTo(State.QUESTION_DEFINITION);
            timeSecondsPicker.setValue(TIME_DEFAULT_SEC);
            timeMinutesPicker.setValue(0);
            yesNumber = 0;
            noNumber = 0;
        }
    }

    private void stopTimer() {
        countDownTimer.cancel();
        countDownEnded();
    }


    private long getTimeFieldsMilliseconds() {
        long minutes = timeMinutesPicker.getValue();
        long seconds = timeSecondsPicker.getValue();
        return ((minutes * 60) + seconds) * 1000;
    }

    public void leftButtonOnClick(View view) {
        // more time
        countDownTimer.cancel();
        long newTimeMilliseconds = getTimeFieldsMilliseconds();
        newTimeMilliseconds += TIME_ADDITION_MILLI;
        if (newTimeMilliseconds > ((MAX_TIME_MINUTES * 60) + 59) * 1000) {
            newTimeMilliseconds = ((MAX_TIME_MINUTES * 60) + 59) * 1000;
        }
        countDown(newTimeMilliseconds);
    }


    public void eraseQuestionOnClick(View view) {
        questionText.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.debug_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.change_bt_name:
                BTChangeName();
                return true;
            case R.id.enable_bt_discoverability:
                enableBTDiscoverability();
                return true;
            case R.id.scan_bt_devices:
                scanBTDevices();
                return true;
            case R.id.enable_ble:
                enableBLE();
                return true;
            case R.id.change_ble_name:
                toast("Not supported - change BT name ...");
                return true;
            case R.id.scan_ble_devices:
                scanBLEDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //private boolean mScanningBLE = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = null;

    private void scanBLEDevices() {

        if (mLeScanCallback == null) {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toast("BLE Devices found with name: " + device.getName() + " address: " + device.getAddress());
                        }
                    });
                }
            };
        }


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage("Set duration of Scan BLE Devices");
        alertDialog.setTitle("Scan BLE Devices");
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setPositiveButton("Enable",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Stops scanning after 10 seconds.
                        int period = Integer.parseInt(input.getText().toString());
                        scanLeDevice(true, period);
                    }
                });

        alertDialog.setNegativeButton("Disable",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        scanLeDevice(false, 0);
                        dialog.cancel();
                    }
                });

        alertDialog.show();


    }

    private Handler scanLEDevicesHandler = null;

    private void scanLeDevice(final boolean enable, final int period) {
        if (mBluetoothAdapter == null) {
            toast("Can't scan devices since the BLE adapter is null!");
            return;
        }
        if (enable) {
            if (scanLEDevicesHandler == null) {
                scanLEDevicesHandler = new Handler();
            }
            // Stops scanning after a pre-defined scan period.
            scanLEDevicesHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    mScanningBLE = false;
                    //noinspection deprecation
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, period);

//            mScanningBLE = true;
            //noinspection deprecation
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
//            mScanningBLE = false;
            //noinspection deprecation
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }


    private void enableBLE() {

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                toast("BT for BLE approved by user");
            } else {
                toast("BT for BLE not approved, return code: " + resultCode);
            }
        }

    }

    private BroadcastReceiver mReceiver = null;

    private void scanBTDevices() {
        // Create a BroadcastReceiver for ACTION_FOUND.
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC addressZ
                    toast("BT Device found with name: " + deviceName + " , mac: " + deviceHardwareAddress);
                }
            }
        };


        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        toast("mReceiver registered");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mReceiver != null) {
            // Don't forget to unregister the ACTION_FOUND receiver.
            unregisterReceiver(mReceiver);

        }
    }


    private void enableBTDiscoverability() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage("Set time for discoverability");
        alertDialog.setTitle("Enable Discoverability");
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setPositiveButton("Go",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int time = Integer.parseInt(input.getText().toString());
                        Intent discoverableIntent =
                                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time);
                        startActivity(discoverableIntent);
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }


    private void BTChangeName() {
        // BT Rename
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage("Select the new BT Name");
        alertDialog.setTitle("Change BT Name");
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setPositiveButton("Go",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString();
                        changeBTName(newName);
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void changeBTName(final String newName) {
        if (newName.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Can't have empty BT Name", Toast.LENGTH_SHORT).show();
        }

        final BluetoothAdapter myBTAdapter = BluetoothAdapter.getDefaultAdapter();
        final long timeToGiveUpMs = System.currentTimeMillis() + 10000;
        if (myBTAdapter == null) {
            toast("No BT Support !!!");
        } else {
            String oldName = myBTAdapter.getName();
            toast("OLD BT Name is: " + oldName);
            if (!oldName.equalsIgnoreCase(newName)) {
                final Handler myTimerHandler = new Handler();
                myBTAdapter.enable();
                myTimerHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (myBTAdapter.isEnabled()) {
                                    myBTAdapter.setName(newName);
                                    if (newName.equalsIgnoreCase(myBTAdapter.getName())) {
                                        toast("Updated BT Name to " + myBTAdapter.getName());
                                        myBTAdapter.disable();
                                    }
                                }
                                if ((!newName.equalsIgnoreCase(myBTAdapter.getName())) && (System.currentTimeMillis() < timeToGiveUpMs)) {
                                    myTimerHandler.postDelayed(this, 500);
                                    if (myBTAdapter.isEnabled())
                                        toast("Update BT Name: waiting on BT Enable");
                                    else
                                        toast("Update BT Name: waiting for Name (" + newName + ") to set in");
                                }
                            }
                        }, 500);
            }
        }
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
