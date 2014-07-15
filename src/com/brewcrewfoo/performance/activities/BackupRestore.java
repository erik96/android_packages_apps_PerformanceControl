package com.brewcrewfoo.performance.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.brewcrewfoo.performance.R;
import com.brewcrewfoo.performance.util.ActivityThemeChangeInterface;
import com.brewcrewfoo.performance.util.Constants;
import com.brewcrewfoo.performance.util.FileArrayAdapter;
import com.brewcrewfoo.performance.util.Helpers;
import com.brewcrewfoo.performance.util.Item;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by H0RN3T on 15.07.2014.
 */
public class BackupRestore extends ListActivity implements Constants, ActivityThemeChangeInterface {
    final Context context = this;
    private File currentDir;
    SharedPreferences mPreferences;
    private boolean mIsLightTheme;
    private FileArrayAdapter adapter;
    private ProgressDialog progressDialog;
    private String nFile;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();

        currentDir = new File(mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath()));
        fill(currentDir);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filechooser_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.close) {
            finish();
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fill(currentDir);
    }
    @Override
    public boolean isThemeChanged() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        return is_light_theme != mIsLightTheme;
    }

    @Override
    public void setTheme() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        mIsLightTheme = is_light_theme;
        setTheme(is_light_theme ? R.style.Theme_Light : R.style.Theme_Dark);
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    private void fill(File f){
        File[]dirs = f.listFiles();
        List<Item> dir = new ArrayList<Item>();
        List<Item>fls = new ArrayList<Item>();
        try{
            assert dirs != null;
            for(File ff: dirs){
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if(ff.isDirectory()){
                    dir.add(new Item(ff.getName(),getString(R.string.dir),date_modify,ff.getAbsolutePath(),"dir"));
                }
                else{
                    fls.add(new Item(ff.getName(), Helpers.ReadableByteCount(ff.length()), date_modify, ff.getAbsolutePath(),"file"));
                }
            }
        }
        catch(Exception e){
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if(!f.getName().equalsIgnoreCase(""))
            dir.add(0,new Item("..",getString(R.string.dir_parent),"",f.getParent(),"dir"));
        adapter = new FileArrayAdapter(this,R.layout.file_view, dir);
        this.setListAdapter(adapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Item o = adapter.getItem(position);
        if(o.getImage().equalsIgnoreCase("dir")){
            currentDir = new File(o.getPath());
            fill(currentDir);
        }
        else{
            nFile=currentDir+"/"+o.getName();
            makedialog();
        }
    }

    private class FlashOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            final StringBuilder sb = new StringBuilder();
            final String dn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/tmp";
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null) progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(BackupRestore.this, null, getString(R.string.wait));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        public CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            dialog.cancel();
            new FlashOperation().execute();
        }
    }

    private void makedialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(null)
                //.setMessage(nFile+" "+getString(R.string.flash_info,part)+" "+tip.toUpperCase()+"\n\n"+getString(R.string.wipe_cache_msg))
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                //finish();
                            }
                        })
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        //alertDialog.setCancelable(false);
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (theButton != null) {
            theButton.setOnClickListener(new CustomListener(alertDialog));
        }
    }

}
