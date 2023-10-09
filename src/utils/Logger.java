package utils;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Logger {

  public static final int TRACE = 1;
  public static final int INFO = 2;
  public static final int WARN = 3;

  final Class<?> clazz;
  volatile int level = INFO;

  final PrintStream stream;

  public Logger(Class<?> clazz, PrintStream stream) {
    this.clazz = clazz;
    this.stream = stream;
  }

  public void trace(String format, Object... args) {
    if (level >= INFO) {
      stream.printf("[%s] %s (%s): %s\n", LocalDateTime.now(), "TRACE", clazz.getName(), format.formatted(args));
    }
  }

  public void info(String format, Object... args) {
    if (level >= INFO) {
      stream.printf("[%s] %s (%s): %s\n", LocalDateTime.now(), "INFO", clazz.getName(), format.formatted(args));
    }
  }

  public void warn(String format, Object... args) {
    if (level >= INFO) {
      stream.printf("[%s] %s (%s): %s\n", LocalDateTime.now(), "WARN", clazz.getName(), format.formatted(args));
    }
  }

  public static class Factory {

    static final Map<String, Logger> store = new ConcurrentHashMap<>();

    public static Logger getLogger(Class<?> clazz) {
      return store.computeIfAbsent(clazz.getName(), clazz1 -> new Logger(clazz, System.out));
    }

    public static void setLevel(Class<?> clazz, int level) {
      getLogger(clazz).level = level;
    }
  }
}
