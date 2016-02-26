package org.eyeseetea.malariacare.monitor.rows;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.monitor.MonitorRowBuilder;
import org.eyeseetea.malariacare.monitor.utils.SurveyStats;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arrizabalaga on 26/02/16.
 */
public class SuspectedRowBuilder extends CounterRowBuilder {

    public SuspectedRowBuilder(Context context){
        super(context, context.getString(R.string.monitor_row_title_suspected));
    }

    @Override
    protected boolean hasToIncrement(Survey survey) {
        return SurveyStats.isSuspected(survey);
    }
}
