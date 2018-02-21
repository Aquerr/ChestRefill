package io.github.aquerr.chestrefill.version;

import com.google.gson.*;
import io.github.aquerr.chestrefill.PluginInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;

/**
 * Created by Aquerr on 2018-02-21.
 */

public class VersionChecker
{
    private static final String USER_AGENT = "Mozilla/5.0";

    public static boolean isLatest(String version)
    {
        //TODO: Check is current version is the latest version

        //String latest = "http://api.github.com/repos/aquerr/chestrefill/releases/latest";

        //This is just for testing
        String latest = "https://api.github.com/repos/Aquerr/EagleFactions/releases";
        String currentTag = "https://api.github.com/repos/Aquerr/EagleFactions/tags/v0.9.10";

        String jsonData = sendRequest(latest);

        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(jsonData);

        if (jsonElement.isJsonArray())
        {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            JsonElement latestRelease = jsonArray.get(0);
            Date latestReleaseDate = Date.from(Instant.parse(latestRelease.getAsJsonObject().get("published_at").getAsString()));

            

        }
        else if(jsonElement.isJsonObject())
        {
            JsonObject jsonArray = jsonElement.getAsJsonObject();

        }



        return false;
    }

    private static String sendRequest(String request)
    {
        try
        {
            URL url = new URL(request);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = bufferedReader.readLine()) != null)
                {
                    response.append(inputLine);
                }
                bufferedReader.close();

                return response.toString();
            }

        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
