/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.database.iomodules.dhis.importer.models;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataValueFlow;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class DataValueExtended implements VisitableFromSDK {

    private final static String TAG = ".DataValueExtended";
    private final static String REGEXP_FACTOR = ".*\\[([0-9]*)\\]";

    DataValueFlow dataValue;

    public DataValueExtended() {
    }

    public DataValueExtended(DataValueFlow dataValue) {
        this.dataValue = dataValue;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public DataValueFlow getDataValue() {
        return dataValue;
    }

    public Option findOptionByQuestion(Question question) {
        if (question == null) {
            return null;
        }

        Answer answer = question.getAnswer();
        if (answer == null) {
            return null;
        }

        List<Option> options = answer.getOptions();
        List<String> optionCodes = new ArrayList<>();
        for (Option option : options) {
            String optionName = option.getName();
            optionCodes.add(optionName);
            if (optionName == null) {
                continue;
            }

            if (optionName.equals(dataValue.getValue())) {
                return option;
            }
        }

        //Log.w(TAG,String.format("Cannot find option '%s'",dataValue.getValue()));
        return null;
    }

    public Collator getDataElement() {
        //// FIXME: 28/12/16
        return null;
    }

    public Collator getValue() {
        //// FIXME: 28/12/16
        return null;
    }
}
