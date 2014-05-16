package com.brewcrewfoo.performance.fragments;

/**
 * Created by h0rn3t on 02.01.2014.
 * http://forum.xda-developers.com/member.php?u=4674443
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.brewcrewfoo.performance.R;
import com.brewcrewfoo.performance.activities.ParamActivity;
import com.brewcrewfoo.performance.activities.PCSettings;
import com.brewcrewfoo.performance.util.CMDProcessor;
import com.brewcrewfoo.performance.util.Constants;
import com.brewcrewfoo.performance.util.GPUClass;
import com.brewcrewfoo.performance.util.Helpers;

import java.io.File;

public class CPUAdvanced extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,Constants {

    SharedPreferences mPreferences;
    private CheckBoxPreference mMpdecision,mIntelliplug,mEcomode,mGenHP,mKraitBoost;
    private Preference mHotplugset,mGpuGovset,mKraitHi,mKraitLo;
    private ListPreference mSOmax,mSOmin,lmcps,lcpuq,lgpufmax,mKraitThres;
    private Context context;
    private String hotpath=Helpers.hotplug_path();
    private final CharSequence[] vmcps={"0","1","2"};
    private GPUClass gpu;
    private String ps="";
    private String ps_cpuquiet="";
    private String app="";
    private String ps_mc_ps="";
    private final int vstep=12500;
    private final int vmin=0;
    private final int nvsteps=25;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
        setHasOptionsMenu(true);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.layout.cpu_advanced);

        final String availableFrequencies = Helpers.readOneLine(STEPS_PATH);

        mSOmax = (ListPreference) findPreference("pref_so_max");
        mSOmin = (ListPreference) findPreference("pref_so_min");

        mMpdecision = (CheckBoxPreference) findPreference("pref_mpdecision");
        mIntelliplug = (CheckBoxPreference) findPreference("pref_intelliplug");
        mEcomode = (CheckBoxPreference) findPreference("pref_ecomode");

        lmcps = (ListPreference) findPreference("pref_mcps");
        lcpuq = (ListPreference) findPreference("pref_cpuquiet");
        mHotplugset = findPreference("pref_hotplug");
        mGenHP = (CheckBoxPreference) findPreference("pref_hp");
        lgpufmax = (ListPreference) findPreference("pref_gpu_fmax");
        mGpuGovset = findPreference("pref_gpugov_set");

        mKraitThres = (ListPreference) findPreference("pref_krait_thres");
        mKraitBoost = (CheckBoxPreference) findPreference("pref_krait_boost");
        mKraitHi = findPreference("pref_krait_hi");
        mKraitLo = findPreference("pref_krait_lo");

        ps=getString(R.string.ps_so_minmax);
        ps_cpuquiet=getString(R.string.ps_cpuquiet);
        ps_mc_ps=getString(R.string.ps_mc_ps);
        app=getString(R.string.app_name);


        if (!new File(SO_MAX_FREQ).exists() || !new File(SO_MIN_FREQ).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("so_min_max");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            if (availableFrequencies != null) {
                CharSequence[] entries = availableFrequencies.split(" ");
                mSOmax.setEntries(entries);
                mSOmax.setEntryValues(entries);
                mSOmin.setEntries(entries);
                mSOmin.setEntryValues(entries);
                final String readsomax=Helpers.readOneLine(SO_MAX_FREQ);
                final String readsomin=Helpers.readOneLine(SO_MIN_FREQ);
                mSOmax.setValue(readsomax);
                mSOmin.setValue(readsomin);
                mSOmax.setSummary(ps+readsomax+" kHz");
                mSOmin.setSummary(ps+readsomin+" kHz");
            }
        }

        if (Helpers.binExist("mpdecision")==null){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("mpdecision");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            Boolean mpdon = Helpers.moduleActive("mpdecision");
            mMpdecision.setChecked(mpdon);
            mPreferences.edit().putBoolean("mpdecision",mpdon).apply();
        }
        if (!new File(INTELLI_PLUG).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("intelliplug");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mIntelliplug.setChecked(Helpers.readOneLine(INTELLI_PLUG).equals("1"));
        }
        if(hotpath==null){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("hotplugset");
            getPreferenceScreen().removePreference(hideCat);
        }
        if (!new File(ECO_MODE).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("ecomode");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mEcomode.setChecked(Helpers.readOneLine(ECO_MODE).equals("1"));
        }
        if (!new File(MC_PS).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("mc_ps");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            lmcps.setEntries(getResources().getStringArray(R.array.mc_ps_array));
            lmcps.setEntryValues(vmcps);
            lmcps.setValue(Helpers.readOneLine(MC_PS));
            lmcps.setSummary(ps_mc_ps+lmcps.getEntry().toString());
        }
        if(!new File(GEN_HP).exists()){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("hp");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            mGenHP.setChecked(Helpers.readOneLine(GEN_HP).equals("1"));
            mGenHP.setSummary(getString(R.string.ps_hp)+" | "+getString(R.string.ps_dsync));
        }
        if(!new File(CPU_QUIET_GOV).exists()){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("cpuq");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            final String cur_cpuq_gov=Helpers.readOneLine(CPU_QUIET_GOV);
            final String s=Helpers.readOneLine(CPU_QUIET_CUR);
            final CharSequence[] govs=cur_cpuq_gov.split(" ");
            lcpuq.setEntries(govs);
            lcpuq.setEntryValues(govs);
            lcpuq.setValue(s);
            lcpuq.setSummary(ps_cpuquiet+s);
        }
        gpu = new GPUClass();
        if(gpu.gpuclk_path()==null){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("gpu_max_clk");
            getPreferenceScreen().removePreference(hideCat);
        }
        else{
            lgpufmax.setEntries(gpu.gpuclk_names());
            lgpufmax.setEntryValues(gpu.gpuclk_values());
            final String s=Helpers.readOneLine(gpu.gpuclk_path());
            lgpufmax.setValue(s);
            //lgpufmax.setSummary(ps+Helpers.toMHz(String.valueOf(Integer.parseInt(s) / 1000)));
            lgpufmax.setSummary(ps+lgpufmax.getEntry());
        }

        if(gpu.gpugovset_path()==null){
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("gpugovset");
            getPreferenceScreen().removePreference(hideCat);
        }

        PreferenceCategory Cat = (PreferenceCategory) findPreference("uv_krait");
        if(!new File(KRAIT_ON_PATH).exists()){
            getPreferenceScreen().removePreference(Cat);
        }
        else {

            mKraitBoost.setChecked(Helpers.readOneLine(KRAIT_ON_PATH).equalsIgnoreCase("N"));

            if(!new File(KRAIT_THRES_PATH).exists()){
                Cat.removePreference(mKraitThres);
            }
            else{
                if (availableFrequencies != null) {
                    CharSequence[] entries = availableFrequencies.split(" ");
                    mKraitThres.setEntries(entries);
                    mKraitThres.setEntryValues(entries);
                    final String readthres =Helpers.readOneLine(KRAIT_THRES_PATH);
                    mKraitThres.setValue(readthres);
                    mKraitThres.setSummary(ps+readthres+" kHz");
                }
            }
            if(!new File(KRAIT_HIGH_PATH).exists()){
                Cat.removePreference(mKraitHi);
            }
            else{
                final String s=Helpers.readOneLine(KRAIT_HIGH_PATH);
                mKraitHi.setSummary(getString(R.string.ps_krait_hi,s));
                mPreferences.edit().putString("pref_krait_hi",s).commit();
            }
            if(!new File(KRAIT_LOWER_PATH).exists()){
                Cat.removePreference(mKraitLo);
            }
            else{
                final String s=Helpers.readOneLine(KRAIT_LOWER_PATH);
                mKraitLo.setSummary(getString(R.string.ps_krait_lo,s));
                mPreferences.edit().putString("pref_krait_lo",s).commit();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.tablist:
                Helpers.getTabList(getString(R.string.menu_tab),(ViewPager) getView().getParent(),getActivity());
                break;
            case R.id.app_settings:
                Intent intent = new Intent(getActivity(), PCSettings.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference==mMpdecision) {
            if(mMpdecision.isChecked()){
                final StringBuilder sb = new StringBuilder();
                sb.append("mpdecisionstart;\n");
                Helpers.shExec(sb,context,true);
            }
            else{
                new CMDProcessor().su.runWaitFor("stop mpdecision");
            }
            return true;
        }
        else if(preference==mHotplugset) {
            Intent i = new Intent(getActivity(), ParamActivity.class);
            i.putExtra("path",hotpath);
            i.putExtra("sob",HOTPLUG_SOB);
            i.putExtra("pref","hotplug");
            startActivity(i);
        }
        else if(preference==mIntelliplug) {
            if (Helpers.readOneLine(INTELLI_PLUG).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + INTELLI_PLUG);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + INTELLI_PLUG);
            }
            return true;
        }
        else if(preference==mEcomode) {
            if (Helpers.readOneLine(ECO_MODE).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + ECO_MODE);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + ECO_MODE);
            }
            return true;
        }
        else if(preference==mGenHP) {
            if (Helpers.readOneLine(GEN_HP).equals("0")){
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + ECO_MODE);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + ECO_MODE);
            }
            return true;
        }
        else if(preference==mGpuGovset) {
            Intent i = new Intent(getActivity(), ParamActivity.class);
            i.putExtra("path",gpu.gpugovset_path());
            i.putExtra("sob",GPU_PARAM_SOB);
            i.putExtra("pref","gpuparam");
            startActivity(i);
        }
        else if(preference==mKraitBoost) {
            if (Helpers.readOneLine(KRAIT_ON_PATH).equalsIgnoreCase("N")){
                new CMDProcessor().su.runWaitFor("busybox echo y > " + KRAIT_ON_PATH);
            }
            else{
                new CMDProcessor().su.runWaitFor("busybox echo n > " + KRAIT_ON_PATH);
            }
            return true;
        }
        else if (preference == mKraitHi) {
            final int currentProgress =Integer.parseInt(Helpers.readOneLine(KRAIT_HIGH_PATH));
            showDialog(currentProgress, vmin, vstep, nvsteps, getString(R.string.pt_krait_hi), "pref_krait_hi");
            return true;
        }
        else if (preference == mKraitLo) {
            final int currentProgress =Integer.parseInt(Helpers.readOneLine(KRAIT_LOWER_PATH));
            showDialog(currentProgress, vmin, vstep, nvsteps, getString(R.string.pt_krait_lo), "pref_krait_lo");
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_so_max")) {
            final String values = mSOmax.getValue();
            if (!values.equals(Helpers.readOneLine(SO_MAX_FREQ))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + SO_MAX_FREQ);
            }
            mSOmax.setSummary(ps+values+" kHz");
        }
        else if (key.equals("pref_so_min")) {
            final String values = mSOmin.getValue();
            if (!values.equals(Helpers.readOneLine(SO_MIN_FREQ))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + SO_MIN_FREQ);
            }
            mSOmin.setSummary(ps+values+" kHz");
        }
        else if(key.equals("pref_mcps")){
            final String values = lmcps.getValue();
            if (!values.equals(Helpers.readOneLine(MC_PS))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + MC_PS);
            }
            lmcps.setSummary(ps_mc_ps+lmcps.getEntry());
        }
        else if(key.equals("pref_cpuquiet")){
            final String values = lcpuq.getValue();
            if (!values.equals(Helpers.readOneLine(CPU_QUIET_CUR))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + CPU_QUIET_CUR);
            }
            lcpuq.setSummary(ps_cpuquiet+values);
        }
        else if(key.equals("pref_gpu_fmax")){
            final String values = lgpufmax.getValue();
            if (!values.equals(Helpers.readOneLine(gpu.gpuclk_path()))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + gpu.gpuclk_path());
            }
            //lgpufmax.setSummary(ps+Helpers.toMHz(String.valueOf(Integer.parseInt(values) / 1000)));
            lgpufmax.setSummary(ps+lgpufmax.getEntry());

            Intent intent = new Intent(INTENT_PP);
            intent.putExtra("from",app);
            context.sendBroadcast(intent);
        }
        else if(key.equals("pref_krait_thres")){
            final String values = mKraitThres.getValue();
            if (!values.equals(Helpers.readOneLine(KRAIT_THRES_PATH))){
                new CMDProcessor().su.runWaitFor("busybox echo "+values+" > " + KRAIT_THRES_PATH);
            }
            mKraitThres.setSummary(ps+values+" kHz");
        }

    }

    private static int getNearestStepIndex(final int value,final int min,final int step,final int total) {
        int index = 0;
        for (int k=0;k<total;k++) {
            if (value > (k*step+min)) index++;
            else break;
        }
        return index;
    }

    protected void showDialog(final int curval,final int min,final int step,final int total,final String titlu,final String key) {
        final LayoutInflater factory = LayoutInflater.from(context);
        final View voltageDialog = factory.inflate(R.layout.voltage_dialog,null);

        final SeekBar voltageSeek = (SeekBar) voltageDialog.findViewById(R.id.voltageSeek);
        final TextView voltageMeter = (TextView) voltageDialog.findViewById(R.id.voltageMeter);

        voltageMeter.setText(String.valueOf(curval) + " μV");
        voltageSeek.setMax(total);
        voltageSeek.setProgress(getNearestStepIndex(curval,vmin,vstep,nvsteps));
        voltageSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress,boolean fromUser) {
                if (fromUser) {
                    final String volt = Integer.toString(progress*step+min);
                    voltageMeter.setText(volt + " μV");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        new AlertDialog.Builder(context)
                .setTitle(titlu)
                .setView(voltageDialog)
                .setPositiveButton(getResources().getString(R.string.ps_volt_save),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                                final String value = Integer.toString(voltageSeek.getProgress() * step + min);
                                SharedPreferences.Editor editor = mPreferences.edit();
                                editor.putString(key, value).commit();
                                if(key.equals("pref_krait_hi")){
                                    mKraitHi.setSummary(getString(R.string.ps_krait_hi, value));
                                    new CMDProcessor().su.runWaitFor("busybox echo "+value+" > " + KRAIT_HIGH_PATH);
                                }
                                else if(key.equals("pref_krait_lo")){
                                    mKraitLo.setSummary(getString(R.string.ps_krait_lo, value));
                                    new CMDProcessor().su.runWaitFor("busybox echo "+value+" > " + KRAIT_LOWER_PATH);
                                }
                            }
                        }
                )
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }
                ).create().show();

    }
}

