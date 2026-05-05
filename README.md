# Image Edge Scanner

這是一個 Java Maven 專案，會把 `Input` 資料夾中的 `.png` 圖片做邊緣偵測（Sobel），並輸出到 `Output` 資料夾。

## 功能
- 讀取 `Input` 中所有 `.png`（不含子資料夾）
- 使用 Sobel 邊緣偵測
- 將結果輸出為 `.png` 到 `Output`（檔名保持一致）

## 需求
- Java 17+
- Maven 3.8+

## 執行
在專案根目錄執行：

```bash
mvn compile exec:java
```

程式會自動建立 `Input` 與 `Output` 資料夾（若不存在）。

## 自訂輸入/輸出路徑
可傳入兩個參數：
1. 輸入資料夾路徑
2. 輸出資料夾路徑

```bash
mvn compile exec:java -Dexec.args="customInput customOutput"
```

## 打包
```bash
mvn package
```

打包後可執行：

```bash
java -jar target/image-edge-scanner-1.0.0.jar
```
