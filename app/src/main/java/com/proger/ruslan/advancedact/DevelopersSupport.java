package com.proger.ruslan.advancedact;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class DevelopersSupport extends AppCompatActivity {

    //здесь происходит обработка отправления писем для разработчика от пользователя

    public final String DEVELOPER_EMAIL = "ruslann09@bk.ru";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developers_support);

        ((Button) findViewById(R.id.sendFeedBack)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final EditText nameField = (EditText) findViewById(R.id.editUserName);
                    String name = nameField.getText().toString();

                    final EditText feedbackField = (EditText) findViewById(R.id.editFeedbackMessage);
                    String feedback = feedbackField.getText().toString();

                    final Spinner feedbackSpinner = (Spinner) findViewById(R.id.feedbackTypeSpinner);
                    String feedbackType = feedbackSpinner.getSelectedItem().toString();

                    final CheckBox responseCheckbox = (CheckBox) findViewById(R.id.responseCheckBox);
                    boolean importMessage = responseCheckbox.isChecked();

                    if ((name == "" || name.length() < 1) || (feedback == "" || feedback.length() < 1)
                            || (feedbackType == "" || feedbackType.length() < 1)) {
                        Toast.makeText(getApplicationContext(), R.string.fill_in_all, Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {DEVELOPER_EMAIL});
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, feedbackType);
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, name + "\n" + feedback
                            + "\n" + (importMessage ? "---import": ""));
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.emai_transfer)));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        ((Button) findViewById(R.id.clearAllRows)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText nameField = (EditText) findViewById(R.id.editUserName);
                nameField.setText("");
                final EditText feedbackField = (EditText) findViewById(R.id.editFeedbackMessage);
                feedbackField.setText("");
                final CheckBox responseCheckbox = (CheckBox) findViewById(R.id.responseCheckBox);
                responseCheckbox.setChecked(false);
            }
        });
    }
}
