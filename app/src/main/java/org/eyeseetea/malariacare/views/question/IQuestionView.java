package org.eyeseetea.malariacare.views.question;

import org.eyeseetea.malariacare.database.model.Value;

public interface IQuestionView {
    void setEnabled(boolean enabled);

    void setHelpText(String helpText);

    void setValue(Value value);
}
