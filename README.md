# データベース接続可視化 - 学習教材

Spring Bootを使用したデータベース接続の学習用プロジェクトです。データベースへの接続、SQL実行、コネクションプールの監視などを視覚的に学ぶことができます。

## このプロジェクトで学べること

1. **データベース接続の基礎**
   - JDBCを使った接続方法
   - データソースの設定
   - 接続情報の確認方法

2. **コネクションプール**
   - HikariCPの仕組み
   - アクティブ接続とアイドル接続
   - プールサイズの管理

3. **SQLの実行**
   - JdbcTemplateの使い方
   - クエリの実行と結果の取得
   - エラーハンドリング

4. **Spring Data JPA**
   - エンティティの定義
   - リポジトリパターン
   - CRUD操作

## 起動方法

### 1. プロジェクトをビルド
```bash
mvnw clean install
```

### 2. アプリケーションを起動
```bash
mvnw spring-boot:run
```

### 3. ブラウザでアクセス
```
http://localhost:8080
```

## 主な機能

### 1. データベース接続情報の表示
- リアルタイムで接続状態を確認
- データベース製品名、バージョン
- ドライバ情報、接続URL

### 2. コネクションプール監視
- アクティブな接続数
- アイドル状態の接続数
- 待機中のスレッド数
- 自動更新機能(5秒ごと)

### 3. SQLクエリ実行
- カスタムSQLを実行
- 結果をテーブル形式で表示
- エラーメッセージの表示

### 4. ユーザー管理
- ユーザーの追加
- ユーザー一覧表示
- ユーザーの削除

### 5. H2コンソール
- ブラウザでデータベースに直接アクセス
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- ユーザー名: `sa`
- パスワード: (空)

## プロジェクト構成

```
src/main/java/com/example/database_connection/
├── DatabaseConnectionApplication.java  # メインクラス
├── config/
│   └── DataInitializer.java           # データ初期化
├── Controller/
│   ├── DatabaseController.java        # REST API
│   └── WebController.java             # ページコントローラ
├── model/
│   └── User.java                      # ユーザーエンティティ
├── repository/
│   └── UserRepository.java            # データアクセス層
└── service/
    └── DatabaseInfoService.java       # ビジネスロジック

src/main/resources/
├── application.properties             # 設定ファイル
└── templates/
    └── index.html                     # メインページ
```

## コードの作り方 - ステップバイステップ

このプロジェクトを一から作成する手順を詳しく解説します。

### ステップ1: プロジェクトの作成

**Spring Initializr** (https://start.spring.io/) で以下の設定でプロジェクトを作成:
- Project: Maven
- Language: Java
- Spring Boot: 3.5.7
- Java: 21
- Dependencies:
  - Spring Web
  - Spring Data JPA
  - H2 Database
  - Thymeleaf

### ステップ2: application.properties の設定

`src/main/resources/application.properties` を開き、データベース設定を追加:

```properties
spring.application.name=database_connection

# H2データベース設定
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2コンソール有効化
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA設定
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true

# HikariCP接続プール設定
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

**ポイント**:
- `jdbc:h2:mem:testdb` - インメモリデータベース(アプリ停止で消える)
- `spring.h2.console.enabled=true` - ブラウザからDBを確認できる
- `spring.jpa.show-sql=true` - 実行されるSQLをコンソールに表示

### ステップ3: エンティティクラスの作成

`src/main/java/com/example/database_connection/model/User.java`:

```java
package com.example.database_connection.model;

import jakarta.persistence.*;

@Entity  // ← JPA エンティティとして認識
@Table(name = "users")  // ← テーブル名を指定
public class User {
    
    @Id  // ← 主キー
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← 自動採番
    private Long id;
    
    private String name;
    private String email;
    
    // コンストラクタ、ゲッター、セッター
    public User() {}
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    // getter/setter は省略(必ず実装すること!)
}
```

**ポイント**:
- `@Entity` - このクラスがデータベーステーブルと対応
- `@Id` - 主キーを示す
- `@GeneratedValue` - IDを自動生成

### ステップ4: リポジトリの作成

`src/main/java/com/example/database_connection/repository/UserRepository.java`:

```java
package com.example.database_connection.repository;

import com.example.database_connection.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // メソッドを書かなくても基本的なCRUD操作が使える!
    // - findAll() - 全件取得
    // - findById() - ID検索
    // - save() - 保存
    // - deleteById() - 削除
}
```

**ポイント**:
- `JpaRepository<User, Long>` を継承するだけでCRUD操作が使える
- `User` がエンティティ型、`Long` が主キーの型

### ステップ5: サービスクラスの作成

`src/main/java/com/example/database_connection/service/DatabaseInfoService.java`:

```java
package com.example.database_connection.service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service  // ← ビジネスロジック層として認識
public class DatabaseInfoService {

    @Autowired  // ← 自動的にSpringが注入
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // データベース接続情報を取得
    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            info.put("connected", true);
            info.put("databaseProductName", metaData.getDatabaseProductName());
            info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            info.put("url", metaData.getURL());
            // ... その他の情報
        } catch (Exception e) {
            info.put("connected", false);
            info.put("error", e.getMessage());
        }
        
        return info;
    }

    // コネクションプール情報を取得
    public Map<String, Object> getConnectionPoolInfo() {
        Map<String, Object> poolInfo = new HashMap<>();
        
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
            
            poolInfo.put("activeConnections", poolMXBean.getActiveConnections());
            poolInfo.put("idleConnections", poolMXBean.getIdleConnections());
            poolInfo.put("totalConnections", poolMXBean.getTotalConnections());
            // ... その他の情報
        }
        
        return poolInfo;
    }

    // SQLクエリを実行
    public List<Map<String, Object>> executeQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
```

**ポイント**:
- `@Service` でビジネスロジック層として登録
- `@Autowired` で必要なオブジェクトを自動注入
- `JdbcTemplate` で簡単にSQL実行

### ステップ6: REST APIコントローラの作成

`src/main/java/com/example/database_connection/Controller/DatabaseController.java`:

```java
package com.example.database_connection.Controller;

import com.example.database_connection.model.User;
import com.example.database_connection.repository.UserRepository;
import com.example.database_connection.service.DatabaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController  // ← REST APIのコントローラ
@RequestMapping("/api/database")  // ← ベースURL
public class DatabaseController {

    @Autowired
    private DatabaseInfoService databaseInfoService;

    @Autowired
    private UserRepository userRepository;

    // GET /api/database/info
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        return ResponseEntity.ok(databaseInfoService.getDatabaseInfo());
    }

    // GET /api/database/users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // POST /api/database/users
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // DELETE /api/database/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
```

**ポイント**:
- `@RestController` - JSON形式でレスポンスを返す
- `@GetMapping`, `@PostMapping`, `@DeleteMapping` - HTTPメソッドに対応
- `@RequestBody` - リクエストボディをJavaオブジェクトに変換
- `@PathVariable` - URLのパラメータを取得

### ステップ7: Webページコントローラの作成

`src/main/java/com/example/database_connection/Controller/WebController.java`:

```java
package com.example.database_connection.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller  // ← HTMLページを返すコントローラ
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";  // ← templates/index.html を表示
    }
}
```

**ポイント**:
- `@Controller` - ビューを返す(HTMLページ)
- `@RestController` との違いに注意

### ステップ8: 初期データの設定

`src/main/java/com/example/database_connection/config/DataInitializer.java`:

```java
package com.example.database_connection.config;

import com.example.database_connection.model.User;
import com.example.database_connection.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // ← 設定クラス
public class DataInitializer {

    @Bean  // ← Springが管理するオブジェクトとして登録
    CommandLineRunner initDatabase(UserRepository repository) {
        return args -> {
            // アプリ起動時に実行される
            repository.save(new User("田中太郎", "tanaka@example.com"));
            repository.save(new User("佐藤花子", "sato@example.com"));
            repository.save(new User("鈴木一郎", "suzuki@example.com"));
            
            System.out.println("データベースの初期化が完了しました");
        };
    }
}
```

**ポイント**:
- `@Configuration` - 設定クラスとして認識
- `CommandLineRunner` - アプリ起動時に実行
- サンプルデータを自動投入

### ステップ9: HTMLページの作成

`src/main/resources/templates/index.html`:

```html
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>データベース接続可視化</title>
    <style>
        /* CSSスタイル */
    </style>
</head>
<body>
    <div class="container">
        <h1>データベース接続可視化</h1>
        
        <!-- データベース接続情報を表示するエリア -->
        <div id="connectionStatus"></div>
        
        <!-- ボタン -->
        <button onclick="loadDatabaseInfo()">更新</button>
    </div>

    <script>
        // JavaScriptでAPIを呼び出す
        async function loadDatabaseInfo() {
            const response = await fetch('/api/database/info');
            const data = await response.json();
            
            // HTMLに結果を表示
            document.getElementById('connectionStatus').innerHTML = 
                `<p>接続状態: ${data.connected ? '接続中' : '未接続'}</p>`;
        }
        
        // ページ読み込み時に自動実行
        window.onload = function() {
            loadDatabaseInfo();
        };
    </script>
</body>
</html>
```

**ポイント**:
- `fetch()` でREST APIを呼び出し
- `async/await` で非同期処理
- JSONデータを受け取ってHTMLに表示

### ステップ10: 実行とテスト

```bash
# ビルド
./mvnw clean install

# 起動
./mvnw spring-boot:run

# ブラウザでアクセス
# http://localhost:8080
```

## 🔍 データフローの理解

```
ブラウザ (index.html)
    ↓ fetch('/api/database/info')
WebController / DatabaseController (@RestController)
    ↓ サービス呼び出し
DatabaseInfoService (@Service)
    ↓ データアクセス
UserRepository (@Repository)
    ↓ JDBC/JPA
DataSource (HikariCP)
    ↓ SQL実行
H2 Database (インメモリDB)
```

## 重要な概念

### 1. 依存性注入 (Dependency Injection)
```java
@Autowired
private UserRepository userRepository;
```
- Springが自動的にオブジェクトを生成して注入
- `new` を使わずにインスタンスを取得できる

### 2. レイヤーアーキテクチャ
```
Controller層 → ユーザーからのリクエストを受け取る
Service層   → ビジネスロジックを処理
Repository層 → データベースアクセス
Model層     → データ構造を定義
```

### 3. アノテーションの役割
- `@Entity` - データベーステーブルと対応
- `@Repository` - データアクセス層
- `@Service` - ビジネスロジック層
- `@Controller` - プレゼンテーション層(HTML)
- `@RestController` - REST API層(JSON)
- `@Autowired` - 依存性注入

### 4. コネクションプールの仕組み
```
アプリ起動
  ↓
HikariCPが接続プールを作成(最小5本の接続を確保)
  ↓
リクエスト来る → プールから接続を取得
  ↓
SQL実行
  ↓
接続を返却(プールに戻す) ← クローズしない!再利用!
  ↓
次のリクエストで再利用
```

## さらに学習を深めるには

1. **H2コンソールで直接SQL実行**
   - `http://localhost:8080/h2-console`
   - テーブル構造を確認
   - 複雑なSQLを試す

2. **コードを改造してみる**
   - 新しいエンティティを追加(例: Book, Product)
   - 検索機能を追加
   - ページネーションを実装

3. **本番向けDBに変更**
   - MySQL、PostgreSQLへの接続
   - `application.properties`の変更だけでOK

4. **ログを読む**
   - コンソールに出力されるSQL文を確認
   - Hibernateの動作を理解

## 🔧 設定ファイル (application.properties)

```properties
# H2データベース設定
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2コンソール有効化
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# HikariCP接続プール設定
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

## 学習の進め方

1. **アプリケーションを起動**
   - まずは起動してダッシュボードを確認

2. **接続状態を確認**
   - 「接続テスト」ボタンで実際の接続を確認
   - 接続情報を見てJDBC URLの構造を理解

3. **コネクションプールを観察**
   - ページをリロードして接続数の変化を観察
   - 5秒ごとの自動更新で動的な変化を確認

4. **SQLクエリを実行**
   - 簡単なSELECT文から始める
   - テーブル一覧を確認するクエリを実行
   - ユーザーテーブルを検索

5. **CRUD操作を試す**
   - ユーザーを追加
   - 一覧で確認
   - 削除してみる

6. **H2コンソールで確認**
   - 直接SQLを実行
   - テーブル構造を確認
   - より複雑なクエリを試す

## 学習ポイント

### データベース接続の仕組み
- `DataSource`がコネクションの管理を担当
- `Connection`オブジェクトを通じてデータベースと通信
- 使い終わったコネクションは必ずクローズ(HikariCPが自動管理)

### コネクションプール
- 毎回新しい接続を作成するのは重い処理
- プールで接続を再利用することで性能向上
- アクティブ/アイドル接続の管理が重要

### Spring Bootの自動設定
- `spring-boot-starter-jdbc`で必要な設定が自動的に行われる
- `application.properties`で簡単にカスタマイズ可能
- HikariCPがデフォルトのコネクションプール

## REST API エンドポイント

- `GET /api/database/info` - データベース接続情報
- `GET /api/database/pool` - コネクションプール状態
- `GET /api/database/test` - 接続テスト
- `GET /api/database/tables` - テーブル一覧
- `POST /api/database/query` - SQLクエリ実行
- `GET /api/database/users` - ユーザー一覧
- `POST /api/database/users` - ユーザー作成
- `DELETE /api/database/users/{id}` - ユーザー削除

## サンプルSQLクエリ

```sql
-- 全ユーザーを取得
SELECT * FROM USERS;

-- ユーザー数をカウント
SELECT COUNT(*) FROM USERS;

-- 全テーブルを表示
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC';

-- テーブルのカラム情報を表示
SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='USERS';
```

## 技術スタック

- Java 21
- Spring Boot 3.5.7
- Spring Data JPA
- HikariCP (コネクションプール)
- H2 Database (インメモリDB)
- Thymeleaf
- Maven


