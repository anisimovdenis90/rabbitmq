import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class ExchangeReceiverApp {

    enum ThemeOfArticle {
        JAVA ("java"),
        C("c"),
        PHP("php");

        String name;

        ThemeOfArticle(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final String EXCHANGE_NAME = "ITBlog";

    private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
    private static Connection connection;
    private static Channel channel;
    private static String queueName;

    public static void main(String[] argv) {
        start();

        System.out.println("Команды:");
        System.out.println("j - подписка на статьи по JAVA");
        System.out.println("c - подписка на статьи по C");
        System.out.println("p - подписка на статьи по PHP");
        System.out.println("uj - отписаться от статей по JAVA");
        System.out.println("uc - отписаться от статей по C");
        System.out.println("up - отписаться от статей по PHP");

        while (true) {
            try {
                String consoleMessage = consoleReader.readLine();
                if ("j".equals(consoleMessage.toLowerCase())) {
                    themeBinding(ThemeOfArticle.JAVA);
                }
                if ("c".equals(consoleMessage.toLowerCase())) {
                    themeBinding(ThemeOfArticle.C);
                }
                if ("p".equals(consoleMessage.toLowerCase())) {
                    themeBinding(ThemeOfArticle.PHP);
                }
                if ("uj".equals(consoleMessage.toLowerCase())) {
                    themeUnBinding(ThemeOfArticle.JAVA);
                }
                if ("uc".equals(consoleMessage.toLowerCase())) {
                    themeUnBinding(ThemeOfArticle.C);
                }
                if ("up".equals(consoleMessage.toLowerCase())) {
                    themeUnBinding(ThemeOfArticle.PHP);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private static void start() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            queueName = channel.queueDeclare().getQueue();
            System.out.println("My queue name: " + queueName);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void themeBinding(ThemeOfArticle themeOfArticle) {
        try {
            channel.queueBind(queueName, EXCHANGE_NAME, themeOfArticle.getName());
            System.out.println(" [*] " + themeOfArticle.getName().toUpperCase() + " article enabled");
            System.out.println(" [*] Waiting for messages");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void themeUnBinding(ThemeOfArticle themeOfArticle) {
        try {
            channel.queueUnbind(queueName, EXCHANGE_NAME, themeOfArticle.getName());
            System.out.println(" [*] " + themeOfArticle.getName() + " article disabled");
            System.out.println(" [*] Waiting for messages");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
