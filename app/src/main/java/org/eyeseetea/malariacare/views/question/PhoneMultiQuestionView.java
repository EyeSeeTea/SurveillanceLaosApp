package org.eyeseetea.malariacare.views.question;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.domain.entity.Phone;
import org.eyeseetea.malariacare.domain.exception.InvalidPhoneException;
import org.eyeseetea.malariacare.views.EditCard;
import org.eyeseetea.malariacare.views.TextCard;

public class PhoneMultiQuestionView extends AMultiQuestionView {
    TextCard header;
    EditCard editCard;

    public PhoneMultiQuestionView(Context context) {
        super(context);

        init(context);
    }

    @Override
    public void setHeader(String headerValue) {
        header.setText(headerValue);
    }

    @Override
    public void setEnabled(boolean enabled) {
        editCard.setEnabled(enabled);
    }

    @Override
    public void setValue(Value value) {
        if (value != null) {
            editCard.setText(value.getValue());
        }
    }

    private void init(final Context context) {
        inflate(context, R.layout.multi_question_tab_phone_row, this);

        header = (TextCard) findViewById(R.id.row_header_text);
        editCard = (EditCard) findViewById(R.id.answer);

        editCard.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    Phone phone = new Phone(editCard.getText().toString());
                    notifyAnswerChanged(phone.getValue());

                } catch (InvalidPhoneException e) {
                    editCard.setError(
                            context.getString(R.string.dynamic_error_phone_format));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
    }
}
