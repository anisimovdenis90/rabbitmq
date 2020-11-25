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

        System.out.println("Укажите тему:");
        System.out.println("j - статьи по JAVA");
        System.out.println("c - статьи по C");
        System.out.println("p - статьи по PHP");

        while (true) {
            try {
                String consoleMessage = consoleReader.readLine();
                if ("j".equals(consoleMessage.toLowerCase())) {
                    themeBinding(ThemeOfArticle.JAVA);
                    break;
                }
                if ("c".equals(consoleMessage.toLowerCase())) {
                    themeBinding(ThemeOfArticle.C);
                    break;
                }
                if ("p".equals(consoleMessage.toLowerCase())) {
                    themeBinding(ThemeOfArticle.PHP);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(" [*] Waiting for messages");
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
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void themeBinding(ThemeOfArticle themeOfArticle) {
        try {
            channel.queueBind(queueName, EXCHANGE_NAME, themeOfArticle.getName());
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void stop() {
        try {
            connection.close();
            channel.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}