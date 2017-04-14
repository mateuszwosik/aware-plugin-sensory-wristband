# AWARE Plugin: Sensory Wristband

# Settings
Parameters adjusted on the dashboard and client:
 - <b>status_plugin_sensory_wristband:</b> (boolean) activate/deactivate plugin
 - <b>frequency_heart_rate:</b> (int) frequency of heart rate measurements

# Broadcasts
  - <b>ACTION_AWARE_BAND_CONNECTED:</b> send when connection and pairing to the band was successful
    - <b>EXTRA_BAND_NAME_DATA:</b> name of connected band
  - <b>ACTION_AWARE_BAND_DISCONNECTED:</b> send when band was disconnected
  - <b>ACTION_AWARE_BAND_NEW_DATA:</b> send when new data was obtained
    - <b>EXTRA_BAND_RSSI_DATA:</b> (int) signal strength
    - <b>EXTRA_BAND_BATTERY_DATA:</b> (BatteryInfo) battery information
    - <b>EXTRA_BAND_HEART_RATE_DATA:</b> (int) heart rate 
    - <b>EXTRA_BAND_STEPS_DATA:</b> (StepsInfo) steps information

# Providers

## Heart Rate Table
> Send after every correct heart rate measurement

Field | Type | Description
----- | ---- | -----------
_id | INTEGER | primary key auto-incremented
timestamp | REAL | unix timestamp in milliseconds of sample
device_id | TEXT | AWARE device ID
heart_rate | INTEGER | heart rate value (bpm)

## Steps Information Table
> Send on disconnecting the band

Field | Type | Description
----- | ---- | -----------
_id | INTEGER | primary key auto-incremented
timestamp | REAL | unix timestamp in milliseconds of sample
device_id | TEXT | AWARE device ID
steps | INTEGER | number of steps
distance | INTEGER | traveled distnce (m)
calories | INTEGER | burned calories (kcal)
