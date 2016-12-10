# AWARE Plugin: Sensory Wristband

# Settings
Parameters adjusted on the dashboard and client:
 - <b>status_plugin_sensory_wristband:</b> (boolean) activate/deactivate plugin

# Broadcasts
  - <b>ACTION_AWARE_BAND_CONNECTED:</b> send when connection and pairing to the band was successful
    - <b>EXTRA_DATA:</b> name of connected band
  - <b>ACTION_AWARE_BAND_DISCONNECTED:</b> send when band was disconnected
  - <b>ACTION_AWARE_BAND_ENABLE_NOTIFICATION:</b> send when plugin enable notifications send from the band
  - <b>ACTION_AWARE_BAND_DISABLE_NOTIFICATION:</b> send when plugin disable notifications send from the band
  - <b>ACTION_AWARE_BAND_RSSI:</b> send when signal strength of the band was updated
    - <b>EXTRA_DATA:</b> (int) signal strength
  - <b>ACTION_AWARE_BAND_BATTERY:</b> send when was change in battery information received from the band
    - <b>EXTRA_DATA:</b> (BatteryInfo) battery information (now only battery level)
  - <b>ACTION_AWARE_BAND_HEART_RATE:</b> send when heart rate was measured
    - <b>EXTRA_DATA:</b> (int) heart rate (when heart rate is equal to 0 it means that was not correctly measured) 
  - <b>ACTION_AWARE_BAND_STEPS:</b> send when received notification from the band with steps informations
    - <b>EXTRA_DATA:</b> (StepsInfo) steps information

# Providers

## Heart Rate Table
> Send after every heart rate measurement

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
