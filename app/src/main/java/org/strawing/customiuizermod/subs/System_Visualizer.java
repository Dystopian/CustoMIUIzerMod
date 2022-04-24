package org.strawing.customiuizermod.subs;

import android.os.Bundle;
import android.preference.Preference;

import org.strawing.customiuizermod.SubFragment;
import org.strawing.customiuizermod.utils.Helpers;

public class System_Visualizer extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		findPreference("pref_key_system_visualizer_colorval").setEnabled("2".equals(Helpers.prefs.getString("pref_key_system_visualizer_color", "1")));
		findPreference("pref_key_system_visualizer_dyntime").setEnabled("5".equals(Helpers.prefs.getString("pref_key_system_visualizer_color", "1")));
		findPreference("pref_key_system_visualizer_color").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_visualizer_colorval").setEnabled("2".equals(newValue));
				findPreference("pref_key_system_visualizer_dyntime").setEnabled("5".equals(newValue));
				return true;
			}
		});

		findPreference("pref_key_system_visualizer_colorval").setOnPreferenceClickListener(openColorSelector);
	}

}
