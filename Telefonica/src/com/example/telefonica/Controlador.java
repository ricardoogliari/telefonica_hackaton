package com.example.telefonica;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.example.telefonica.R;

public class Controlador extends Service {

	SharedPreferences preference;
	SharedPreferences.Editor editor;

	byte contTemperaturaExcecao;
	byte contHumidadeExcecao;

	@Override
	public void onCreate() {
		super.onCreate();

		preference = getSharedPreferences("telefonica", MODE_PRIVATE);
		editor = preference.edit();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				verifica();
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 0, 2000);

		return 0;
	}

	public void verifica() {
		boolean temTemMax = preference
				.contains(ListaDispositivos.CHAVE_TEMP_MAX);
		boolean temTemMin = preference
				.contains(ListaDispositivos.CHAVE_TEMP_MIN);
		boolean humMax = preference.contains(ListaDispositivos.CHAVE_HUM_MAX);
		boolean humMin = preference.contains(ListaDispositivos.CHAVE_HUM_MIN);
		boolean gelIni = preference
				.contains(ListaDispositivos.CHAVE_GEL_INICIO);
		boolean gelFim = preference.contains(ListaDispositivos.CHAVE_GEL_FIM);
		
		final int temMaxVl = Integer.parseInt(preference.getString(
				ListaDispositivos.CHAVE_TEMP_MAX, "0"));
		final int temMinVl = Integer.parseInt(preference.getString(
				ListaDispositivos.CHAVE_TEMP_MIN, "0"));
		final int humMaxVl = Integer.parseInt(preference.getString(
				ListaDispositivos.CHAVE_HUM_MAX, "0"));
		final int humMinVl = Integer.parseInt(preference.getString(
				ListaDispositivos.CHAVE_HUM_MIN, "0"));
		final int gelIniVl = Integer.parseInt(preference.getString(
				ListaDispositivos.CHAVE_GEL_INICIO, "0"));
		final int gelFimVl = Integer.parseInt(preference.getString(
				ListaDispositivos.CHAVE_GEL_FIM, "0"));
		
		Log.e("TELEFONICA", "getIniVl: "+gelIniVl);
		Log.e("TELEFONICA", "getFimVl: "+gelFimVl);

		if (temTemMin && temTemMax) {
			AQuery aq = new AQuery(this);
			aq.ajax("http://dca.telefonicabeta.com/m2m/v2/services/47lfm5rhbq5u/"
					+ "assets/47lfm5rhbq5u/data/?limit=1&offset=0&sortBy=!samplingTime&attribute=temperature",
					String.class, new AjaxCallback<String>() {
						@Override
						public void callback(String url, String object,
								AjaxStatus status) {
							try {
								JSONObject root = new JSONObject(object);
								JSONArray data = root.getJSONArray("data");

								JSONObject obj = data.getJSONObject(0);
								JSONObject ms = obj.getJSONObject("ms");
								int v = ms.getInt("v");

								if (v < temMinVl || v > temMaxVl) {
									contTemperaturaExcecao++;
									if (contTemperaturaExcecao > 5) {
										lancaNotificacao("A temperatura da geladeira está desregulada!");
									}
								} else {
									contTemperaturaExcecao = 0;
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
		}

		if (humMax && humMin) {
			AQuery aq = new AQuery(this);
			aq.ajax("http://dca.telefonicabeta.com/m2m/v2/services/47lfm5rhbq5u/"
					+ "assets/47lfm5rhbq5u/data/?limit=1&offset=0&sortBy=!samplingTime&attribute=relativeHumidity",
					String.class, new AjaxCallback<String>() {
						@Override
						public void callback(String url, String object,
								AjaxStatus status) {
							try {
								JSONObject root = new JSONObject(object);
								JSONArray data = root.getJSONArray("data");

								JSONObject obj = data.getJSONObject(0);
								JSONObject ms = obj.getJSONObject("ms");
								int v = ms.getInt("v");

								if (v < humMinVl || v > humMaxVl) {
									contHumidadeExcecao++;
									if (contHumidadeExcecao > 5) {
										lancaNotificacao("A humidade da geladeira está desregulada!");
									}
								} else {
									contHumidadeExcecao = 0;
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
		}

		if (gelIni && gelFim) {
			AQuery aq = new AQuery(this);
			aq.ajax("http://dca.telefonicabeta.com/m2m/v2/services/47lfm5rhbq5u/"
					+ "assets/47lfm5rhbq5u/data/?limit=1&offset=0&sortBy=!samplingTime&attribute=amount",
					String.class, new AjaxCallback<String>() {
						@Override
						public void callback(String url, String object,
								AjaxStatus status) {
							try {
								JSONObject root = new JSONObject(object);
								JSONArray data = root.getJSONArray("data");

								JSONObject obj = data.getJSONObject(0);
								JSONObject ms = obj.getJSONObject("ms");
								int v = ms.getInt("v");

								if (v == 1) {
									long tempoMillis = System
											.currentTimeMillis();
									Calendar cal = Calendar.getInstance();
									cal.setTimeInMillis(tempoMillis);
									int hora = cal.get(Calendar.HOUR_OF_DAY);
									if (hora >= gelIniVl && hora <= gelFimVl) {
										lancaNotificacao("Alguém está assaltando a geladeira!");
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		}
	}

	public void lancaNotificacao(String aviso) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, "Atenção!", when);
		notification.setLatestEventInfo(this, "Atenção", aviso, null);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
	}

}
