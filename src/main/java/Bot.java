import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import static org.telegram.abilitybots.api.db.MapDBContext.onlineInstance;

public class Bot {
    // private static String BOT_NAME = "teater"; //"MyTestBot";
    // private static String BOT_TOKEN = "854110210:AAGZCyWzVq2ge2SnXcTE75I0R5rlhqoLqR4"; //"881713712:AAErddrey3kouEag5Qxyds7e6Fd-5Q-9kh4";

    private static String PROXY_HOST = "200.89.178.88";
    private static Integer PROXY_PORT = 80;

    public static void main(String[] args){
        try {
            if (checkProxyConnection()) {
                ApiContextInitializer.init();

                TelegramBotsApi botsApi = new TelegramBotsApi();

                System.out.println(DefaultBotOptions.class);
                DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

                botOptions.setProxyHost(PROXY_HOST);
                botOptions.setProxyPort(PROXY_PORT);
                botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
                System.out.println("222");
                try {
                    botsApi.registerBot(new MyPoolBot(botOptions));
                    System.out.println("regist");
                } catch (TelegramApiRequestException e) {
                    e.printStackTrace();
                }

                System.out.println("end");
            }

        } catch (IOException e) {
            System.out.println("PROXY FAILED");
        }

    }

    //ф-ция, вызывающая класс, где проверяется работоспособность прокси
    private static boolean checkProxyConnection() throws IOException {
        String proxyType = "HTTP";

        CheckProxy checkProxy = new CheckProxy();
        boolean result = checkProxy.check(PROXY_HOST, PROXY_PORT, proxyType);
        return result;
    }
}