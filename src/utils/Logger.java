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

  volatile Level level = Level.OFF;
  final Class<?> clazz;

  public Logger(Class<?> clazz) {
    this.clazz = clazz;
  }


  public void trace(String format, Object... args) {
  }

  public void info(String format, Object... args) {
  }

  public void warn(String format, Object... args) {
  }

  public void error(String format, Object... args) {
  }

  static class PrintStreamLogger extends Logger {

    PrintStream printStream;

    public PrintStreamLogger(Class<?> clazz) {
      super(clazz);
    }

    public void trace(String format, Object... args) {
      if (level.val <= Level.TRACE.val) {
        log("TRACE", format.formatted(args));
      }
    }

    public void info(String format, Object... args) {
      if (level.val <= Level.INFO.val) {
        log("INFO", format.formatted(args));
      }
    }

    public void warn(String format, Object... args) {
      if (level.val <= Level.WARN.val) {
        log("WARN", format.formatted(args));
      }
    }

    public void error(String format, Object... args) {
      if (level.val <= Level.ERROR.val) {
        log("ERROR", format.formatted(args));
      }
    }

    private void log(String level, String message) {
      if (Objects.isNull(printStream)) {
        printStream = getPrintStream();
      }
      printStream.printf("%s (%s): %s\n", level, clazz.getSimpleName(), message);
    }

    private PrintStream getPrintStream() {
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
  }

  public enum Level {
    TRACE(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    OFF(5),
    ;

    final int val;

    Level(int val) {
      this.val = val;
    }
  }

  public static class Factory {

    static final Map<String, Logger> store = new ConcurrentHashMap<>();

    public static Logger getLogger(Class<?> clazz) {
      return store.computeIfAbsent(clazz.getName(), clazz1 -> new PrintStreamLogger(clazz));
    }

    public static void setLevel(String clazz, String level) {
      var logger = store.get(clazz);
      if (Objects.nonNull(logger)) {
        logger.level = Level.valueOf(level);
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
