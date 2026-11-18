package com.kage.dataguard;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.View;
import android.widget.*;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView usageText, permissionText;
    private EditText budgetInput;
    private long budgetMb = 500;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        budgetMb = getPreferences(0).getLong("budget", 500);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 36, 32, 24);
        root.setBackgroundColor(0xFF101522);

        TextView title = text("KAGE DATA GUARD", 26, 0xFFFFFFFF);
        root.addView(title);
        root.addView(text("Mobile data monitor • Offline • No account", 14, 0xFF9EABC4));

        usageText = text("Today's mobile data: checking…", 20, 0xFFFFFFFF);
        LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(-1, -2);
        gap.topMargin = 28;
        root.addView(usageText, gap);

        permissionText = text("", 14, 0xFFFFC56E);
        root.addView(permissionText);

        root.addView(text("Daily budget (MB)", 16, 0xFFFFFFFF));
        budgetInput = new EditText(this);
        budgetInput.setSingleLine(true);
        budgetInput.setInputType(2);
        budgetInput.setText(String.valueOf(budgetMb));
        root.addView(budgetInput);

        root.addView(button("SAVE DAILY BUDGET", v -> saveBudget()));
        root.addView(button("GRANT USAGE ACCESS", v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))));
        root.addView(button("START MONITORING", v -> {
            if (!hasUsageAccess()) startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            else startService(new Intent(this, MonitorService.class));
            refresh();
        }));
        root.addView(button("STOP MONITORING", v -> stopService(new Intent(this, MonitorService.class))));
        root.addView(button("OPEN ANDROID DATA SETTINGS", v -> {
            try { startActivity(new Intent("android.settings.DATA_USAGE_SETTINGS")); }
            catch (Exception e) { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        }));
        root.addView(text("\nAndroid restricts ordinary apps from switching mobile data off directly. At your limit, this app alerts you; set Android's own data limit as a backup. Usage statistics may be delayed.", 13, 0xFFFFC56E));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
        refresh();
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); t.setPadding(0, 10, 0, 10); return t;
    }
    private Button button(String label, View.OnClickListener click) {
        Button b = new Button(this); b.setText(label); b.setOnClickListener(click); return b;
    }
    private void saveBudget() {
        try {
            long value = Long.parseLong(budgetInput.getText().toString().trim());
            if (value < 1 || value > 100000) throw new NumberFormatException();
            budgetMb = value; getPreferences(0).edit().putLong("budget", value).apply();
            Toast.makeText(this, "Daily budget saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Toast.makeText(this, "Enter a budget from 1 to 100000 MB", Toast.LENGTH_LONG).show(); }
    }
    private boolean hasUsageAccess() {
        AppOpsManager ops = (AppOpsManager)getSystemService(Context.APP_OPS_SERVICE);
        return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }
    static long readTodayBytes(Context c) {
        try {
            Calendar start = Calendar.getInstance(); start.set(Calendar.HOUR_OF_DAY,0); start.set(Calendar.MINUTE,0); start.set(Calendar.SECOND,0); start.set(Calendar.MILLISECOND,0);
            NetworkStatsManager nsm = (NetworkStatsManager)c.getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            long total = 0;
            NetworkStats stats = nsm.querySummary(ConnectivityManager.TYPE_MOBILE, null, start.getTimeInMillis(), System.currentTimeMillis());
            while (stats.hasNextBucket()) { stats.getNextBucket(bucket); total += bucket.getRxBytes() + bucket.getTxBytes(); }
            stats.close(); return total;
        } catch (Exception e) { return -1; }
    }
    private void refresh() {
        boolean allowed = hasUsageAccess();
        permissionText.setText("Usage Access: " + (allowed ? "GRANTED" : "REQUIRED"));
        long bytes = allowed ? readTodayBytes(this) : -1;
        usageText.setText(bytes < 0 ? "Today's mobile data: unavailable" :
            String.format(Locale.getDefault(), "Today: %.1f MB / %d MB", bytes / 1048576d, budgetMb));
    }
    @Override protected void onResume() { super.onResume(); if (usageText != null) refresh(); }
}
