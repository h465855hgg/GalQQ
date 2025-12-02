package top.galqq.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AI日志管理器 - 持久化AI请求/错误日志
 */
public class AiLogManager {
    private static final String TAG = "GalQQ.AiLog";
    private static final String LOG_FILE_NAME = "ai_requests.log";
    private static final int MAX_LOG_SIZE = 1024 * 1024; // 1MB
    
    private static File getLogFile(Context context) {
        File logDir = new File(context.getFilesDir(), "galqq_logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        return new File(logDir, LOG_FILE_NAME);
    }
    
    /**
     * 添加日志
     */
    public static synchronized void addLog(Context context, String message) {
        try {
            File logFile = getLogFile(context);
            
            // 检查文件大小，超过限制则清空
            if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
                clearLogs(context);
            }
            
            // 添加时间戳
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            String logEntry = "[" + timestamp + "] " + message + "\n---\n";
            
            // 追加到文件
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write(logEntry.getBytes());
            fos.close();
            
            Log.d(TAG, "日志已记录: " + message);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write log", e);
        }
    }
    
    /**
     * 添加AI请求失败日志
     */
    public static void logAiError(Context context, String provider, String model, 
                                   String url, String error) {
        StringBuilder sb = new StringBuilder();
        sb.append("AI请求失败\n");
        sb.append("Provider: ").append(provider).append("\n");
        sb.append("Model: ").append(model).append("\n");
        sb.append("URL: ").append(url).append("\n");
        sb.append("Error: ").append(error);
        
        addLog(context, sb.toString());
    }
    
    /**
     * 添加AI请求成功日志
     */
    public static void logAiSuccess(Context context, String provider, String model, 
                                     String userMessage, int optionsCount) {
        logAiSuccess(context, provider, model, userMessage, optionsCount, null);
    }
    
    /**
     * 添加AI请求成功日志（带完整响应）
     */
    public static void logAiSuccess(Context context, String provider, String model, 
                                     String userMessage, int optionsCount, String fullResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("AI请求成功\n");
        sb.append("Provider: ").append(provider).append("\n");
        sb.append("Model: ").append(model).append("\n");
        sb.append("Message: ").append(userMessage.substring(0, Math.min(50, userMessage.length()))).append("...\n");
        sb.append("生成选项数: ").append(optionsCount);
        
        // 如果启用了详细日志且有完整响应，则记录
        if (fullResponse != null && !fullResponse.isEmpty()) {
            sb.append("\n\n=== AI完整响应 ===\n");
            sb.append(fullResponse);
            sb.append("\n=== 响应结束 ===");
        }
        
        addLog(context, sb.toString());
    }
    
    /**
     * 获取所有日志
     */
    public static String getLogs(Context context) {
        try {
            File logFile = getLogFile(context);
            if (!logFile.exists()) {
                return "暂无日志";
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(logFile)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            
            if (sb.length() == 0) {
                return "暂无日志";
            }
            
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read logs", e);
            return "读取日志失败: " + e.getMessage();
        }
    }
    
    /**
     * 清除所有日志
     */
    public static synchronized void clearLogs(Context context) {
        try {
            File logFile = getLogFile(context);
            if (logFile.exists()) {
                logFile.delete();
            }
            Log.d(TAG, "日志已清除");
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear logs", e);
        }
    }
    
    /**
     * 获取日志文件路径（用于调试）
     */
    public static String getLogFilePath(Context context) {
        return getLogFile(context).getAbsolutePath();
    }
}
