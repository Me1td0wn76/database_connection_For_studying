package com.example.database_connection.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.database_connection.model.User;
import com.example.database_connection.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {
        return args -> {
            // サンプルユーザーを追加
            repository.save(new User("田中太郎", "tanaka@example.com"));
            repository.save(new User("佐藤花子", "sato@example.com"));
            repository.save(new User("鈴木一郎", "suzuki@example.com"));

            System.out.println("========================================");
            System.out.println("データベースの初期化が完了しました");
            System.out.println("サンプルユーザー数: " + repository.count());
            System.out.println("========================================");
        };
    }
}
