package raisetech.StudentManagement.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {


  @ExceptionHandler(StudentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleStudentNotFound(StudentNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        "Not Found",
        List.of(ex.getMessage())
    );
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
    String message = ex.getConstraintViolations().stream()
        .map(v -> v.getMessage())
        .findFirst()
        .orElse("リクエストパラメータに問題があります");

    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        List.of(message)
    );

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst()
        .orElse("入力値に不正があります");

    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Validation Failed",
        List.of(message)
    );

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String param = ex.getName(); // パラメータ名（例: "from"）
    String value = String.valueOf(ex.getValue()); // 入力値（例: "おはよう"）
    String requiredType = ex.getRequiredType() != null
        ? ex.getRequiredType().getSimpleName()
        : "適切な型";

    String message = String.format(
        "パラメータ '%s' に渡された値 '%s' は %s 型（日付など）に変換できません。",
        param, value, requiredType
    );

    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        List.of(message)
    );

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  // その他（５００番サーバーエラー）
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Unexpected Error",
        List.of(ex.getMessage())
    );
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParams(
      MissingServletRequestParameterException ex) {
    String name = ex.getParameterName(); // 例: "max"
    String message = String.format("リクエストパラメータ '%s' が不足しています。", name);

    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Missing Parameter",
        List.of(message)
    );
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Invalid Argument",
        List.of(ex.getMessage())
    );
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PracticeException.class)
  public ResponseEntity<ErrorResponse> handlePracticeException(PracticeException ex) {
    ErrorResponse error = new ErrorResponse(404, "Not Found", List.of(ex.getMessage()));
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

}
