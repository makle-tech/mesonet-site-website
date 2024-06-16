import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve static files from the /public directory
        server.createContext("/", new StaticFileHandler("/public/index.html"));
        
        server.createContext("/data", new DataHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }

    static class StaticFileHandler implements HttpHandler {
        private final String filePath;

        StaticFileHandler(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            File file = new File("public/index.html");

            if (file.exists()) {
                byte[] byteArray = Files.readAllBytes(file.toPath());
                t.getResponseHeaders().set("Content-Type", "text/html");
                t.sendResponseHeaders(200, byteArray.length);
                OutputStream os = t.getResponseBody();
                os.write(byteArray, 0, byteArray.length);
                os.close();
            } else {
                String response = "404 (Not Found)\n";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String jsonResponse = fetchDataFromURL("https://mesonet-010makdev.replit.app");
            t.getResponseHeaders().set("Content-Type", "application/json");
            byte[] response = jsonResponse.getBytes();
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response, 0, response.length);
            os.close();
        }

        private String fetchDataFromURL(String urlString) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
            } catch (Exception e) {
                e.printStackTrace();
                result.append("{\"windSpeed\": \"No data yet\", \"windDirection\": \"No data yet\", \"timestamp\": \"2024-06-16T22:30:38.004Z\"}");
            }
            return result.toString();
        }
    }
}
