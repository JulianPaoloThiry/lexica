/*
 *  Copyright (C) 2008-2009 Rev. Johnny Healey <rev.null@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.serwylo.lexica;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

public class LexicaConfig extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
       	super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
        findPreference("resetScores").setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("resetScores".equals(preference.getKey())) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.pref_resetScores))
                    .setMessage(getString(R.string.reset_scores_prompt))
                    .setPositiveButton(android.R.string.ok, promptListener)
                    .setNegativeButton(android.R.string.cancel, promptListener)
                    .create().show();
            return true;
        }
        return false;
    }

    private DialogInterface.OnClickListener promptListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                clearHighScores();
            }
        }
    };

    private void clearHighScores() {
        getSharedPreferences(ScoreActivity.SCORE_PREF_FILE, Context.MODE_PRIVATE).edit().clear().commit();
        Toast.makeText(this, R.string.high_scores_reset, Toast.LENGTH_SHORT).show();
    }
}
