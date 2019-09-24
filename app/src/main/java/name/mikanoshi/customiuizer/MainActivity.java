package name.mikanoshi.customiuizer;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

import name.mikanoshi.customiuizer.utils.Helpers;

public class MainActivity extends Activity {

	MainFragment mainFrag = null;
	SharedPreferences.OnSharedPreferenceChangeListener prefsChanged;
	FileObserver fileObserver;

	@Override
	protected void attachBaseContext(Context base) {
		try {
			super.attachBaseContext(Helpers.getLocaleContext(base));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		boolean mSDKFound = ((MainApplication)getApplication()).mStarted;
		if (mSDKFound) Helpers.setMiuiTheme(this, R.style.MIUIPrefs);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (!mSDKFound) {
			Toast.makeText(this, R.string.sdk_failed, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		prefsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
				Log.i("prefs", "Changed: " + key);
				requestBackup();
				Object val = sharedPrefs.getAll().get(key);
				String path = "";
				if (val instanceof String)
					path = "string/";
				else if (val instanceof Set<?>)
					path = "stringset/";
				else if (val instanceof Integer)
					path = "integer/";
				else if (val instanceof Boolean)
					path = "boolean/";
				getContentResolver().notifyChange(Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/" + path + key), null);
				if (!path.equals(""))
				getContentResolver().notifyChange(Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/pref/" + path + key), null);
			}
		};
		Helpers.prefs.registerOnSharedPreferenceChangeListener(prefsChanged);
		Helpers.fixPermissionsAsync(getApplicationContext());

		try {
			fileObserver = new FileObserver(Helpers.getProtectedContext(this).getDataDir() + "/shared_prefs", FileObserver.CLOSE_WRITE) {
				@Override
				public void onEvent(int event, String path) {
					Helpers.fixPermissionsAsync(getApplicationContext());
				}
			};
			fileObserver.startWatching();
		} catch (Throwable t) {
			Log.e("prefs", "Failed to start FileObserver!");
		}

		Helpers.updateNewModsMarking(this);

		mainFrag = new MainFragment();
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFrag).commit();
	}

	protected void onDestroy() {
		try {
			if (prefsChanged != null) Helpers.prefs.unregisterOnSharedPreferenceChangeListener(prefsChanged);
			if (fileObserver != null) fileObserver.stopWatching();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_container);
		if (fragment != null)
		if (((PreferenceFragmentBase)fragment).isAnimating) return;
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			PreferenceFragmentBase fragment = (PreferenceFragmentBase)getFragmentManager().findFragmentById(R.id.fragment_container);
			if (fragment instanceof MainFragment && fragment.getView() != null) try {
				fragment.getView().post(fragment::showImmersionMenu);
				return true;
			} catch (Throwable t) {}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void requestBackup() {
		BackupManager bm = new BackupManager(getApplicationContext());
		bm.dataChanged();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case Helpers.REQUEST_PERMISSIONS_BACKUP:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					mainFrag.backupSettings(this);
				else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
					Toast.makeText(this, R.string.permission_save, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show();
				break;
			case Helpers.REQUEST_PERMISSIONS_RESTORE:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					mainFrag.restoreSettings(this);
				else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
					Toast.makeText(this, R.string.permission_restore, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show();
				break;
			case Helpers.REQUEST_PERMISSIONS_WIFI:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Fragment frag = getFragmentManager().findFragmentById(R.id.fragment_container);
					if (frag instanceof name.mikanoshi.customiuizer.subs.System_NoScreenLock)
					((name.mikanoshi.customiuizer.subs.System_NoScreenLock)frag).openWifiNetworks();
				} else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
					Toast.makeText(this, R.string.permission_scan, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show();
				break;
			case Helpers.REQUEST_PERMISSIONS_REPORT:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					mainFrag.createReport();
				else
					Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show();
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

}