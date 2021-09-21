package com.clinton;

public class Application {
    public static void main(String[] args) {
        NewsConsumer newsConsumer = new NewsConsumer();

        newsConsumer.start();
    }
}
