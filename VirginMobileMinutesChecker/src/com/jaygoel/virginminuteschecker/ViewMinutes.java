package com.jaygoel.virginminuteschecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
import java.util.Map;

public class ViewMinutes extends Activity implements Runnable
{

    String PREFS_NAME = "loginInfo";
    ProgressDialog pd;
    Map<String, String> rc = null;
    // private TextView tv;
    Activity me = this;

	Map<String, String> min = null;
	//********
	TableLayout tableLayout1;
	TableLayout tableLayout2;
	ProgressBar pb_minutes;
	TextView min_value;
	TableLayout minutes;
	//********

	String username, password;
	
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_minutes);
        setTitle(getString(R.string.viewTitle));

        // tv = (TextView) this.findViewById(R.id.minutes);
        setLoginInfo();

        if (username.equals("u") || password.equals("p"))
        {
            Intent i = new Intent(this, MinutesChecker.class);
            startActivityForResult(i, 1);
            // startActivity(i);
        }
        else
        {
            gatherAndDisplay();
        }

    }

    private void setLoginInfo()
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        username = settings.getString("username", "u");
        password = settings.getString("password", "p");
    }

    @Override
    protected void onActivityResult(final int reqCode, final int resultCode, final Intent intent)
    {
        Log.d("DEBUG", "in onActivityResult");
        if (reqCode == 1 && resultCode != RESULT_CANCELED)
        {
            Log.d("DEBUG", "login activity succeeded, continuing");

            setLoginInfo();
            gatherAndDisplay();
        }
        else
        {
            Log.d("DEBUG", "login activity failed, repeating");
            Log.d("DEBUG", Integer.toString(reqCode));
            Log.d("DEBUG", Integer.toString(resultCode));

            showErrorMessageAndRequery();
        }
    }

    private void gatherAndDisplay()
    {
        pd = new ProgressDialog(ViewMinutes.this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage(getString(R.string.loadingMessage));
        pd.setIndeterminate(true);
        pd.setCancelable(false);

        doInfo();
    }

    private void doInfo()
    {
        pd.show();

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.logout:
    	    	clearDisplay();
                TableLayout tl = (TableLayout) findViewById(R.id.minutes);
                tl.removeAllViews();

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.clear();

                // Commit the edits!
                editor.commit();

                SharedPreferences cache = getSharedPreferences("cache", 0);
                SharedPreferences.Editor ceditor = cache.edit();
                ceditor.clear();
                ceditor.commit();

                Intent i = new Intent(this, MinutesChecker.class);
                startActivityForResult(i, 1);
                return true;
            case R.id.refresh:
    	    	clearDisplay();
                doInfo();
                return true;
                // case R.id.settings:
                // Intent i2 = new Intent(this, Preferences.class);
                // startActivity(i2);
                // return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void run()
    {
        rc = WebsiteScraper.getInfo(username, password);
        handler.sendEmptyMessage(0);
    }

//<<<<<<< HEAD
//	private void doInfo() {
//		pd.show();
//
//		Thread t = new Thread(this);
//		t.start();
//	}

	private void clearDisplay() {
    	TableLayout t1 = (TableLayout) findViewById(R.id.minutes);
    	t1.removeAllViews();
    	ProgressBar mProgress = (ProgressBar) findViewById(R.id.pb_minutes);
    	mProgress.setProgress(0);
    	mProgress.setVisibility(ProgressBar.INVISIBLE);
    	TextView minval = (TextView) findViewById(R.id.min_value);
        minval.setText("");
       	
	}

    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(final Message msg)
        {
            pd.dismiss();
            if (rc.get("isValid").equals("TRUE"))
            {

                // cache minutes used
                SharedPreferences cache = getSharedPreferences("cache", 0);
                SharedPreferences.Editor ceditor = cache.edit();
                ceditor.putString("minutes", rc.get("Minutes Used"));
                ceditor.commit();

			    TableLayout tbl = (TableLayout) findViewById(R.id.tableLayout1);
			    tbl.setVisibility(1);
                TableLayout tl = (TableLayout) findViewById(R.id.minutes);
                tl.removeAllViews();

                int current = 0;
                for (Map.Entry<String, String> entry : rc.entrySet())
                {

                    if (entry.getKey().equals("isValid"))
                    {
                        continue;
                    }

                    current++;

                    TableRow tr = new TableRow(me);
                    tr.setId(100 + current);
                    tr.setLayoutParams(new LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

                    // Create a TextView to show the name of the property
                    TextView labelTV = new TextView(me);
                    labelTV.setId(200 + current);
                    labelTV.setText(entry.getKey());
                    labelTV.setTextColor(Color.LTGRAY);
                    labelTV.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7);
                    labelTV.setLayoutParams(new LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                    tr.addView(labelTV);

                    // Create a TextView to show that property's value
                    TextView valueTV = new TextView(me);
                    valueTV.setId(current);
                    valueTV.setText(entry.getValue());
                    valueTV.setTextColor(Color.WHITE);
                    valueTV.setTextSize(TypedValue.COMPLEX_UNIT_PT, 9);
                    valueTV.setLayoutParams(new LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                    tr.addView(valueTV);

			        // Create the Progress Bar
                    if (entry.getKey().equals("Minutes Used")){
			        	TableLayout pb_tbl = (TableLayout) findViewById(R.id.tableLayout2);
				        
			         	String m = entry.getValue().replaceAll(" ", "");
			           	String[] minute_str = m.split("/");
			           	int cur_min = Integer.parseInt(minute_str[0]);
			           	int total_min = Integer.parseInt(minute_str[1]);
			            	
			           	ProgressBar mProgress = (ProgressBar) findViewById(R.id.pb_minutes);
				        mProgress.setVisibility(1);
			          	mProgress.setMax(total_min);
				        mProgress.setProgress(cur_min);

				        TextView minval = (TextView) findViewById(R.id.min_value);
				        String rem_min = Integer.toString(total_min - cur_min);
			         	minval.setText(rem_min + " Minutes Remaining");
			           	minval.setTextColor(Color.WHITE);
			           	minval.setTextSize(TypedValue.COMPLEX_UNIT_PT ,5);	
			           	minval.setLayoutParams(new LayoutParams(
				               LayoutParams.FILL_PARENT,
				               LayoutParams.WRAP_CONTENT));
			            	
			            }
			            
				        //if (entry.getKey().equals("Charge Deducted")){
				        //	String due_date = entry.getValue();
				        //	Calendar cal = Calendar.getInstance();
				        //	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
				        //	String d = dateFormat.format(cal.getTime());
				        //	String days_left = d.add(Calendar.DATE,due_date);
				        //}
				        //String m = entry.getValue();
			         
			            //TextView m_view = new TextView(me);
			            //m_view.setText((CharSequence) min);
			            //tr.addView(m_view);
				        
			        	tl.addView(tr);
	                    // Add the TableRow to the TableLayout
	                    //tl.addView(tr, new TableLayout.LayoutParams(
	                    //    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			            
                }

                // tv.setText(rc.get("info"));
            }
            else
            {
                showErrorMessageAndRequery();
            }
        }
    };

    private void showErrorMessageAndRequery()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(me);
        builder.setMessage(getString(R.string.loginFail)).setCancelable(false)
            .setNeutralButton("Ok.", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    Intent i = new Intent(me, MinutesChecker.class);
                    startActivityForResult(i, 1);
                    // startActivity(i);

                }
            });

        AlertDialog alert = builder.create();

        alert.show();

    }
    

}
