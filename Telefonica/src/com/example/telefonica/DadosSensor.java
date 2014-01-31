package com.example.telefonica;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.example.telefonica.R;

public class DadosSensor extends Activity {

	private ProgressDialog dialogo;
	
	public String id, sensor;
	
	private String valores = "";
	
	private WebView wvGrafico;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dados_sensor);
		
		id = getIntent().getStringExtra("id");
		sensor = getIntent().getStringExtra("sensor");
		
		wvGrafico = (WebView) findViewById(R.id.wvGrafico);
		
		dialogo = ProgressDialog.show(DadosSensor.this, "Aguarde", "Aguarde enquanto os dados são recuperados!");
		
		AQuery aq = new AQuery(DadosSensor.this);
		Log.e("TELEFONICA", "http://dca.telefonicabeta.com/m2m/v2/services/"+id+"/assets/"+id+"/data/?limit=10&offset=0&sortBy=!samplingTime&attribute="+sensor);
		aq.ajax("http://dca.telefonicabeta.com/m2m/v2/services/"+id+"/assets/"+id+"/data/?limit=10&offset=0&sortBy=!samplingTime&attribute="+sensor, String.class, new AjaxCallback<String>(){
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				try {
					JSONObject root = new JSONObject(object);
					JSONArray data = root.getJSONArray("data");
					
					for (int i = 0; i < data.length(); i++){
						JSONObject obj = data.getJSONObject(i);
						JSONObject ms = obj.getJSONObject("ms");
						int v = ms.getInt("v");
						if (i > 0){
							valores += ","+v;
						} else {
							valores += ""+v;
						}
					} 
					
					dialogo.dismiss();
					String strURL = "https://chart.googleapis.com/chart?" + 
				            "cht=lc&" + //define o tipo do gráfico "linha"
				            "chxt=x,y&" + //imprime os valores dos eixos X, Y
				            "chs="+360+"x"+360+"&" + //define o tamanho da imagem
				            "chd=t:"+valores+"&" + //valor de cada coluna do gráfico
				            "chdl=Valores&" + //legenda do gráfico
				            "chxr=1,0,1024&" + //define o valor de início e fim do eixo 
				            "chds=0,1024&" + //define o valor de escala dos dados 
				            "chg=0,5,0,0&" + //desenha linha horizontal na grade
				            "chco=3D7930&" + //cor da linha do gráfico 
				            "chtt=Dados+do+Sensor&" + //cabeçalho do gráfico
				            "chm=B,C5D4B5BB,0,0,0"; //fundo verde 
				 
					wvGrafico = (WebView)findViewById(R.id.wvGrafico);
			        wvGrafico.loadUrl(strURL);
				 
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

	}
	


}
