package com.github.swapUniba.pulse.crowd.appcategorize;

import com.github.frapontillo.pulse.util.PulseLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.Map;
import java.util.Scanner;

public class GooglePlayCategorizer {

    private static final String FILE_NAME = "category_app.txt";
    private static final String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=";
    private static String FILE_PATH;

    private static Logger logger = PulseLogger.getLogger(GooglePlayCategorizer.class);

    private String packageName;
    private String category;

    static {
        try {

            FILE_PATH = Paths.get(
                    GooglePlayCategorizer.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent().toString();

        } catch (URISyntaxException e) {
            logger.error("Unable to retrieve the path correctly");
        }
    }

    public GooglePlayCategorizer(String packageName) {
        this.packageName = packageName;
    }

    public String getCategory() {
        if (category == null) {
            Map<String, String> categoryMap = readCategoryApp();

            if (categoryMap.containsKey(packageName)) {
                category = categoryMap.get(packageName);
                logger.info("Used cached category for " + packageName);
            } else {
                String content;
                URLConnection connection;

                try {
                    connection = new URL(GOOGLE_PLAY_URL + packageName).openConnection();
                    Scanner scanner = new Scanner(connection.getInputStream());
                    scanner.useDelimiter("\\Z");
                    content = scanner.next();

                    String[] strings = content.split("\"genre\">");

                    // check
                    if (strings.length == 2) {
                        strings = strings[1].split("</span>");
                        category = strings[0];
                    }

                } catch (Exception ex) {
                    logger.error("An error occurred during the connection to Google Play");
                }

                categoryMap.put(packageName, category);
                if (!saveCategoryApp(categoryMap)) {
                    logger.error("An error occurred during the file saving");
                }
            }
        }
        return category;
    }

    private static Boolean saveCategoryApp(Map<String, String> categoryMap){
        Boolean done = false;
        Gson gson = new Gson();
        String json = gson.toJson(categoryMap);

        if (FILE_PATH != null) {
            Path path = FileSystems.getDefault().getPath(FILE_PATH, FILE_NAME);
            try {
                Files.write(path, json.getBytes("UTF-8"));
                done = true;
            } catch (IOException e) {
                logger.error("Unable to save the file to path " + FILE_PATH);
            }
        }

        return done;
    }

    private static Map<String, String> readCategoryApp() {
        Gson gson = new Gson();
        String json = null;
        if (FILE_PATH != null) {
            Path path = FileSystems.getDefault().getPath(FILE_PATH, FILE_NAME);
            try {
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    json = new String(Files.readAllBytes(path));
                } else {
                    json = "{}";
                    Files.write(path, json.getBytes("UTF-8"),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
    }

}
