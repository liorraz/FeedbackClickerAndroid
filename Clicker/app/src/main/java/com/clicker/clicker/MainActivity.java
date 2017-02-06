package com.clicker.clicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {


    public static final int TIME_DEFAULT_SEC = 30;
    public static final int TIME_ADDITION_MILLI = TIME_DEFAULT_SEC * 1000;
    public static final int MAX_TIME_MINUTES = 10;
    // GUI

    static private View[] classComponents;
    static private View[] timeComponents;
    static private View[] questionComponents;
    static private Button[] buttons; // create, right , left
    static private View[] afterQuestionSentComponents;

    static private TextView[] questionLabelComponent;

    private TextView answerYesLabel;
    private TextView answerNoLabel;

    private NumberPicker timeMinutesPicker;
    private NumberPicker timeSecondsPicker;

    private EditText numberOfStudentsText;

    private MyGraphView pieChartView;

    // End GUI

    private State state = State.CLASS_DEFINITION;
    private CountDownTimer countDownTimer;


    // pie test
    private int yesNumber = 0;
    private int noNumber = 0;
    private int numberOfStudents = 0;

    private final static Random random = new Random();


    private enum State {
        CLASS_DEFINITION {
            @Override
            State turnTo(State state) {
                if (state == QUESTION_DEFINITION) {
                    makeEnabled(false, classComponents);
                    makeVisible(false, buttons[0]);// remove "create" button
                    makeVisible(true, questionComponents);
                    makeEnabled(true, questionComponents);
                    makeVisible(true, timeComponents);
                    makeEnabled(true, timeComponents);
                    questionLabelComponent[0].setText(questionPrefix + " " + questionNumber);
                    makeVisible(true, buttons[1]); // add "question" button
                    buttons[1].setText(R.string.sendQuestionStr);
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
                    buttons[1].setText(R.string.stopTimeStr);
                    makeVisible(true, buttons[2]);
                    makeEnabled(true, buttons[2]);
                    return state;
                }
                return this;
            }
        }, QUESTION_SENDING {
            @Override
            State turnTo(State state) {
                if (state == QUESTION_STOPPING) {
                    buttons[1].setText(R.string.newQuestionStr);
                    makeEnabled(false, buttons[2]);
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
                    makeVisible(false, buttons[2]);
                    buttons[1].setText(R.string.sendQuestionStr);
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
        EditText questionText = (EditText) findViewById(R.id.questionText);
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
        questionComponents = new View[]{questionLabel, questionText};
        questionLabelComponent = new TextView[]{questionLabel};
        buttons = new Button[]{createClassButton, rightButton, leftButton};

        LinearLayout pieChartContainer = (LinearLayout) findViewById(R.id.pieChartGrid);
        // calculate in re-write data
        float[] angleValues = rawDataToAngles(new float[]{0, 0, 1});
        pieChartView = new MyGraphView(this, angleValues);
        pieChartView.setVisibility(View.INVISIBLE);
        pieChartContainer.addView(pieChartView);

        afterQuestionSentComponents = new View[]{answerYesLabel, answerNoLabel, pieChartView};
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
                pieChartView.setValues(values);
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

    private class MyGraphView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float[] valueDegrees;
        private final int[] COLORS = {Color.GREEN, Color.RED, Color.GRAY};
        private RectF rectf = new RectF(50, 50, 500, 500);


        private MyGraphView(Context context, float[] valueDegrees) {
            super(context);
            validateLength(valueDegrees);
            this.valueDegrees = valueDegrees;
        }

        private void validateLength(float[] valueDegrees) {
            if (COLORS.length != valueDegrees.length) {
                throw new RuntimeException("Can't set values for length different than " + COLORS.length);
            }
        }


        private void setValues(float[] values) {
            validateLength(values);

            // copy to mine
            System.arraycopy(values, 0, valueDegrees, 0, values.length);
            invalidate();
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float temp = 0;
            for (int i = 0; i < valueDegrees.length; i++) {
                if (i == 0) {
                    paint.setColor(COLORS[i]);
                    canvas.drawArc(rectf, 0, valueDegrees[i], true, paint);
                } else {
                    temp += valueDegrees[i - 1];
                    paint.setColor(COLORS[i]);
                    canvas.drawArc(rectf, temp, valueDegrees[i], true, paint);
                }
            }
        }

    }
}
