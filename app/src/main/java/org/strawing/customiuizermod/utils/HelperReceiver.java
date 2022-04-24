package org.strawing.customiuizermod.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HelperReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context ctx, Intent intent) {
		if (intent.getAction() == null) return;
		
		if (intent.getAction().equals("org.strawing.customiuizermod.SAVEEXCEPTION")) {
			try {
				Throwable thw = (Throwable)intent.getSerializableExtra("throwable");
				if (thw == null) return;
				StringWriter stackTrace = new StringWriter();

				File f = new File(ctx.getFilesDir().getAbsolutePath() + "/uncaught_exceptions");
				if (!f.exists()) f.createNewFile();
				
				try (FileOutputStream fOut = new FileOutputStream(f, true)) {
					try (OutputStreamWriter output = new OutputStreamWriter(fOut)) {
						output.write(stackTrace + "\n\n");
					}
				}
			} catch (Throwable t) {}
		} else if (intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
			Helpers.fixPermissionsAsync(ctx);
		}
	}
}
