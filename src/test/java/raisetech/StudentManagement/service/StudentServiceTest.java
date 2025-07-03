package raisetech.StudentManagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.StudentManagement.controller.converter.StudentConverter;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentApplicationStatus;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  @Mock
  private StudentRepository repository;

  @Mock
  private StudentConverter converter;

  private StudentService sut;

  @BeforeEach
  void before() {
    sut = new StudentService(repository, converter);
  }

  @Test
  void 受講生詳細の一覧検索_リポジトリとコンバーターの処理が適切に呼び出せていること() {
    //事前準備
    List<Student> studentList = new ArrayList<>();
    List<StudentCourse> studentCourseList = new ArrayList<>();
    List<StudentApplicationStatus> studentApplicationStatusList = new ArrayList<>();

    when(repository.search()).thenReturn(studentList);
    when(repository.searchStudentCourseList()).thenReturn(studentCourseList);
    when(repository.searchStudentApplicationStatus()).thenReturn(studentApplicationStatusList);

    sut.searchStudentList();

    verify(repository, times(1)).search();
    verify(repository, times(1)).searchStudentCourseList();
    verify(converter, times(1)).convertStudentDetails(studentList, studentCourseList,
        studentApplicationStatusList);
  }

  @Test
  void 受講生詳細検索＿リポジトリの処理が適切に呼び出せており受講生詳細のオブジェクトが正確に返されていること() {
    Student student = new Student("777", "田中太郎", "タナカタロウ", "タロ",
        "tokiwa@example.com", "名古屋", 18, "男性", "とても頑張ります", false);
    List<StudentCourse> studentCourses = new ArrayList<>();
    List<StudentApplicationStatus> studentApplicationStatuses = new ArrayList<>();

    StudentCourse studentCourse = new StudentCourse("99", "777", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 00, 00, 00),
        LocalDateTime.of(2026, 3, 31, 00, 00, 00));
    studentCourses.add(studentCourse);

    StudentApplicationStatus studentApplicationStatus = new StudentApplicationStatus(555,
        1234, "受講終了");
    studentApplicationStatuses.add(studentApplicationStatus);

    when(repository.searchStudent("777")).thenReturn(student);
    when(repository.searchStudentCourse(student.getId())).thenReturn(studentCourses);

    StudentDetail expected = new StudentDetail(student, studentCourses);
    StudentDetail actual = sut.searchStudent("777");

    verify(repository, times(1)).searchStudent("777");
    verify(repository, times(1)).searchStudentCourse(student.getId());
    assertEquals(expected, actual);
  }

  @Test
  void 受講生登録＿リポジトリと受講生コース情報を登録する際の初期情報を設定する処理及び登録情報をまとめた受講生詳細のオブジェクトが正確に返されていること() {
    Student student = new Student("777", "田中太郎", "タナカタロウ", "タロ",
        "tokiwa@example.com", "名古屋", 18, "男性", "とても頑張ります", false);
    List<StudentCourse> studentCourses = new ArrayList<>();

    StudentCourse studentCourse = new StudentCourse("99", "777", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 00, 00, 00),
        LocalDateTime.of(2026, 3, 31, 00, 00, 00));

    studentCourses.add(studentCourse);

    StudentDetail expected = new StudentDetail(student, studentCourses);
    StudentDetail actual = sut.registerStudent(new StudentDetail(student, studentCourses));

    verify(repository, times(1)).registerStudent(student);
    verify(repository, times(1)).registerStudentCourse(studentCourse);
    assertEquals(expected, actual);
  }

  @Test
  void 受講生情報の更新＿リポジトリの処理が適切に呼び出せていること() {
    Student student = new Student("777", "田中太郎", "タナカタロウ", "タロ",
        "tokiwa@example.com", "名古屋", 18, "男性", "とても頑張ります", false);
    List<StudentCourse> studentCourses = new ArrayList<>();

    StudentCourse studentCourse = new StudentCourse("99", "777", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 00, 00, 00),
        LocalDateTime.of(2026, 3, 31, 00, 00, 00));

    studentCourses.add(studentCourse);
    StudentDetail studentDetail = new StudentDetail(student, studentCourses);

    sut.updateStudent(studentDetail);

    verify(repository, times(1)).updateStudent(student);
    verify(repository, times(1)).updateStudentCourse(studentCourse);
  }

  @Test
  void 受講生詳細の登録＿初期化処理が行われること() {
    String id = "999";
    Student student = new Student();
    student.setId(id);
    StudentCourse studentCourse = new StudentCourse();

    sut.initStudentsCourse(studentCourse, student.getId());

    assertEquals(id, studentCourse.getStudentId());
    assertEquals(LocalDateTime.now().getHour(),
        studentCourse.getCourseStartAt().getHour());
    assertEquals(LocalDateTime.now().plusYears(1).getYear(),
        studentCourse.getCourseEndAt().getYear());
  }
}