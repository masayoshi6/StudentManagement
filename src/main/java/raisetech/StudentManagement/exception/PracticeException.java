package raisetech.StudentManagement.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//@ControllerAdvice
@RestControllerAdvice
public class PracticeException extends RuntimeException {

  public PracticeException() {
    super();
  }

  public PracticeException(String message) {
    super(message);
  }

  public PracticeException(String message, Throwable cause) {
    super(message, cause);
  }

  public PracticeException(Throwable cause) {
    super(cause);
  }


  // 受講生が見つからなかった場合
  @ExceptionHandler(PracticeException.class)
  public ResponseEntity<String> handleStudentNotFound(PracticeException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  // バリデーションエラー（@Patternなど）
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
    return new ResponseEntity<>("IDは半角数字で入力してください。", HttpStatus.BAD_REQUEST);
  }

  // その他の予期せぬエラー
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleOtherExceptions(Exception ex) {
    return new ResponseEntity<>("サーバーエラーが発生しました。", HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
