package raisetech.StudentManagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
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
    Student student = getStudent();

    List<StudentCourse> studentCourses = getStudentCourses();

    List<StudentApplicationStatus> studentApplicationStatuses = getStudentApplicationStatuses();

    when(repository.searchStudent("777")).thenReturn(student);
    when(repository.searchStudentCourse(student.getId())).thenReturn(studentCourses);
    when(repository.searchStudentApplicationStatus()).thenReturn(studentApplicationStatuses);

    StudentDetail expected = new StudentDetail(student, studentCourses, studentApplicationStatuses);
    StudentDetail actual = sut.searchStudent("777");

    verify(repository, times(1)).searchStudent("777");
    verify(repository, times(1)).searchStudentCourse(student.getId());
    verify(repository, times(1)).searchStudentApplicationStatus();

    assertEquals(expected, actual);
  }

  @Test
  void 受講生登録＿初期情報設定と登録情報が正しく返されること() {
    Student student = getStudent();

    List<StudentCourse> studentCourses = new ArrayList<>();
    StudentCourse studentCourse = new StudentCourse(99, "777", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 0, 0, 0),
        LocalDateTime.of(2026, 3, 31, 0, 0, 0));
    studentCourses.add(studentCourse);

    StudentDetail request = new StudentDetail(student, studentCourses, List.of());

    StudentDetail actual = sut.registerStudent(request);

    verify(repository).registerStudent(student);
    verify(repository).registerStudentCourse(studentCourse);
    verify(repository).registerApplicationStatus(
        argThat(status ->
            status.getStudentCourseId().equals(99) &&
                status.getStatus().equals("仮申込"))
    );

    assertEquals(student, actual.getStudent());
    assertEquals(studentCourses, actual.getStudentCourseList());

    StudentApplicationStatus actualStatus = actual.getStudentApplicationStatus().get(0);
    assertEquals(99, actualStatus.getStudentCourseId());
    assertEquals("仮申込", actualStatus.getStatus());
    assertNull(actualStatus.getId()); // 自動採番なのでnullのまま
  }

  @Test
  void 受講生情報の更新＿リポジトリの処理が適切に呼び出せていること() {
    Student student = getStudent();

    List<StudentCourse> studentCourses = new ArrayList<>();
    StudentCourse studentCourse = new StudentCourse(99, "777", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 0, 0, 0),
        LocalDateTime.of(2026, 3, 31, 0, 0, 0));
    studentCourses.add(studentCourse);

    List<StudentApplicationStatus> studentApplicationStatuses = new ArrayList<>();
    StudentApplicationStatus studentApplicationStatus = new StudentApplicationStatus(555,
        1234, "受講終了");
    studentApplicationStatuses.add(studentApplicationStatus);

    StudentDetail studentDetail = new StudentDetail(student, studentCourses,
        studentApplicationStatuses);

    sut.updateStudent(studentDetail);

    verify(repository, times(1)).updateStudent(student);
    verify(repository, times(1)).updateStudentCourse(studentCourse);
    verify(repository, times(1)).updateApplicationStatus(studentApplicationStatus);
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


  //以下は、メソッド抽出を行なったメソッドです！　
  //DTOクラスのコンストラクタの呼び出しおよび、そのリスト化などを行なっております！
  private Student getStudent() {
    return new Student("777", "田中太郎", "タナカタロウ", "タロ",
        "tokiwa@example.com", "名古屋", 18, "男性", "とても頑張ります", false);
  }

  private List<StudentCourse> getStudentCourses() {
    List<StudentCourse> studentCourses = new ArrayList<>();
    StudentCourse studentCourse = new StudentCourse(99, "777", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 0, 0, 0),
        LocalDateTime.of(2026, 3, 31, 0, 0, 0));
    studentCourses.add(studentCourse);
    return studentCourses;
  }

  private List<StudentApplicationStatus> getStudentApplicationStatuses() {
    List<StudentApplicationStatus> studentApplicationStatuses = new ArrayList<>();
    StudentApplicationStatus studentApplicationStatus = new StudentApplicationStatus(555,
        99, "受講終了");
    studentApplicationStatuses.add(studentApplicationStatus);
    return studentApplicationStatuses;
  }

}
