package com.example.mouseracer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;

import com.example.mouseracer.R;

import java.util.Locale;


public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        findViewById(R.id.tv1).setOnClickListener(this);
        findViewById(R.id.tv2).setOnClickListener(this);
        findViewById(R.id.tv3).setOnClickListener(this);
        findViewById(R.id.tv4).setOnClickListener(this);
        findViewById(R.id.tv1).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv1:
                startActivity(new Intent(this, MenuActivity.class));
                break;
            case R.id.tv2:
                startActivity(new Intent(this, GuideActivity.class));
                break;
            case R.id.tv3:
             //     startActivity(new Intent(this, BleActivity.class));
                startActivity(new Intent(this, ScanMainActivity.class));
                break;
            case R.id.tv4:
                dialogChoice();
                break;
            default:
                break;
        }
    }

    /**
     * 单选
     */
    private void dialogChoice() {
        final String items[] = {getResources().getString(R.string.app_chinase), getResources()
                .getString(R.string.app_english)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("单选");
        builder.setIcon(R.mipmap.lauch);
        builder.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        if (0 == which) {
//                            com.mouse.app.Constants.langae = "zh";
//                        } else if (1 == which) {
//                            com.mouse.app.Constants.langae = "en";
//                        }
                    }
                });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //   setLangue();
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switchLanguage(Constants.langae);
    }

    public void setLangue() {
        Locale locale = new Locale(Constants.langae);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        //回到应用的首页
        startActivity(new Intent(this, MainActivity.class));
    }

    //核心设置的代码
    protected void switchLanguage(String language) {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        switch (language) {
            case "zh":
                config.locale = Locale.CHINESE;
                resources.updateConfiguration(config, dm);
                break;
            case "en":
                config.locale = Locale.ENGLISH;
                resources.updateConfiguration(config, dm);
                break;
            default:
                config.locale = Locale.US;
                resources.updateConfiguration(config, dm);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure to quit?")
                    .setPositiveButton("sure", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setCancelable(false).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void startSetting(Context context, String packageName) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", packageName, null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", packageName);
        }
        context.startActivity(intent);
    }

}
