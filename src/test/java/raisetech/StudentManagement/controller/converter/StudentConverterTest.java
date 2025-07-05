package raisetech.StudentManagement.controller.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentApplicationStatus;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;

class StudentConverterTest {

  private StudentConverter converter;

  @BeforeEach
  void setUp() {
    converter = new StudentConverter();
  }

  @Test
  void 受講生とその受講生のコース情報およびコース申し込み状況が紐づくこと() {
    Student student1 = new Student("1", "田中太郎", "タナカタロウ", "タロ",
        "taro@example.com", "東京", 35, "男性", "", false);

    Student student2 = new Student("2", "山本彩夏", "ヤマモトアヤカ", "アヤ",
        "aya@example.com", "愛知", 25, "女性", "", false);
    List<Student> studentList = List.of(student1, student2);

    StudentCourse course1 = new StudentCourse(5, "1", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 0, 0),
        LocalDateTime.of(2026, 3, 31, 0, 0));

    StudentCourse course2 = new StudentCourse(6, "1", "AWSコース",
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2026, 3, 31, 0, 0));

    StudentCourse course3 = new StudentCourse(7, "2", "フロントエンドコース",
        LocalDateTime.of(2025, 7, 1, 0, 0),
        LocalDateTime.of(2026, 3, 31, 0, 0));
    List<StudentCourse> studentCourseList = List.of(course1, course2, course3);

    StudentApplicationStatus status1 = new StudentApplicationStatus(1, 5, "仮申込");
    StudentApplicationStatus status2 = new StudentApplicationStatus(2, 6, "本申込");
    StudentApplicationStatus status3 = new StudentApplicationStatus(3, 7, "受講中");
    List<StudentApplicationStatus> studentApplicationStatusList = List.of(status1, status2,
        status3);

    List<StudentDetail> result = converter.convertStudentDetails(studentList, studentCourseList,
        studentApplicationStatusList);

    assertEquals(2, result.size());

    StudentDetail detail1 = result.getFirst();
    assertEquals(student1, detail1.getStudent());
    assertEquals(2, detail1.getStudentCourseList().size());
    assertEquals(2, detail1.getStudentApplicationStatus().size());

    StudentDetail detail2 = result.get(1);
    assertEquals(student2, detail2.getStudent());
    assertEquals(1, detail2.getStudentCourseList().size());
    assertEquals(1, detail2.getStudentApplicationStatus().size());

  }


  @Test
  void 受講生のリストが空のとき戻り値も空のリストになること() {
    List<Student> studentList = new ArrayList<>();
    List<StudentCourse> studentCourseList = getStudentCourses();

    List<StudentApplicationStatus> studentApplicationStatusList = getStudentApplicationStatuses();

    List<StudentDetail> result = converter.convertStudentDetails(studentList, studentCourseList,
        studentApplicationStatusList);

    assertTrue(result.isEmpty());
  }


  @Test
  void コースリストおよびコース申込状況リストが空でも全ての受講生情報を返しコース情報と申し込み状況は空になっていること() {
    List<Student> studentList = getStudents();
    List<StudentCourse> studentCourseList = new ArrayList<>();
    List<StudentApplicationStatus> studentApplicationStatusList = new ArrayList<>();

    List<StudentDetail> result = converter.convertStudentDetails(studentList, studentCourseList,
        studentApplicationStatusList);

    assertEquals(1, result.size());
    assertTrue(result.getFirst().getStudentCourseList().isEmpty());
    assertTrue(result.getFirst().getStudentApplicationStatus().isEmpty());
  }


  @Test
  void 受講生情報のリストがnullのときNullPointerExceptionが発生すること() {
    List<StudentCourse> studentCourseList = getStudentCourses();

    List<StudentApplicationStatus> studentApplicationStatusList = getStudentApplicationStatuses();

    assertThrows(NullPointerException.class, () ->
        converter.convertStudentDetails(null, studentCourseList, studentApplicationStatusList));
  }

  @Test
  void 受講生コース情報のリストがnullのときNullPointerExceptionが発生すること() {
    List<Student> studentList = getStudents();

    List<StudentApplicationStatus> studentApplicationStatusList = getStudentApplicationStatuses();

    assertThrows(NullPointerException.class, () ->
        converter.convertStudentDetails(studentList, null, studentApplicationStatusList));
  }

  @Test
  void 受講生コース情報の申込状況リストがnullのときNullPointerExceptionが発生すること() {
    List<Student> studentList = getStudents();

    List<StudentCourse> studentCourseList = getStudentCourses();

    assertThrows(NullPointerException.class, () ->
        converter.convertStudentDetails(studentList, studentCourseList, null));
  }

  //以下は、メソッド抽出を行なったメソッドです！
  //DTOクラスのコンストラクタの呼び出しおよび、そのリスト化を行なっております！
  private List<Student> getStudents() {
    return List.of(new Student("1", "田中太郎", "タナカタロウ", "タロ",
        "taro@example.com", "東京", 35, "男性", "", false));
  }

  private List<StudentCourse> getStudentCourses() {
    return List.of(new StudentCourse(5, "1", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 0, 0),
        LocalDateTime.of(2026, 3, 31, 0, 0)));
  }

  private List<StudentApplicationStatus> getStudentApplicationStatuses() {
    return List.of(new StudentApplicationStatus(1, 5, "仮申込"));
  }
}
