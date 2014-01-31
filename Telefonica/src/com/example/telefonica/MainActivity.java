package com.example.telefonica;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.example.telefonica.R;

public class MainActivity extends Activity {

	private Button btnLogin;
	private EditText edtLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		edtLogin = (EditText) findViewById(R.id.edtLogin);

		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new Loga().execute();
			}
		});
	}

	ProgressDialog dialogo;

	class Loga extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			dialogo = ProgressDialog.show(MainActivity.this, "Aguarde",
					"Por favor aguarde, validando login!");
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			AQuery aq = new AQuery(MainActivity.this);
			aq.ajax("http://dca.telefonicabeta.com/m2m/v2/services/"
					+ edtLogin.getText() + "/", String.class,
					new AjaxCallback<String>() {
						@Override
						public void callback(String url, String object,
								AjaxStatus status) {
							verificaLogin(object);
						}
					});

			return null;
		}

	}

	public void verificaLogin(String object) {
		try {
			dialogo.dismiss();
			JSONObject root = new JSONObject(object);
			if (!root.has("exceptionId")) {
				Intent i = new Intent(MainActivity.this,
						ListaDispositivos.class);
				i.putExtra("id", edtLogin.getText().toString());
				startActivity(i);

			} else {
				Toast.makeText(MainActivity.this, "Token inválido",
						Toast.LENGTH_LONG).show();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
