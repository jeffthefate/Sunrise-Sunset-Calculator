package com.kbsbng.androidapps.sunrise_sunset_calculator;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;

import android.app.DatePickerDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class CurrentLocationSunriseSunsetCalcution extends FragmentActivity {
	private static final int POINT_ON_MAP_ACTIVITY = 2;
	private EditText dateField;
	private EditText locationField;

	private int year;
	private int month;
	private int day;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location loc;
	
	int chosenLongitude;
	int chosenLatitude;
	
	enum LocationSource {
		MY_CURRENT_LOCATION,
		POINT_ON_MAP
	}

	private LocationSource locationSource = LocationSource.MY_CURRENT_LOCATION;
	
	public static class LocationTypeDialog extends DialogFragment {
		public static LocationTypeDialog newInstance() {
			return new LocationTypeDialog();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Dialog d = new Dialog(getActivity());
			d.setTitle("Choose Location:");
			return d;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.location_type_chooser,
					container, false);
			TextView currentLocationText = (TextView) v
					.findViewById(R.id.currentLocationOption);
			currentLocationText.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					((CurrentLocationSunriseSunsetCalcution) getActivity())
							.handleCurrectLocationChoice();
					getDialog().cancel();
				}
			});
			
			TextView pointOnMapText = (TextView) v.findViewById(R.id.pointOnMapOption);
			pointOnMapText.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					((CurrentLocationSunriseSunsetCalcution) getActivity())
					.handlePointOnMapChoice();
			getDialog().cancel();
				}
			});

			return v;
		}
	}

	public class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int y, int m, int d) {
			// Do something with the date chosen by the user
			year = y;
			month = m;
			day = d;
			updateDate();
			dateField.clearFocus();
			View focusSearch = dateField.focusSearch(View.FOCUS_FORWARD);
			if (focusSearch != null) {
				focusSearch.requestFocus();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		setContentView(R.layout.activity_current_location_sunrise_sunset_calculation);
		dateField = (EditText) findViewById(R.id.dateField);
		locationField = (EditText) findViewById(R.id.locationText);
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		updateDate();

		dateField.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_UP) {
					showDatePicker();
				}
				return true;
			}
		});

		locationField.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_UP) {
					showLocationPicker();
				}
				return true;
			}
		});

		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				loc = location;
				locationManager.removeUpdates(locationListener);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		requestForLocation();
		
	}


	private void requestForLocation() {
		String provider;
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		}
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		}
		else {
			provider = LocationManager.PASSIVE_PROVIDER;
		}
		locationManager.requestLocationUpdates(
				provider, 0, 0, locationListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(
				R.menu.activity_current_location_sunrise_sunset_calculation,
				menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case POINT_ON_MAP_ACTIVITY:
			if (resultCode != RESULT_OK) {
				break;
			}
			chosenLatitude = data.getExtras().getInt("latitude");
			chosenLongitude = data.getExtras().getInt("longitude");
			break;
		}
	}

	public void showDatePicker() {
		final DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}

	public void showLocationPicker() {
		final DialogFragment newFragment = LocationTypeDialog.newInstance();
		newFragment.show(getSupportFragmentManager(), "locationPicker");
	}
	
	private void findCurrentLocation() {
		if (loc == null) {
			loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		if (loc == null) {
			loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (loc == null) {
			loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}
		if (loc == null) {
			return;
		}
	}

	public void calculateSunriseSunset(View v) {
		Intent intent = new Intent(getApplicationContext(), DisplaySunCalculationResults.class);
		double longitude, latitude;
		
		if (locationSource == LocationSource.MY_CURRENT_LOCATION) {
			findCurrentLocation();
			longitude = loc.getLongitude();
			latitude = loc.getLatitude();
		}
		else {
			longitude = chosenLongitude / 1E6;
			latitude = chosenLatitude / 1E6;
		}
		
		intent.putExtra("longitude", longitude);
		intent.putExtra("latitude", latitude);
		intent.putExtra("day", day);
		intent.putExtra("month", month);
		intent.putExtra("year", year);
		startActivity(intent);
	}

	void handleCurrectLocationChoice() {
		locationSource = LocationSource.MY_CURRENT_LOCATION;
		locationField.setText("My Current Location");
		requestForLocation();
		View focusSearch = locationField.focusSearch(View.FOCUS_FORWARD);
		if (focusSearch != null) {
			focusSearch.requestFocus();
		}
	}
	protected void handlePointOnMapChoice() {
		locationSource = LocationSource.POINT_ON_MAP;
		locationField.setText("Point on map");
		Intent intent = new Intent(getApplicationContext(), ChooseLocationFromMap.class);
		startActivityForResult(intent, POINT_ON_MAP_ACTIVITY);
	}
	
	

	private void updateDate() {
		SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
		final Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		dateField.setText(format.format(c.getTime()));
	}
}
