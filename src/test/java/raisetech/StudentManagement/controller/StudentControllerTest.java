package raisetech.StudentManagement.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentApplicationStatus;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.exception.PracticeException;
import raisetech.StudentManagement.service.StudentService;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private StudentService service;

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void 受講生詳細の一覧検索が実行できて空のリストが返ってくること() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/studentList"))
        .andExpect(status().isOk());

    verify(service, times(1)).searchStudentList();
  }

  @Test
  void 受講生詳細の受講生で適切な値を入力した時に入力チェックに異常が発生しないこと() {
    Student student = new Student();
    student.setId("1");
    student.setName("江並公史");
    student.setKanaName("エナミコウジ");
    student.setNickname("エナミ");
    student.setEmail("test@example.com");
    student.setArea("奈良県");
    student.setSex("男性");

    Set<ConstraintViolation<Student>> violations = validator.validate(student);

    assertThat(violations.size()).isEqualTo(0);
  }

  @Test
  void 受講生詳細の受講生でIDに数字以外を用いた時に入力チェックに掛かること() {
    Student student = new Student();
    student.setId("テストです");
    student.setName("江並公史");
    student.setKanaName("エナミコウジ");
    student.setNickname("エナミ");
    student.setEmail("test@example.com");
    student.setArea("奈良県");
    student.setSex("男性");

    Set<ConstraintViolation<Student>> violations = validator.validate(student);

    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations).extracting("message")
        .containsOnly("数字のみ入力するようにしてください。");
  }


  @Test
  void 存在する受講生IDを用いて検索をし適切な受講生詳細オブジェクトが返ってくること()
      throws Exception {
    //↓ 前準備
    Student mockStudent = getStudent();

    List<StudentCourse> mockStudentCourseList = getStudentCourses();

    List<StudentApplicationStatus> mockStudentApplicationStatusList = getStudentApplicationStatuses();

    StudentDetail mockStudentDetail = new StudentDetail(mockStudent, mockStudentCourseList,
        mockStudentApplicationStatusList);
    //↑ ここまでが前準備
    when(service.searchStudent(mockStudent.getId())).thenReturn(mockStudentDetail);

    String expectedJson = """
           {
              "student": {
              "id": "1",
              "name": "田中太郎",
              "kanaName": "タナカタロウ",
              "nickname": "タロ",
              "email": "taro@example.com",
              "area": "東京",
              "age": 35,
              "sex": "男性",
              "remark": "",
              "deleted": false
        },
        "studentCourseList": [
        {
            "id": 5,
            "studentId": "1",
            "courseName": "Javaコース",
            "courseStartAt": "2025-04-01T00:00:00",
            "courseEndAt": "2026-03-31T00:00:00"
        }
        ],
        "studentApplicationStatus": [
                {
                    "id": 1,
                    "studentCourseId": 5,
                    "status": "本申込"
                }
        ]
        }""";

    mockMvc.perform(MockMvcRequestBuilders.get("/student/{id}", mockStudent.getId()))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }


  @Test
  void 存在しない受講生IDを用いて検索をしたときに適切なエラーメッセージが返ってくること()
      throws Exception {
    // 前準備
    String studentId = "999";
    when(service.searchStudent(studentId)).thenReturn(null);

    mockMvc.perform(get("/student/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message[0]").value("受講生が見つかりませんでした。ID: 999"));

  }

  @Test
  void 受講生検索をする際に数字でない文字列で検索をしたとき500番エラーを返すこと()
      throws Exception {
    // 前準備
    String invalidId = "abc";  // 数字でない

    mockMvc.perform(MockMvcRequestBuilders.get("/student/{id}", invalidId))
        .andExpect(status().isBadRequest());
  }


  @Test
  void 受講生登録が成功すること() throws Exception {
    // 前準備
    Student mockStudent = getStudent();

    List<StudentCourse> mockStudentCourseList = getStudentCourses();

    List<StudentApplicationStatus> mockStudentApplicationStatusList = getApplicationStatuses();

    StudentDetail request = new StudentDetail(mockStudent, mockStudentCourseList,
        mockStudentApplicationStatusList);
    StudentDetail response = new StudentDetail(mockStudent, mockStudentCourseList,
        mockStudentApplicationStatusList);
    //↑ ここまでが前準備

    when(service.registerStudent(any(StudentDetail.class))).thenReturn(response);

    String expectedJson = """
           {
              "student": {
              "id": "1",
              "name": "田中太郎",
              "kanaName": "タナカタロウ",
              "nickname": "タロ",
              "email": "taro@example.com",
              "area": "東京",
              "age": 35,
              "sex": "男性",
              "remark": "",
              "deleted": false
        },
        "studentCourseList": [
        {
            "id": 5,
            "studentId": "1",
            "courseName": "Javaコース",
            "courseStartAt": "2025-04-01T00:00:00",
            "courseEndAt": "2026-03-31T00:00:00"
        }
        ],
        "studentApplicationStatus": [
                {
                    "id": 1,
                    "studentCourseId": 5,
                    "status": "仮申込"
                }
        ]
        }""";
    mockMvc.perform(
            post("/registerStudent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  void 受講生情報の更新に成功すること() throws Exception {
    // 前準備（リクエスト用オブジェクトを準備）
    Student mockStudent = getStudent();

    List<StudentCourse> mockStudentCourseList = getStudentCourses();

    List<StudentApplicationStatus> mockStudentApplicationStatusList = getApplicationStatuses();

    StudentDetail request = new StudentDetail(mockStudent, mockStudentCourseList,
        mockStudentApplicationStatusList);

    doNothing().when(service).updateStudent(any(StudentDetail.class));

    mockMvc.perform(
            put("/updateStudent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(content().json("{\"message\":\"更新処理が成功しました。\"}"));
  }


  @Test
  void 強制的に例外を発生させるHTTPの実行時にレスポンスのステータスがisOkでないことを検証する()
      throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/practiceException"))
        .andExpect(status().isNotFound()) // 404エラーを期待
        .andExpect(result ->
            assertInstanceOf(PracticeException.class, result.getResolvedException()))
        .andExpect(result ->
            assertEquals("エラー発生", result.getResolvedException().getMessage()));
  }

  @Test
  void 受講生詳細の受講生コース情報で適切な値を入力した時に入力チェックに異常が発生しないこと() {
    StudentCourse studentCourse = getStudentCourse();

    Set<ConstraintViolation<StudentCourse>> violations = validator.validate(studentCourse);

    assertThat(violations.size()).isEqualTo(0);
  }

  //以下は、メソッド抽出を行なったメソッドです！　
  //DTOクラスのコンストラクタの呼び出しおよび、そのリスト化などを行なっております！
  private Student getStudent() {
    return new Student("1", "田中太郎", "タナカタロウ", "タロ",
        "taro@example.com", "東京", 35, "男性", "", false);
  }

  private List<StudentCourse> getStudentCourses() {
    StudentCourse mockStudentCourse = getStudentCourse();
    return List.of(mockStudentCourse);
  }

  private StudentCourse getStudentCourse() {
    return new StudentCourse(5, "1", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 0, 0),
        LocalDateTime.of(2026, 3, 31, 0, 0));
  }

  private List<StudentApplicationStatus> getStudentApplicationStatuses() {
    StudentApplicationStatus mockStudentApplicationStatus = new StudentApplicationStatus(1, 5,
        "本申込");
    return List.of(mockStudentApplicationStatus);
  }

  private List<StudentApplicationStatus> getApplicationStatuses() {
    StudentApplicationStatus mockStudentApplicationStatus = new StudentApplicationStatus(1, 5,
        "仮申込");
    return List.of(mockStudentApplicationStatus);
  }

  @Test
  void カナ名がアから始まる受講生の検索ができること() throws Exception {
    String prefix = "ア";
    Student student = new Student("1", "相川かずき", "アイカワカズキ", "カズ",
        "kazu@example.com", "東京", 35, "男性", "", false);

    List<StudentDetail> mockList = List.of(new StudentDetail(student, List.of(getStudentCourse()),
        getApplicationStatuses()));

    Mockito.when(service.findStudentsByNamePrefix(prefix)).thenReturn(mockList);

    mockMvc.perform(get("/starts-with/{prefix}", "ア"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].student.name").value("相川かずき"))
        .andExpect(jsonPath("$[0].student.kanaName").value("アイカワカズキ"));

  }

  @Test
  void カナ名検索時に全角カタカナ１文字以外をしようして検索をかけると例外が発生すること()
      throws Exception {
    // ひらがななどの無効な文字
    mockMvc.perform(get("/starts-with/{prefix}", "あ"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void カナ名検索時に空文字で検索をかけたら例外が発生すること() throws Exception {
    mockMvc.perform(get("/starts-with/{prefix}", " "))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 受講生の年齢検索が適切に行えること() throws Exception {
    Student student = new Student("1", "相川かずき", "アイカワカズキ", "カズ",
        "kazu@example.com", "東京", 23, "男性", "", false);

    List<StudentDetail> mockList = List.of(new StudentDetail(student, List.of(getStudentCourse()),
        getApplicationStatuses()));
    Mockito.when(service.findStudentsByAgeRange(20, 29)).thenReturn(mockList);

    mockMvc.perform(get("/age-range")
            .param("min", "20")
            .param("max", "29"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].student.id").value("1"))
        .andExpect(jsonPath("$[0].student.name").value("相川かずき"));
  }

  @Test
  void 年齢検索でminの方がmaxよりも大きい数値で検索をかけた場合に例外を発生させること()
      throws Exception {
    mockMvc.perform(get("/age-range")
            .param("min", "30")
            .param("max", "20"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 年齢検索で検索範囲の上限値_max_を指定ぜずに検索をかけた場合に例外を発生させること()
      throws Exception {
    mockMvc.perform(get("/age-range")
            .param("min", "20"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 年齢検索で数値以外のキーワードで検索をかけたら例外を発生させること() throws Exception {
    mockMvc.perform(get("/age-range")
            .param("min", "あ")
            .param("max", "29"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 年齢検索で負の数で検索をかけたら例外を発生させること() throws Exception {
    mockMvc.perform(get("/age-range")
            .param("min", "-1")
            .param("max", "29"))
        .andExpect(status().isBadRequest());
  }

}
