package com.klinker.android.messaging_sliding;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewScheduledSms extends Activity implements AdapterView.OnItemSelectedListener{

    private Context context;

    private EditText mEditText;

    private ListPopupWindow lpw;

    private int currentYear;
    private int currentMonth;
    private int currentDay;
    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;
    int hour;
    int minute;
    private Button btDate;
    private Button btTime;
    private TextView timeDisplay;
    private TextView dateDisplay;
    private EditText contactSearch;
    private EditText messageText;

    private String repetition;

    public SharedPreferences sharedPrefs;

    public boolean firstContactSearch = true;

    public ArrayList<String> contactNames, contactNumbers, contactTypes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduled_sms_activity);

        sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        context = this;

        mEditText = (EditText) findViewById(R.id.messageEntry2);

        final Calendar c = Calendar.getInstance();
        currentYear = c.get(Calendar.YEAR);
        currentMonth = c.get(Calendar.MONTH);
        currentDay = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        timeDisplay = (TextView) findViewById(R.id.currentTime);
        dateDisplay = (TextView) findViewById(R.id.currentDate);
        btDate = (Button) findViewById(R.id.setDate);
        btTime = (Button) findViewById(R.id.setTime);

        lpw = new ListPopupWindow(NewScheduledSms.this);

        lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                TextView view2 = (TextView) arg1.findViewById(R.id.receivedMessage);

                String[] t1 = contactSearch.getText().toString().split("; ");
                String string = "";

                for (int i = 0; i < t1.length - 1; i++)
                {
                    string += t1[i] + "; ";
                }

                contactSearch.setText(string + view2.getText() + "; ");
                contactSearch.setSelection(contactSearch.getText().length());
                lpw.dismiss();
                firstContactSearch = true;
            }

        });

        // Creates the spinner for repetition of scheduled sms
        Spinner spinner = (Spinner) findViewById(R.id.repetitionSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.repetition_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        contactSearch = (EditText) findViewById(R.id.contactEntry);

        contactSearch.requestFocus();

        contactSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (firstContactSearch)
                {
                    try
                    {
                        contactNames = new ArrayList<String>();
                        contactNumbers = new ArrayList<String>();
                        contactTypes = new ArrayList<String>();

                        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                        String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};

                        Cursor people = getContentResolver().query(uri, projection, null, null, null);

                        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                        people.moveToFirst();
                        do {
                            int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                            String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                            if (sharedPrefs.getBoolean("mobile_only", false))
                            {
                                if (type == 2)
                                {
                                    contactNames.add(people.getString(indexName));
                                    contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                                    contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
                                }
                            } else
                            {
                                contactNames.add(people.getString(indexName));
                                contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                                contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
                            }
                        } while (people.moveToNext());
                        people.close();
                    } catch (IllegalArgumentException e)
                    {

                    }


                }
            }

            @SuppressLint("DefaultLocale")
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<String> searchedNames = new ArrayList<String>();
                ArrayList<String> searchedNumbers = new ArrayList<String>();
                ArrayList<String> searchedTypes = new ArrayList<String>();

                String text = contactSearch.getText().toString();

                String[] text2 = text.split("; ");

                text = text2[text2.length-1];

                if (text.startsWith("+"))
                {
                    text = text.substring(1);
                }

                Pattern pattern;

                try
                {
                    pattern = Pattern.compile(text.toLowerCase());
                } catch (Exception e)
                {
                    pattern = Pattern.compile(text.toLowerCase().replace("(", "").replace(")", "").replace("?", "").replace("[", "").replace("{", "").replace("}", "").replace("\\", ""));
                }

                for (int i = 0; i < contactNames.size(); i++)
                {
                    try
                    {
                        Long.parseLong(text);

                        if (text.length() <= contactNumbers.get(i).length())
                        {
                            Matcher matcher = pattern.matcher(contactNumbers.get(i));
                            if(matcher.find())
                            {
                                searchedNames.add(contactNames.get(i));
                                searchedNumbers.add(contactNumbers.get(i));
                                searchedTypes.add(contactTypes.get(i));
                            }
                        }
                    } catch (Exception e)
                    {
                        if (contactNames == null)
                        {
                            contactNames = new ArrayList<String>();
                            contactNumbers = new ArrayList<String>();
                            contactTypes = new ArrayList<String>();
                        }
                        if (text.length() <= contactNames.get(i).length())
                        {
                            Matcher matcher = pattern.matcher(contactNames.get(i).toLowerCase());
                            if(matcher.find())
                            {
                                searchedNames.add(contactNames.get(i));
                                searchedNumbers.add(contactNumbers.get(i));
                                searchedTypes.add(contactTypes.get(i));
                            }
                        }
                    }
                }

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;

                lpw.setAdapter(new ContactSearchArrayAdapter((Activity)context, searchedNames, searchedNumbers, searchedTypes));
                lpw.setAnchorView(findViewById(R.id.contactEntry));
                lpw.setWidth(width - 20); // TODO better sizing
                lpw.setHeight(height/3);

                if (firstContactSearch)
                {
                    lpw.show();
                    firstContactSearch = false;
                }

                if (text.length() == 0)
                {
                    lpw.dismiss();
                    firstContactSearch = true;
                }


            }

            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton emojiButton = (ImageButton) findViewById(R.id.display_emoji);

        if (!sharedPrefs.getBoolean("emoji", false))
        {
            emojiButton.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mEditText.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mEditText.setLayoutParams(params);
        } else
        {
            emojiButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Insert Emojis");
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    View frame = inflater.inflate(R.layout.emoji_frame, null);

                    final EditText editText = (EditText) frame.findViewById(R.id.emoji_text);
                    ImageButton peopleButton = (ImageButton) frame.findViewById(R.id.peopleButton);
                    ImageButton objectsButton = (ImageButton) frame.findViewById(R.id.objectsButton);
                    ImageButton natureButton = (ImageButton) frame.findViewById(R.id.natureButton);
                    ImageButton placesButton = (ImageButton) frame.findViewById(R.id.placesButton);
                    ImageButton symbolsButton = (ImageButton) frame.findViewById(R.id.symbolsButton);

                    final GridView emojiGrid = (GridView) frame.findViewById(R.id.emojiGrid);
                    Button okButton = (Button) frame.findViewById(R.id.emoji_ok);

                    if (sharedPrefs.getBoolean("emoji_type", true))
                    {
                        emojiGrid.setAdapter(new EmojiAdapter2(context));
                        emojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                            {
                                editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
                                editText.setSelection(editText.getText().length());
                            }
                        });

                        peopleButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(0);
                            }
                        });

                        objectsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + (2 * 7));
                            }
                        });

                        natureButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + (3 * 7));
                            }
                        });

                        placesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                            }
                        });

                        symbolsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                            }
                        });
                    } else
                    {
                        emojiGrid.setAdapter(new EmojiAdapter(context));
                        emojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                            {
                                editText.setText(EmojiConverter.getSmiledText(context, editText.getText().toString() + EmojiAdapter.mEmojiTexts[position]));
                                editText.setSelection(editText.getText().length());
                            }
                        });

                        peopleButton.setMaxHeight(0);
                        objectsButton.setMaxHeight(0);
                        natureButton.setMaxHeight(0);
                        placesButton.setMaxHeight(0);
                        symbolsButton.setMaxHeight(0);

                        LinearLayout buttons = (LinearLayout) frame.findViewById(R.id.linearLayout);
                        buttons.setMinimumHeight(0);
                        buttons.setVisibility(View.GONE);
                    }

                    builder.setView(frame);
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    okButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (sharedPrefs.getBoolean("emoji_type", true))
                            {
                                mEditText.setText(EmojiConverter2.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
                                mEditText.setSelection(mEditText.getText().length());
                            } else
                            {
                                mEditText.setText(EmojiConverter.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
                                mEditText.setSelection(mEditText.getText().length());
                            }

                            dialog.dismiss();
                        }

                    });
                }

            });
        }

        btDate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                showDialog(DATE_DIALOG_ID);
            }
        });
        btTime.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });

        // Inflate a "Done/Discard" custom action bar view.
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_done_discard, null);

        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doneClick();
                    }
                });

        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        discardClick();
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, reservationDate, currentYear,
                        currentMonth, currentDay);
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, timeDate, hour, minute, false);
        }
        return null;
    }

    // To-do: make a date object to display in different time formats, check out the messageArrayAdapter class, it works with the dates
    private DatePickerDialog.OnDateSetListener reservationDate = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
                dateDisplay.setText((month + 1) + "/" + day + "/" + year);
        }

    };

    private TimePickerDialog.OnTimeSetListener timeDate = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hours, int minutes) {
            if (minutes < 10)
            {
                timeDisplay.setText(hours + ":0" + minutes);
            } else
            {
                timeDisplay.setText(hours + ":" + minutes);
            }
        }
    };

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        repetition = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public boolean discardClick()
    {
        finish();
        return true;
    }

    public boolean doneClick()
    {
        // This is where we will get all of the information that we need and set the alarm manager
        finish();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {

    }
}