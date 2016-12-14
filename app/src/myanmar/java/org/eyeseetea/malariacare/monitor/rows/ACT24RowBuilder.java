package org.eyeseetea.malariacare.monitor.rows;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.monitor.utils.SurveyMonitor;

/**
 * Created by idelcano on 21/07/2016.
 */
public class ACT24RowBuilder extends CounterRowBuilder {
    protected Object defaultValueColumn() {
        //// TODO: 14/12/2016 Remove it
        return "-";
    }

    public ACT24RowBuilder(Context context) {
        super(context, context.getString(R.string.ACT_x_24));
    }

    @Override
    protected Integer incrementCount(SurveyMonitor surveyMonitor) {
        return (surveyMonitor.isACT24()) ? 1 : 0;
    }
}