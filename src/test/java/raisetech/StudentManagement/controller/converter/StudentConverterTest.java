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
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;

class StudentConverterTest {

  private StudentConverter converter;

  @BeforeEach
  void setUp() {
    converter = new StudentConverter();
  }

  @Test
  void 受講生とその受講生のコース情報が紐づくこと() {
    Student student1 = new Student("1", "田中太郎", "タナカタロウ", "タロ",
        "taro@example.com", "東京", 35, "男性", "", false);

    Student student2 = new Student("2", "山本彩夏", "ヤマモトアヤカ", "アヤ",
        "aya@example.com", "愛知", 25, "女性", "", false);
    List<Student> studentList = List.of(student1, student2);

    StudentCourse course1 = new StudentCourse("5", "1", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 00, 00),
        LocalDateTime.of(2026, 3, 31, 00, 00));

    StudentCourse course2 = new StudentCourse("6", "1", "AWSコース",
        LocalDateTime.of(2025, 5, 1, 00, 00),
        LocalDateTime.of(2026, 3, 31, 00, 00));

    StudentCourse course3 = new StudentCourse("7", "2", "フロントエンドコース",
        LocalDateTime.of(2025, 7, 1, 00, 00),
        LocalDateTime.of(2026, 3, 31, 00, 00));
    List<StudentCourse> studentCourseList = List.of(course1, course2, course3);

    List<StudentDetail> result = converter.convertStudentDetails(studentList, studentCourseList);

    assertEquals(2, result.size());

    StudentDetail detail1 = result.get(0);
    assertEquals(student1, detail1.getStudent());
    assertEquals(2, detail1.getStudentCourseList().size());

    StudentDetail detail2 = result.get(1);
    assertEquals(student2, detail2.getStudent());
    assertEquals(1, detail2.getStudentCourseList().size());
  }


  @Test
  void 受講生のリストが空のとき戻り値も空のリストになること() {
    List<Student> studentList = new ArrayList<>();
    List<StudentCourse> studentCourseList = List.of(
        new StudentCourse("5", "1", "Javaコース",
            LocalDateTime.of(2025, 4, 1, 00, 00),
            LocalDateTime.of(2026, 3, 31, 00, 00)));

    List<StudentDetail> result = converter.convertStudentDetails(studentList, studentCourseList);

    assertTrue(result.isEmpty());
  }

  @Test
  void コースリストが空でも全ての受講生情報を返しコース情報は空になっていること() {
    List<Student> studentList = List.of(new Student("1", "田中太郎", "タナカタロウ", "タロ",
        "taro@example.com", "東京", 35, "男性", "", false));
    List<StudentCourse> studentCourseList = new ArrayList<>();

    List<StudentDetail> result = converter.convertStudentDetails(studentList, studentCourseList);

    assertEquals(1, result.size());
    assertTrue(result.get(0).getStudentCourseList().isEmpty());
  }

  @Test
  void 受講生情報のリストがnullのときNullPointerExceptionが発生すること() {
    List<StudentCourse> studentCourseList = List.of(new StudentCourse("5", "1", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 00, 00),
        LocalDateTime.of(2026, 3, 31, 00, 00)));

    assertThrows(NullPointerException.class, () ->
        converter.convertStudentDetails(null, studentCourseList));
  }

  @Test
  void 受講生コース情報のリストがnullのときNullPointerExceptionが発生すること() {
    List<Student> studentList = List.of(new Student("1", "田中太郎", "タナカタロウ", "タロ",
        "taro@example.com", "東京", 35, "男性", "", false));

    assertThrows(NullPointerException.class, () ->
        converter.convertStudentDetails(studentList, null));
  }
}