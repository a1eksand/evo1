package utils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Logger {

  final Class<?> clazz;
  volatile Level level = Level.WARN;
  PrintStream printStream;

  public Logger(Class<?> clazz) {
    this.clazz = clazz;
  }

  public void trace(String format, Object... args) {
    if (level.val <= Level.TRACE.val) {
      getPrintStream().printf("[%s] %s (%s): %s\n", LocalDateTime.now(), "TRACE", clazz.getName(), format.formatted(args));
    }
  }

  public void info(String format, Object... args) {
    if (level.val <= Level.INFO.val) {
      getPrintStream().printf("[%s] %s (%s): %s\n", LocalDateTime.now(), "INFO", clazz.getName(), format.formatted(args));
    }
  }

  public void warn(String format, Object... args) {
    if (level.val <= Level.WARN.val) {
      getPrintStream().printf("[%s] %s (%s): %s\n", LocalDateTime.now(), "WARN", clazz.getName(), format.formatted(args));
    }
  }

  PrintStream getPrintStream() {
    if (Objects.isNull(printStream)) {
      synchronized (this) {
        if (Objects.isNull(printStream)) {
          try {
            printStream = new PrintStream(Files.newOutputStream(Path.of(clazz.getSimpleName() + "." + System.currentTimeMillis() + ".log")));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    return printStream;
  }

  public enum Level {
    TRACE(1),
    INFO(2),
    WARN(3),
    ;

    final int val;

    Level(int val) {
      this.val = val;
    }
  }

  public static class Factory {

    static final Map<String, Logger> store = new ConcurrentHashMap<>();

    public static Logger getLogger(Class<?> clazz) {
      return store.computeIfAbsent(clazz.getName(), clazz1 -> new Logger(clazz));
    }

    public static void setLevel(String clazz, String level) {
      var log = store.get(clazz);
      if (Objects.nonNull(log)) {
        log.level = Level.valueOf(level);
      }
    }

    public static List<String> getLevels() {
      return store
          .entrySet()
          .stream()
          .sorted(Map.Entry.comparingByKey())
          .map(e -> "%s: %s".formatted(e.getKey(), e.getValue().level))
          .toList();
    }
  }
}
