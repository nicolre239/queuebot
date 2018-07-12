import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.logging.BotLogger;
import java.util.logging.FileHandler;

public class Main {
    public static void main(String[] args) {
        try {
            // logger
            FileHandler fileHandler = new FileHandler("logger.log", true);
            BotLogger.registerLogger(fileHandler);

            ApiContextInitializer.init();

            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi();

            // Set up Http proxy
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            // Proxy
            final String PROXY_HOST = "173.249.48.140" /* proxy host */;
            final Integer PROXY_PORT = 8080 /* proxy port */;
            HttpHost httpHost = new HttpHost(PROXY_HOST, PROXY_PORT);
            RequestConfig requestConfig = RequestConfig.custom().setProxy(httpHost).setAuthenticationEnabled(false).build();
            botOptions.setRequestConfig(requestConfig);
            botOptions.setHttpProxy(httpHost);

            // Register your newly created AbilityBot
            Bot bot = new Bot(BotConstants.BOT_TOKEN, BotConstants.BOT_NAME, botOptions);
            botsApi.registerBot(bot);

        } catch (Exception e) {
            ///all exception
            e.printStackTrace();
        }
    }
}