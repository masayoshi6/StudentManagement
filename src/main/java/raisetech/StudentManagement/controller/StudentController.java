package raisetech.StudentManagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.exception.PracticeException;
import raisetech.StudentManagement.service.StudentService;

/**
 * 受講生の検索や登録、更新などを行うREST APIとして受け付けるControllerです。
 */
@Slf4j
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
    StudentDetail student = service.searchStudent(id);
    if (student == null) {
      throw new PracticeException("受講生が見つかりませんでした。ID: " + id);
    }
    return student;
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
  public ResponseEntity<Map<String, String>> updateStudent(
      @RequestBody @Valid StudentDetail studentDetail) {
    service.updateStudent(studentDetail);
    Map<String, String> response = new HashMap<>();
    response.put("message", "更新処理が成功しました。");

    return ResponseEntity.ok(response);
  }

  /**
   * 受講生の名前一覧検索です。引数で受け取ったカタカナから始まる受講生の一覧を取得します。
   *
   * @param prefix 受講生のカナ名の１文字目（カタカナとします）
   * @return 引数のカタカナから始まる名前の受講生のリスト
   */
  @Operation(
      summary = "受講生カナ名検索",
      description = "カナ名の先頭１文字を指定して受講生の情報を取得します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に受講生情報を取得しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "カナ名の形式エラー",
                      description = "カナ名の先頭１文字が全角カタカナでない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "カナ名の形式が不正です。全角カタカナ１文字を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定されたカタカナから始まる名前の受講生が見つかりません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "受講生未登録",
                      description = "指定したカタカナから始まる名前の受講生が存在しない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "受講生が見つかりませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "prefix",
      description = "受講生カナ名先頭１文字（全角カタカナのみ）",
      required = true,
      example = "ア")
  @GetMapping("/starts-with/{prefix}")
  public List<StudentDetail> getStudentsStartingWith(
      @PathVariable @NotBlank
      @Pattern(regexp = "^[\\u30A1-\\u30F6]$", message = "全角カタカナ1文字（ア～ン）で入力してください")
      String prefix) {

    return service.findStudentsByNamePrefix(prefix);

  }

  /**
   * 受講生の年齢検索です。引数で受け取った２つの整数の範囲内の年齢をもつ受講生の一覧を取得します。
   *
   * @param minAge 年齢検索の下限
   * @param maxAge 年齢検索の上限
   * @return minAge以上 maxAge以下の年齢の受講生のリスト
   */
  @Operation(
      summary = "受講生年齢検索",
      description = "年齢を指定して受講生の情報を取得します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に受講生情報を取得しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "年齢の形式エラー",
                      description = "年齢が数値でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "年齢の形式が不正です。０以上の整数を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定された年齢区間の受講生が見つかりません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "受講生未登録",
                      description = "指定した年齢区間の受講生が存在しない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定年齢の受講生は見つかりませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      description = "受講生の年齢区間（０以上の整数のみ）",
      required = true,
      example = "30")
  @GetMapping("/age-range")
  public List<StudentDetail> getStudentsByAgeRange(
      @RequestParam(name = "min") @NotNull @Min(0) int minAge,//intやInteger型に@NotBlankは使えない！
      @RequestParam(name = "max") @NotNull @Min(0) int maxAge
  ) {
    if (minAge > maxAge) {
      throw new IllegalArgumentException("min は max 以下である必要があります");
    }

    return service.findStudentsByAgeRange(minAge, maxAge);

  }

  /**
   * 受講生の性別検索です。「男性」または「女性」の受講生一覧を取得します。
   *
   * @param sex 受講生の性別
   * @return 「男性」または「女性」の受講生のリスト
   */
  @Operation(
      summary = "受講生性別検索",
      description = "性別を指定して受講生の情報を取得します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に受講生情報を取得しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "性別の形式エラー",
                      description = "性別が「男性」または「女性」でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "性別の形式が不正です。「男性」または「女性」を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定された性別の受講生が見つかりません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "受講生未登録",
                      description = "指定した性別の受講生が存在しない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定性別の受講生は見つかりませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "sex",
      description = "受講生の性別「男性」または「女性」のみ）",
      required = true,
      example = "男性")
  @GetMapping("/gender-select/{sex}")
  public List<StudentDetail> getStudentByGender(@PathVariable @NotBlank String sex) {

    return service.findStudentsByGender(sex);
  }

  /**
   * コース名を指定し、そのコースに所属している受講生一覧を返すメソッドです。
   *
   * @param courseName 検索したいコース名
   * @return 指定したコースに所属している受講生一覧
   */
  @Operation(
      summary = "受講生コース検索",
      description = "所属コース名を指定して受講生の情報を取得します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に受講生情報を取得しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "コース名の形式エラー",
                      description = "存在しないコース名を指定した場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "コース名の形式が不正です。存在するコース名を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定されたコース所属する受講生が見つかりません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "受講生未登録",
                      description = "指定したコースに所属している受講生が存在しない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定コースに所属する受講生は見つかりませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "courseName",
      description = "受講生が所属しているコース名）",
      required = true,
      example = "Javaコース")
  @GetMapping("/courseName/{courseName}")
  public List<StudentDetail> getStudentsByCourse(@PathVariable @NotBlank String courseName) {

    return service.findStudentsByCourse(courseName);
  }

  /**
   * コースの受講開始日で受講生検索を行うメソッドです。 引数に指定する２つの日付の間に受講開始となる受講生詳細のリストを返します。 (例：2025-04-01 から 2025-05-31
   * の間のいずれかの日に受講開始となる受講生を検索する、などです。）
   *
   * @param from 検索区間の始まりの日
   * @param to   検索区間の終わりの日
   * @return 該当区間の間に受講が開始される受講生詳細のリスト
   */
  @Operation(
      summary = "受講生コース開始日検索",
      description = "所属コースの開始日の区間を指定してその区間内に受講がスタートする受講生の情報を取得します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に受講生情報を取得しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "受講開始日の形式エラー",
                      description = "受講開始日に日付以外（例：おはよう）を指定した場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "パラメータ from に渡された値 'おはよう' は LocalDate型に変換できません。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定された期間内に受講がスタートする受講生が見つかりません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "受講生未登録",
                      description = "指定した期間内に受講がスタートする受講生が存在しない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定期間内に受講がスタートする受講生は見つかりませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "from",
      description = "（受講開始日）検索区間の始まりの日",
      required = true,
      example = "2025-04-01")
  @GetMapping("/courses/start-date")
  public List<StudentDetail> getStudentsByCourseStartDateRange(
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
  ) {
    return service.findStudentsByCourseStartDateRange(from.atStartOfDay(), to.atTime(23, 59, 59));
  }

  /**
   * 申込状況から受講生を検索するメソッドです。
   *
   * @param status 仮申込、本申込、受講中、受講終了のいずれか
   * @return 該当する受講生詳細のリスト
   */
  @Operation(
      summary = "受講生コース申込状況検索",
      description = "コースの申込状況で検索をし、該当申込状況のコースを持つ受講生の情報を取得します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に受講生情報を取得しました。"),
          @ApiResponse(
              responseCode = "404",
              description = "適切な申し込み状況を入力してください。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "受講生未登録",
                      description = "仮申込、本申込、受講中、受講終了のいずれかでもないキーワードで検索をかけた場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "適切な申し込み状況を入力してください。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "from",
      description = "（受講開始日）検索区間の始まりの日",
      required = true,
      example = "2025-04-01")
  @GetMapping("/status/{status}")
  public List<StudentDetail> getStudentsByStatus(@PathVariable @NotBlank String status) {

    return service.findStudentsByStatus(status);
  }

}
