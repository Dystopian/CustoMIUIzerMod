package org.strawing.customiuizermod.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.strawing.customiuizermod.mods.GlobalActions;

public class UnlockReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getBundleExtra(Constants.EXTRA_BUNDLE);
		if (bundle != null) {
			Intent sendIntent = new Intent();
			sendIntent.setAction(GlobalActions.ACTION_PREFIX + "UnlockSetForced");
			sendIntent.putExtras(bundle);
			context.sendBroadcast(sendIntent);
		}
	}

}
