package raisetech.StudentManagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.exception.PracticeException;
import raisetech.StudentManagement.service.StudentService;

/**
 * 受講生の検索や登録、更新などを行うREST APIとして受け付けるControllerです。
 */
@Validated
@RestController
public class StudentController {

  private StudentService service;


  @Autowired
  public StudentController(StudentService service) {
    this.service = service;

  }

  /**
   * 受講生詳細の一覧検索です。 全件検索を行うので、条件指定は行いません。
   *
   * @return 受講生詳細一覧（全件）
   */
  @Operation(summary = "一覧検索", description = "受講生の一覧を検索します。")
  @GetMapping("/studentList")
  public List<StudentDetail> getStudentList() {
    return service.searchStudentList();
  }

  /**
   * 意図的に例外を発生させる練習用のメソッドです。
   *
   * @return 例外を発生させる練習用のメソッドのため、特にございません。
   * @throws PracticeException
   */
  @Operation(summary = "例外処理練習用API", description = "発生した例外を受け取り、例外処理を行います。")
  @GetMapping("/practiceException")
  public StudentDetail getPracticeException() throws PracticeException {
    throw new PracticeException("エラー発生");
  }


  /**
   * 受講生詳細の検索です。 IDに紐づく任意の受講生の情報を取得します。
   *
   * @param id 受講生ID
   * @return 受講生
   */
  @Operation(
      summary = "受講生検索",
      description = "受講生IDを指定して受講生の情報を取得します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に受講生情報を取得しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正なID形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "ID形式エラー",
                      description = "IDが数値でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "IDの形式が不正です。数値のIDを指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定されたIDの受講生が見つかりません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "受講生未登録",
                      description = "指定したIDの受講生が存在しない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "受講生が見つかりませんでした。ID: 1234",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "id",
      description = "受講生ID（数値のみ）",
      required = true,
      example = "123")
  @GetMapping("/student/{id}")
  public StudentDetail getStudent(@PathVariable @NotBlank @Pattern(regexp = "^\\d+$") String id) {
    //throw new TestException(id + "番の人に対してエラーが発生しました");
    StudentDetail student = service.searchStudent(id);
    if (student == null) {
      throw new PracticeException("受講生が見つかりませんでした。ID: " + id);
    }
    return student;
    //return service.searchStudent(id);
  }


  /**
   * 受講生詳細の登録を行います。
   *
   * @param studentDetail 受講生詳細
   * @return 実行結果
   */
  @Operation(
      summary = "受講生登録",
      description = "新しい受講生を登録します。",
      tags = {"受講生管理"},
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に登録されました。"),
          @ApiResponse(responseCode = "400",
              description = "入力データが不正です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "ID形式エラー",
                      description = "IDが数値でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "IDの形式が不正です。数値のIDを指定してください。",
                            "code": 400
                          }"""))
          )})
  @PostMapping("/registerStudent")
  public ResponseEntity<StudentDetail> registerStudent(
      @RequestBody @Valid StudentDetail studentDetail) {
    StudentDetail responseStudentDetail = service.registerStudent(studentDetail);
    return ResponseEntity.ok(responseStudentDetail);
  }

  /**
   * 受講生詳細の更新を行います。キャンセルフラグの更新もここで行います。（論理削除）
   *
   * @param studentDetail 受講生詳細
   * @return 実行結果
   */
  @Operation(summary = "受講生更新", description = "受講生の情報を更新します。")
  @PutMapping("/updateStudent")
  public ResponseEntity<String> updateStudent(@RequestBody @Valid StudentDetail studentDetail) {
    service.updateStudent(studentDetail);
    return ResponseEntity.ok("更新処理が成功しました。");
  }

  /*@ExceptionHandler(TestException.class)
  public ResponseEntity<String> handleTestException(TestException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }*/

}
