package name.starnberger.guenther.android.smsticket;

import name.starnberger.guenther.android.smsticket.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Ticket extends Activity implements View.OnClickListener {

	public static final String PREFS_NAME = "OebbTicketPrefs";
	public static final String DEFAULT_USER = "Max Mustermann";
	public static final int MAX_SMS_LENGTH = 400;
	AutoCompleteTextView abfahrtsbahnhof;
	AutoCompleteTextView zielbahnhof;
	EditText name;
	Button button;
	Button swap;
	Spinner erwachsene;
	Spinner kinder;
	Spinner klasse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		abfahrtsbahnhof = (AutoCompleteTextView) findViewById(R.id.abfahrtsbahnhof);
		zielbahnhof = (AutoCompleteTextView) findViewById(R.id.zielbahnhof);
		name = (EditText) findViewById(R.id.name);
		erwachsene = (Spinner) findViewById(R.id.erwachsene);
		kinder = (Spinner) findViewById(R.id.kinder);
		klasse = (Spinner) findViewById(R.id.klasse);

		SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, 0);
		abfahrtsbahnhof.setText(mPrefs.getString("abfahrtsbahnhof",
				"Wien Südbahnhof"));
		zielbahnhof.setText(mPrefs.getString("zielbahnhof", "Langenzersdorf"));
		name.setText(mPrefs.getString("name", DEFAULT_USER));
		erwachsene.setSelection(mPrefs.getInt("erwachsene", 1));
		kinder.setSelection(mPrefs.getInt("kinder", 0));
		klasse.setSelection(mPrefs.getInt("klasse", 1));

		button = (Button) findViewById(R.id.send_button);
		button.setOnClickListener(this);

		// Two distinct Adapters are required to prevent AutoComplete from
		// sometimes completing the "other" field
		ArrayAdapter<String> abfahrtsAutoAdapter = new ArrayAdapter<String>(
				this, R.layout.list_item, Station.STATIONS);
		abfahrtsbahnhof.setAdapter(abfahrtsAutoAdapter);
		ArrayAdapter<String> zielAutoAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, Station.STATIONS);
		zielbahnhof.setAdapter(zielAutoAdapter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putString("abfahrtsbahnhof", abfahrtsbahnhof.getText().toString());
		ed.putString("zielbahnhof", zielbahnhof.getText().toString());
		ed.putString("name", name.getText().toString());
		ed.putInt("erwachsene", erwachsene.getSelectedItemPosition());
		ed.putInt("kinder", kinder.getSelectedItemPosition());
		ed.putInt("klasse", klasse.getSelectedItemPosition());
		ed.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.swap:
			String tmp = abfahrtsbahnhof.getText().toString();
			abfahrtsbahnhof.setText(zielbahnhof.getText().toString());
			zielbahnhof.setText(tmp);
			// button.requestFocus();
			return true;
		case R.id.about:
			Intent i = new Intent(this, About.class);
			startActivity(i);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View view) {
		if (name.getText().toString().equals(DEFAULT_USER)) {
			showMsg("Bitte richtigen Namen eingeben!");
		} else if ((erwachsene.getSelectedItemPosition() == 0)
				&& (kinder.getSelectedItemPosition() == 0)) {
			showMsg("Anzahl der Tickets muss größer als Null sein!");
		} else if (view == button) {
			genSms();
		}
	}

	private void showMsg(String text) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	private void genSms() {
		String smsBody = "Zug*" + abfahrtsbahnhof.getText() + "*"
				+ zielbahnhof.getText() + "*" + name.getText() + "*"
				+ (String) erwachsene.getSelectedItem() + "Erw*"
				+ kinder.getSelectedItem() + "K*" + klasse.getSelectedItem()
				+ ".KL";

		if (smsBody.length() < MAX_SMS_LENGTH) {
			sendSms(smsBody, getString(R.string.dest_sms_number));
		} else {
			showMsg("SMS zu lang!");
		}
	}

	private void sendSms(String body, String number) {
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.putExtra("sms_body", body);
		sendIntent.putExtra("address", number);
		sendIntent.setType("vnd.android-dir/mms-sms");
		startActivity(sendIntent);
	}
}
