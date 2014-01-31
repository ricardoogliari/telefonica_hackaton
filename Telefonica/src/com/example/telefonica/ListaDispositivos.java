package com.example.telefonica;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.example.telefonica.R;

public class ListaDispositivos extends Activity implements OnItemClickListener {

	private String id;

	private Button btnTemperatura;
	private Button btnHumidade;
	private Button btnIniciaGeladeira;
	
	private EditText tempMax;
	private EditText tempMin;
	private EditText humMax;
	private EditText humMin;
	private EditText gelIni;
	private EditText gelFim;
	
	private ListView listView;
	
	private SharedPreferences preference;
	private SharedPreferences.Editor editor;
	
	public static String CHAVE_TEMP_MAX = "tempMaxima";
	public static String CHAVE_TEMP_MIN = "tempMinima";
	public static String CHAVE_HUM_MAX = "humMaxima";
	public static String CHAVE_HUM_MIN = "humMinima";
	public static String CHAVE_GEL_INICIO = "gelIni";
	public static String CHAVE_GEL_FIM = "gelFim";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lista_dispositivos);

		id = getIntent().getStringExtra("id");

		listView = (ListView) findViewById(R.id.listView);
		 
		btnTemperatura = (Button) findViewById(R.id.btnTemperatura);
		btnHumidade = (Button) findViewById(R.id.btnHumidade);
		btnIniciaGeladeira = (Button) findViewById(R.id.btnControleGeladeira);
		
		tempMax = (EditText) findViewById(R.id.edtTempMaxima);
		tempMin = (EditText) findViewById(R.id.edtTempMinima);
		humMax = (EditText) findViewById(R.id.edtHumidadeMaxima);
		humMin = (EditText) findViewById(R.id.edtHumidadeMinima);
		gelIni = (EditText) findViewById(R.id.edtInicioGel);
		gelFim = (EditText) findViewById(R.id.edtFinalGel);
		
		preference = getSharedPreferences("telefonica", MODE_PRIVATE);
		editor = preference.edit();
		
		if (!preference.contains("INICIOU_SERVICE")){
			//iniciar
			Intent i = new Intent(this, Controlador.class);
			startService(i);
			editor.putBoolean("INICIOU_SERVICE", true);
			editor.commit();
		}
		
		estadoCampos();

		new BuscaDispositivos().execute();
	}
	
	public void estadoCampos(){
		tempMax.setEnabled(!preference.contains(CHAVE_TEMP_MAX));
		tempMin.setEnabled(!preference.contains(CHAVE_TEMP_MIN));
		humMax.setEnabled(!preference.contains(CHAVE_HUM_MAX));
		humMin.setEnabled(!preference.contains(CHAVE_HUM_MIN));
		gelIni.setEnabled(!preference.contains(CHAVE_GEL_INICIO));
		gelFim.setEnabled(!preference.contains(CHAVE_GEL_FIM));
		
		btnTemperatura.setText(preference.contains(CHAVE_TEMP_MAX)?"Parar":"Iniciar");
		btnHumidade.setText(preference.contains(CHAVE_HUM_MAX)?"Parar":"Iniciar");
		btnIniciaGeladeira.setText(preference.contains(CHAVE_GEL_INICIO)?"Parar":"Iniciar");
	}

	class BuscaDispositivos extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			AQuery aq = new AQuery(ListaDispositivos.this);
			aq.ajax("http://dca.telefonicabeta.com/m2m/v2/services/" + id
					+ "/assets", String.class, new AjaxCallback<String>() {
				@Override
				public void callback(String url, String object,
						AjaxStatus status) {
					try {
						JSONObject root = new JSONObject(object);
						int count = root.getInt("count");
						JSONArray data = root.getJSONArray("data");

						String[] nomes = new String[count];

						for (int i = 0; i < data.length(); i++) {
							JSONObject obj = data.getJSONObject(i);
							JSONObject asset = obj.getJSONObject("asset");
							String nome = asset.getString("name");
							nomes[i] = nome;
						}

						listView.setAdapter(new ArrayAdapter<String>(
								ListaDispositivos.this,
								android.R.layout.simple_list_item_1, nomes));
						listView.setOnItemClickListener(ListaDispositivos.this);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			return null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		// TODO Auto-generated method stub
		Object o = listView.getItemAtPosition(pos);
		String str = (String) o;// As you are using Default String Adapter

		Intent i = new Intent(ListaDispositivos.this, DetalhesDispositivo.class);
		i.putExtra("id", id);
		startActivity(i);
	}

	public void iniciaControleGeladeira(View v) {
		if (btnIniciaGeladeira.getText().toString().equals("Parar")){
			editor.remove(CHAVE_GEL_FIM);
			editor.remove(CHAVE_GEL_INICIO);
		} else {
			editor.putString(CHAVE_GEL_FIM, gelFim.getText().toString());
			editor.putString(CHAVE_GEL_INICIO, gelIni.getText().toString());
		}
		
		editor.commit();
		
		estadoCampos();
	}

	public void iniciaTemperatura(View v) {
		if (btnTemperatura.getText().toString().equals("Parar")){
			editor.remove(CHAVE_TEMP_MAX);
			editor.remove(CHAVE_TEMP_MIN);
		} else {
			editor.putString(CHAVE_TEMP_MAX, tempMax.getText().toString());
			editor.putString(CHAVE_TEMP_MIN, tempMin.getText().toString());
		}
		
		editor.commit();
		
		estadoCampos();
	}

	public void iniciaHumidade(View v) {
		if (btnHumidade.getText().toString().equals("Parar")){
			editor.remove(CHAVE_HUM_MAX);
			editor.remove(CHAVE_HUM_MIN);
		} else {
			editor.putString(CHAVE_HUM_MAX, humMax.getText().toString());
			editor.putString(CHAVE_HUM_MIN, humMin.getText().toString());
		}
		
		editor.commit();
		
		estadoCampos();
	}

}
