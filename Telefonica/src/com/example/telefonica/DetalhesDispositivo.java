package com.example.telefonica;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.example.telefonica.R;

public class DetalhesDispositivo extends ListActivity {

	private String id;
	
	private String[] nomes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detalhes_dispositivos);
		
		id = getIntent().getStringExtra("id");
		
		new ListaDispositivos().execute();
	}
	
	class ListaDispositivos extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			AQuery aq = new AQuery(DetalhesDispositivo.this);
			aq.ajax("http://dca.telefonicabeta.com/m2m/v2/services/"+id+"/assets/"+id, String.class, new AjaxCallback<String>(){
				@Override
				public void callback(String url, String object, AjaxStatus status) {
					try {
						JSONObject root = new JSONObject(object);
						JSONObject data = root.getJSONObject("data");
						JSONArray sensorData = data.getJSONArray("sensorData");
						
						nomes = new String[sensorData.length()];
						
						for (int i = 0; i < sensorData.length(); i++){
							JSONObject obj = sensorData.getJSONObject(i);
							JSONObject ms = obj.getJSONObject("ms");
							String p = ms.getString("p");
							nomes[i] = p;
						}
						
						setListAdapter(new ArrayAdapter<String>(
								DetalhesDispositivo.this, android.R.layout.simple_list_item_1,
								nomes));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			return null;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long ONGiD) {
	    Intent i = new Intent(DetalhesDispositivo.this, DadosSensor.class);
        i.putExtra("sensor", nomes[position]);
        i.putExtra("id", id);
        
        startActivity(i);
	}

}
