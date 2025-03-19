package eu.hansolo.fx.glucopi;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletionException;

import static java.nio.charset.StandardCharsets.UTF_8;


public class Helper {
    private static HttpClient httpClient;


    // ******************** REST calls ****************************************
    public static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                         .connectTimeout(Duration.ofSeconds(20))
                         .followRedirects(Redirect.NORMAL)
                         .version(java.net.http.HttpClient.Version.HTTP_2)
                         .build();
    }

    public static final HttpResponse<String> get(final String uri) {
        if (null == httpClient) { httpClient = createHttpClient(); }
        HttpRequest request = HttpRequest.newBuilder()
                                         .GET()
                                         .uri(URI.create(uri))
                                         .setHeader("Accept", "application/json")
                                         .timeout(Duration.ofSeconds(60))
                                         .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response;
            } else {
                // Problem with url request
                System.out.println("Error connecting to " + uri + " with response code: " + response.statusCode());
                return response;
            }
        } catch (CompletionException | InterruptedException | IOException e) {
            System.out.println("Error connecting to " + uri + " with exception: " + e);
            return null;
        }
    }
    public static final HttpResponse<String> get(final String uri, final String apiKey) { return get(uri, apiKey, ""); }
    public static final HttpResponse<String> get(final String uri, final String apiKey, final String userAgent) {
        if (null == httpClient) { httpClient = createHttpClient(); }
        final String userAgentText = (null == userAgent || userAgent.isEmpty()) ? "ConfiCheck" : "ConfiCheck (" + userAgent + ")";
        HttpRequest request = HttpRequest.newBuilder()
                                         .GET()
                                         .uri(URI.create(uri))
                                         .setHeader("Accept", "application/json")
                                         .setHeader("User-Agent", userAgentText)
                                         .setHeader("x-api-key", apiKey) // needed for Intelligence Cloud authentification
                                         .timeout(Duration.ofSeconds(60))
                                         .build();
        //System.out.println(request.toString());
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response;
            } else if (response.statusCode() == 503) {
                System.out.println("Rate limited");
                return response;
            } else if (response.statusCode() == 403) {
                System.out.println("Forbidden");
                return response;
            } else if (response.statusCode() == 404) {
                System.out.println("Not Found");
                return response;
            } else {
                // Problem with url request
                return response;
            }
        } catch (CompletionException | InterruptedException | IOException e) {
            return null;
        }
    }

    public static final HttpResponse<String> httpHeadRequestSync(final String uri) {
        if (null == httpClient) { httpClient = createHttpClient(); }

        final HttpRequest request = HttpRequest.newBuilder()
                                               .HEAD()
                                               .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                               .uri(URI.create(uri))
                                               .build();

        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            return response;
        } catch (CompletionException | InterruptedException | IOException e) {
            return null;
        }
    }

    public static final String getTextFromUrl(final String uri) {
        // Get all text from given uri
        try (var stream = URI.create(uri).toURL().openStream()) {
            return new String(stream.readAllBytes(), UTF_8);
        } catch(Exception e) {
            System.out.println("Error reading text from uri: " + uri);
            return "";
        }
    }


    // ******************** Internal Classes **********************************
    static class StreamReader extends Thread {
        private final InputStream  is;
        private final StringWriter sw;

        StreamReader(final InputStream is) {
            this.is = is;
            sw = new StringWriter();
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1) { sw.write(c); }
            } catch (IOException e) { }
        }

        String getResult() { return sw.toString(); }
    }
}

