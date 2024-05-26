import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

//Abruf von Wetter Daten aus der API
//returned aktuelle Wetterdaten aus API
//GUI zeigt diese an
public class WetterApp {
//abruf der Wetter Daten aus gegebener Region
    public static JSONObject getWeatherData(String locationName)    {
        //Region Koordinaten via geolocation API
        JSONArray locationData = getLocationData(locationName);

        //extrahiere latitude und longitude Daten
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        //API Anfrage URL mit Regionkoordinaten
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" +
                latitude +
                "&longitude=" +
                longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m";
        try {
        //Ruf API auf und bekomm eine Antwort
            HttpURLConnection conn = fetchApiResponse(urlString);
        //Schau nach Antwortstatus - 200 ist Erfolg
            if(conn.getResponseCode() != 200)  {
                System.out.println("Error: Could not connect to API");
                return null;
            }
        //Speicher JSON Ergebnisse
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext())   {
            //Lies und speicher im String Builder
                resultJson.append(scanner.nextLine());
            }
            //Schließe Scanner
            scanner.close();

            //Schließe URL Verbindung
            conn.disconnect();

            //Parse durch unsere Daten
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            //Empfang stündliche Daten
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            //Unsere aktuelle Zeit
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);
            //Krieg temperatur
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            //Krieg Weather Code
            JSONArray weathercode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));


            //Krieg Feuchtigkeitsdaten
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            //Krieg Windgeschwindigkeit
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windspeedData.get(index);

            //An das Frontend zu übergebenes JSON Objekt, mit den Daten
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);
            return weatherData;

        } catch(Exception e)  {
            e.printStackTrace();
        }
        return null;
    }
    //Ruft Geokoordinaten für Regionnamen ab
    public static JSONArray getLocationData(String locationName)   {
    //alle Leertasten aus Regionname entfernen und zum + ändern - wegen json format
    locationName =locationName.replaceAll(" ", "+");
    //Bau die API URL mit Region Parametern
    String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
            locationName + "&count=10&language=en&format=json";

    try {
        //Ruf API auf und bekomm Antwort
        HttpURLConnection conn = fetchApiResponse(urlString);

        //Check Antwortstatus, falls 200 lief alles gut
        if (conn.getResponseCode() != 200)   {
            System.out.println("Error: Could not connect API");
            return null;
        }   else {
            //Speicher API Ergebnisse
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            //Lies und speicher die json Ergebnisse im StringBuilder
            while(scanner.hasNext())    {
                resultJson.append(scanner.nextLine());
            }
            //Schließe Scanner
            scanner.close();

            //Schließe URL Verbindung
            conn.disconnect();

            //Parse den json String in ein json Objekt
            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            //Bekomme die Liste der Regionsdaten, welche die API generiert hat, durch den Ortsnamen
            JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
            return locationData;

        }

    }   catch (Exception e)  {
        e.printStackTrace();
    }
    //Kann dieRegion nicht finden
    return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString)  {
        try  {
            //Verbindungsaufbau versuch
            URL url = new URL(urlString);
            HttpURLConnection conn  = (HttpURLConnection) url.openConnection();

            //Setze Abruf Methode auf get
            conn.setRequestMethod("GET");

            //Mit unserer API Verbinden
            conn.connect();
            return conn;
        }   catch(IOException e)  {
            e.printStackTrace();
        }
        //Konnte keine Verbindung herstellen
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList)  {
        String currentTime = getCurrentTime();
        //Durch Zeitliste gehen und passenden Index unserer Zeit finden
        for(int i = 0; i<timeList.size();i++)  {
        String time = (String) timeList.get(i);
        if(time.equalsIgnoreCase(currentTime))  {
            //gib den Index zurück
            return i;
        }
        }
        return 0;
    }

    public static String getCurrentTime()  {
        //bekomm aktuelles Datum und Zeit
        LocalDateTime currentDateTime = LocalDateTime.now();

        //Formatiere Datum zu 2023-09-02T00:00
        //so wirds in der API gelesen
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        //Formatiere und gib die Zeit aus
        String formattedDataTime = currentDateTime.format(formatter);
        return formattedDataTime;
    }

    //Weathercode lesbar machen
    private static String convertWeatherCode(long weathercode)  {
        String weatherCondition = "";
        if(weathercode == 0L)  {
            weatherCondition = "Clear";
        }   else if(weathercode <= 3L && weathercode > 0L)  {
            weatherCondition = "Cloudy";
        } else if ((weathercode >= 51L && weathercode <= 67L)
        || weathercode >= 80L && weathercode <= 99L) {
            weatherCondition = "Rain";
        } else if (weathercode >= 71L && weathercode <= 77L) {
            weatherCondition = "Snow";
        }
        return weatherCondition;

    }
}
