# AlarmMe - A Location based alarm app

## How to use this project

You need Android Studio to work with this repository.

To compile this project you will need to be a Google Maps developer, in order to create and display the map. You can get an API key by visiting the [Google Maps Android API](https://developers.google.com/maps/documentation/android-sdk/get-api-key) website.

This app also uses the [OpenWeather API](https://openweathermap.org/api). Register (its free) and grab your API key.

Then lastly create a resource file `.../res/values/api_keys.xml` (this path is ignored by git) with the following content:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_maps_api_key" templateMergeStrategy="preserve" translatable="false">YOUR_GOOGLE_MAPS_API_KEY</string>
    <string name="open_weather_api_key" translatable="false">YOUR_OPEN_WEATHER_API_KEY</string>
</resources>
```

The `Kotlin` plugin for Android Studio is also required.
