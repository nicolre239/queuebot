import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {

    private static String BOT_NAME = "Queue";
    private static String BOT_TOKEN = "604894063:AAFmwrvtWWLQmA_6tTscqOsIeQY0Cy6LA7A" /* your bot's token here */;

    private static String PROXY_HOST = "192.116.142.153" /* proxy host */;
    private static Integer PROXY_PORT = 8080 /* proxy port */;

    public static void main(String[] args) {
        try {

            ApiContextInitializer.init();

            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi();

            // Set up Http proxy
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            HttpHost httpHost = new HttpHost(PROXY_HOST, PROXY_PORT);

            RequestConfig requestConfig = RequestConfig.custom().setProxy(httpHost).setAuthenticationEnabled(false).build();
            botOptions.setRequestConfig(requestConfig);
            botOptions.setHttpProxy(httpHost);

            // Register your newly created AbilityBot
            Bot bot = new Bot(BOT_TOKEN, BOT_NAME, botOptions);

            botsApi.registerBot(bot);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}