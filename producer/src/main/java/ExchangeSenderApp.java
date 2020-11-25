import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class ExchangeSenderApp {

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


    public static void main(String[] argv) {
        start();

        System.out.println("Консольные команды:");
        System.out.println("end - завершение работы");
        System.out.println("j +текст_статьи - статья по JAVA");
        System.out.println("c +текст_статьи - статья по C#");
        System.out.println("p +текст_статьи - статья по PHP");

        while (true) {
            try {
                String message = consoleReader.readLine();
                if ("end".equals(message.toLowerCase())) {
                    break;
                } else if ("j".equals(message.split(" ", 2)[0])) {
                    sendMessage(message.split(" ", 2)[1], ThemeOfArticle.JAVA);
                } else if ("c".equals(message.split(" ", 2)[0])) {
                    sendMessage(message.split(" ", 2)[1], ThemeOfArticle.C);
                } else if ("p".equals(message.split(" ", 2)[0])) {
                    sendMessage(message.split(" ", 2)[1], ThemeOfArticle.PHP);
                } else {
                    System.out.println("Неизвестная команда " + "'" + message.split(" ", 2)[0] + "'");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        stop();
    }

    private static void start() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(String message, ThemeOfArticle themeOfArticle) {
        try {
            channel.basicPublish(EXCHANGE_NAME, themeOfArticle.getName(), null, message.getBytes("UTF-8"));
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
