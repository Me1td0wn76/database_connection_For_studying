package com.example.database_connection.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.database_connection.model.User;
import com.example.database_connection.repository.UserRepository;
import com.example.database_connection.service.DatabaseInfoService;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    @Autowired
    private DatabaseInfoService databaseInfoService;

    @Autowired
    private UserRepository userRepository;

    /**
     * データベース接続情報を取得
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        return ResponseEntity.ok(databaseInfoService.getDatabaseInfo());
    }

    /**
     * コネクションプール情報を取得
     */
    @GetMapping("/pool")
    public ResponseEntity<Map<String, Object>> getPoolInfo() {
        return ResponseEntity.ok(databaseInfoService.getConnectionPoolInfo());
    }

    /**
     * 接続テスト
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> result = new HashMap<>();
        boolean connected = databaseInfoService.testConnection();
        result.put("connected", connected);
        result.put("message", connected ? "接続成功!" : "接続失敗");
        return ResponseEntity.ok(result);
    }

    /**
     * テーブル一覧取得
     */
    @GetMapping("/tables")
    public ResponseEntity<List<String>> getTables() {
        return ResponseEntity.ok(databaseInfoService.getTableNames());
    }

    /**
     * カスタムクエリ実行
     */
    @PostMapping("/query")
    public ResponseEntity<?> executeQuery(@RequestBody Map<String, String> request) {
        try {
            String sql = request.get("sql");
            List<Map<String, Object>> results = databaseInfoService.executeQuery(sql);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * ユーザー一覧取得
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * ユーザー作成
     */
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * ユーザー削除
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "ユーザーが削除されました");
        return ResponseEntity.ok(response);
    }
}
