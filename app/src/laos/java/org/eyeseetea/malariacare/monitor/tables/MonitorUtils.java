package org.eyeseetea.malariacare.monitor.tables;

import android.content.Context;

import org.eyeseetea.malariacare.monitor.MonitorRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.ACT6x1RowBuilder;
import org.eyeseetea.malariacare.monitor.rows.ACT6x2RowBuilder;
import org.eyeseetea.malariacare.monitor.rows.ACT6x3RowBuilder;
import org.eyeseetea.malariacare.monitor.rows.ACT6x4RowBuilder;
import org.eyeseetea.malariacare.monitor.rows.CombinedACTRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.DeniedRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.DrugRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.NegativeRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.PfPvRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.PfRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.PositiveRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.PositivityRateRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.PregnantRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.PvRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.RDTRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.ReferralRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.SevereRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.SubmissionRowBuilder;
import org.eyeseetea.malariacare.monitor.rows.SuspectedRowBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 21/07/2016.
 */
public class MonitorUtils extends AMonitorUtils {

    public MonitorUtils(Context context) {
        super(context);
    }

    public List<MonitorRowBuilder> defineRows(){
        List<MonitorRowBuilder> rowBuilders = new ArrayList<>();
        //rowBuilders.add(new PeriodRowBuilder(context));
        rowBuilders.add(new RDTRowBuilder(context));
        rowBuilders.add(new ACT6x1RowBuilder(context));
        rowBuilders.add(new ACT6x2RowBuilder(context));
        rowBuilders.add(new ACT6x3RowBuilder(context));
        rowBuilders.add(new ACT6x4RowBuilder(context));
        rowBuilders.add(new CombinedACTRowBuilder(context));
        return rowBuilders;
    }

    public List<MonitorRowBuilder> defineSuspectedRows() {
        List<MonitorRowBuilder> rowBuilders = new ArrayList<>();
        //rowBuilders.add(new PeriodRowBuilder(context));
        rowBuilders.add(new SuspectedRowBuilder(context));
        rowBuilders.add(new PositiveRowBuilder(context));
        rowBuilders.add(new PfRowBuilder(context));
        rowBuilders.add(new PvRowBuilder(context));
        rowBuilders.add(new PfPvRowBuilder(context));
        rowBuilders.add(new ReferralRowBuilder(context));
        rowBuilders.add(new NegativeRowBuilder(context));
        rowBuilders.add(new PositivityRateRowBuilder(context));
        rowBuilders.add(new SubmissionRowBuilder(context));
        rowBuilders.add(new SevereRowBuilder(context));
        rowBuilders.add(new PregnantRowBuilder(context));
        rowBuilders.add(new DeniedRowBuilder(context));
        rowBuilders.add(new DrugRowBuilder(context));
        return rowBuilders;
    }
}
