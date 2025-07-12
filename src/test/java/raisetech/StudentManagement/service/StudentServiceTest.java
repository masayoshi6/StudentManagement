package raisetech.StudentManagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
import raisetech.StudentManagement.exception.StudentNotFoundException;
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

  @Test
  void カナ名プレフィックスで受講生が見つかるときStudentDetailリストを返すこと() {
    String prefix = "ア";
    List<Student> students = List.of(
        new Student("1", "相川かずき", "アイカワカズキ", "カズ", "kazu@example.com", "東京", 25,
            "男性", "", false));
    List<StudentCourse> courses = List.of(new StudentCourse(1, "1", "Java", null, null));
    List<StudentApplicationStatus> statuses = List.of(new StudentApplicationStatus(1, 1, "仮申込"));
    List<StudentDetail> expectedDetails = List.of(mock(StudentDetail.class));

    when(repository.findStudentsByNamePrefix(prefix)).thenReturn(students);
    when(repository.searchStudentCourseList()).thenReturn(courses);
    when(repository.searchStudentApplicationStatus()).thenReturn(statuses);
    when(converter.convertStudentDetails(students, courses, statuses)).thenReturn(expectedDetails);

    List<StudentDetail> result = sut.findStudentsByNamePrefix(prefix);

    assertThat(result).isEqualTo(expectedDetails);
    verify(repository).findStudentsByNamePrefix(prefix);
    verify(converter).convertStudentDetails(students, courses, statuses);
  }

  @Test
  void 該当する受講生が存在しない場合は例外を発生させること() {
    String prefix = "ン";
    when(repository.findStudentsByNamePrefix(prefix)).thenReturn(List.of());

    assertThatThrownBy(() -> sut.findStudentsByNamePrefix(prefix))
        .isInstanceOf(StudentNotFoundException.class)
        .hasMessage("該当する受講生が見つかりませんでした。");

    verify(repository).findStudentsByNamePrefix(prefix);
    verify(converter, never()).convertStudentDetails(any(), any(), any());
  }

  @Test
  void 指定された年齢範囲に該当する受講生が存在する場合StudentDetailリストを返すこと() {
    int minAge = 20;
    int maxAge = 30;

    List<Student> students = List.of(
        new Student("1", "相川かずき", "アイカワカズキ", "カズ", "kazu@example.com", "東京", 25,
            "男性", "", false));
    List<StudentCourse> courses = List.of(new StudentCourse(1, "1", "Java", null, null));
    List<StudentApplicationStatus> statuses = List.of(new StudentApplicationStatus(1, 1, "仮申込"));
    List<StudentDetail> expectedDetails = List.of(mock(StudentDetail.class));

    when(repository.findStudentsByAgeRange(minAge, maxAge)).thenReturn(students);
    when(repository.searchStudentCourseList()).thenReturn(courses);
    when(repository.searchStudentApplicationStatus()).thenReturn(statuses);
    when(converter.convertStudentDetails(students, courses, statuses)).thenReturn(expectedDetails);

    List<StudentDetail> result = sut.findStudentsByAgeRange(minAge, maxAge);

    assertThat(result).isEqualTo(expectedDetails);
    verify(repository).findStudentsByAgeRange(minAge, maxAge);
    verify(converter).convertStudentDetails(students, courses, statuses);
  }

  @Test
  void 指定された年齢範囲に該当する受講生が存在しない場合例外を発生させること() {
    int minAge = 50;
    int maxAge = 60;
    when(repository.findStudentsByAgeRange(minAge, maxAge)).thenReturn(List.of());

    assertThatThrownBy(() -> sut.findStudentsByAgeRange(minAge, maxAge))
        .isInstanceOf(StudentNotFoundException.class)
        .hasMessage("指定された年齢範囲に該当する受講生が見つかりませんでした。");

    verify(repository).findStudentsByAgeRange(minAge, maxAge);
    verify(converter, never()).convertStudentDetails(any(), any(), any());
  }

  @Test
  void 性別で検索をして該当する受講生データがある場合受講生詳細が正しく返されること() {
    String gender = "男性";

    List<Student> mockStudents = List.of(getStudent());
    List<StudentCourse> mockCourses = getStudentCourses();
    List<StudentApplicationStatus> mockStatuses = getStudentApplicationStatuses();
    List<StudentDetail> expectedDetails = List.of(
        new StudentDetail(getStudent(), mockCourses, mockStatuses));

    when(repository.findStudentsByGender(gender)).thenReturn(mockStudents);
    when(repository.searchStudentCourseList()).thenReturn(mockCourses);
    when(repository.searchStudentApplicationStatus()).thenReturn(mockStatuses);
    when(converter.convertStudentDetails(mockStudents, mockCourses, mockStatuses)).thenReturn(
        expectedDetails);

    List<StudentDetail> actualDetails = sut.findStudentsByGender(gender);

    assertEquals(expectedDetails, actualDetails);

    verify(repository).findStudentsByGender(gender);
    verify(repository).searchStudentCourseList();
    verify(repository).searchStudentApplicationStatus();
    verify(converter).convertStudentDetails(mockStudents, mockCourses, mockStatuses);
  }

  @Test
  void コース名で検索をして該当受講生データがある場合受講生詳細が返されること() {
    String courseName = "Javaコース";

    Student student1 = getStudent();
    Student student2 = new Student("2", "山本彩夏", "ヤマモトアヤカ", "アヤ",
        "aya@example.com", "東京", 25, "女性", "", false);

    StudentCourse course1 = new StudentCourse(99, "777", "Javaコース",
        LocalDateTime.of(2025, 4, 1, 0, 0, 0),
        LocalDateTime.of(2026, 3, 31, 0, 0, 0));
    StudentCourse course2 = new StudentCourse(100, "2", "AWSコース",
        LocalDateTime.of(2025, 5, 1, 0, 0, 0),
        LocalDateTime.of(2026, 4, 30, 0, 0, 0));

    List<Student> allStudents = List.of(student1, student2);
    List<StudentCourse> courseList = List.of(course1, course2);
    List<StudentApplicationStatus> statusList = List.of(
        new StudentApplicationStatus(1, 99, "受講中"),
        new StudentApplicationStatus(2, 100, "受講終了")
    );
    List<StudentDetail> expectedDetails = List.of(new StudentDetail());

    when(repository.search()).thenReturn(allStudents);
    when(repository.findStudentsByCourse(courseName)).thenReturn(courseList);
    when(repository.searchStudentApplicationStatus()).thenReturn(statusList);
    when(converter.convertStudentDetails(anyList(), eq(courseList), eq(statusList)))
        .thenReturn(expectedDetails);

    List<StudentDetail> result = sut.findStudentsByCourse(courseName);

    assertEquals(expectedDetails, result);
    verify(repository).search();
    verify(repository).findStudentsByCourse(courseName);
    verify(repository).searchStudentApplicationStatus();
    verify(converter).convertStudentDetails(anyList(), eq(courseList), eq(statusList));
  }

  @Test
  void 存在しないコース名が指定されたときに例外が発生すること() {
    String courseName = "機械学習コース"; // 不正なコース名

    StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
      sut.findStudentsByCourse(courseName);
    });

    assertEquals("適切なコース名を入力してください。", exception.getMessage());
    verifyNoInteractions(repository);
    verifyNoInteractions(converter);
  }

  @Test
  void コース検索結果が空だった場合に例外が発生すること() {
    String courseName = "AWSコース";

    when(repository.search()).thenReturn(List.of(getStudent()));
    when(repository.findStudentsByCourse(courseName)).thenReturn(Collections.emptyList());

    StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
      sut.findStudentsByCourse(courseName);
    });

    assertEquals("指定されたコースに所属する受講生が見つかりません。", exception.getMessage());
    verify(repository).findStudentsByCourse(courseName);
    verifyNoMoreInteractions(repository);
    verifyNoInteractions(converter);
  }

  @Test
  void リポジトリからコース情報がnullで返された場合に例外が発生すること() {
    String courseName = "英会話コース";

    when(repository.findStudentsByCourse(courseName)).thenReturn(null);

    StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
      sut.findStudentsByCourse(courseName);
    });

    assertEquals("指定されたコースに所属する受講生が見つかりません。", exception.getMessage());
    verify(repository).findStudentsByCourse(courseName);
    verify(repository).search();
    verifyNoMoreInteractions(repository);
    verifyNoInteractions(converter);
  }

  @Test
  void 該当する受講開始日のコースと受講生が存在する場合受講生詳細を返すこと() {
    LocalDateTime from = LocalDateTime.of(2025, 4, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2025, 9, 30, 23, 59);

    StudentCourse course1 = new StudentCourse(1, "1", "Javaコース", from.plusDays(1), null);
    StudentCourse course2 = new StudentCourse(2, "2", "AWSコース", from.plusDays(2), null);
    List<StudentCourse> filteredCourses = List.of(course1, course2);

    Student student1 = new Student("1", "田中太郎", "タナカタロウ", "タロ",
        "taro@example.com", "東京", 25, "男性", "", false);
    Student student2 = new Student("2", "山本彩夏", "ヤマモトアヤカ", "アヤ",
        "aya@example.com", "東京", 25, "女性", "", false);
    List<Student> students = List.of(student1, student2);

    List<StudentApplicationStatus> statuses = List.of(
        new StudentApplicationStatus(1, 1, "受講中"),
        new StudentApplicationStatus(2, 2, "受講終了")
    );

    List<StudentDetail> expectedDetails = List.of(new StudentDetail());

    when(repository.findCoursesByStartDateRange(from, to)).thenReturn(filteredCourses);
    when(repository.findStudentsByIds(List.of("1", "2"))).thenReturn(students);
    when(repository.searchStudentApplicationStatus()).thenReturn(statuses);
    when(converter.convertStudentDetails(students, filteredCourses, statuses)).thenReturn(
        expectedDetails);

    List<StudentDetail> result = sut.findStudentsByCourseStartDateRange(from, to);

    assertEquals(expectedDetails, result);
    verify(repository).findCoursesByStartDateRange(from, to);
    verify(repository).findStudentsByIds(List.of("1", "2"));
    verify(repository).searchStudentApplicationStatus();
    verify(converter).convertStudentDetails(students, filteredCourses, statuses);
  }

  @Test
  void 開始日範囲に該当するコースが空のとき例外が発生すること() {
    LocalDateTime from = LocalDateTime.of(2025, 1, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2025, 1, 31, 23, 59);

    when(repository.findCoursesByStartDateRange(from, to)).thenReturn(Collections.emptyList());

    StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
      sut.findStudentsByCourseStartDateRange(from, to);
    });

    assertEquals("指定された受講開始日範囲に該当する受講生は見つかりませんでした。",
        exception.getMessage());
    verify(repository).findCoursesByStartDateRange(from, to);
    verifyNoMoreInteractions(repository);
    verifyNoInteractions(converter);
  }

  @Test
  void コース一覧がnullの場合例外が発生すること() {
    LocalDateTime from = LocalDateTime.of(2025, 2, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2025, 2, 28, 23, 59);

    when(repository.findCoursesByStartDateRange(from, to)).thenReturn(null);

    StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
      sut.findStudentsByCourseStartDateRange(from, to);
    });

    assertEquals("指定された受講開始日範囲に該当する受講生は見つかりませんでした。",
        exception.getMessage());
    verify(repository).findCoursesByStartDateRange(from, to);
    verifyNoMoreInteractions(repository);
    verifyNoInteractions(converter);
  }

  @Test
  void 存在する申込状況で検索をかけて該当データがある場合その受講生詳細を返すこと() {
    String status = "受講中";

    Student student1 = new Student("1", "田中太郎", "タナカタロウ", "タロ",
        "taro@example.com", "東京", 25, "男性", "", false);
    Student student2 = new Student("2", "山本彩夏", "ヤマモトアヤカ", "アヤ",
        "aya@example.com", "東京", 25, "女性", "", false);
    List<Student> allStudents = List.of(student1, student2);

    StudentCourse course1 = new StudentCourse(101, "1", "Javaコース", null, null);
    StudentCourse course2 = new StudentCourse(102, "2", "AWSコース", null, null);
    List<StudentCourse> allCourses = List.of(course1, course2);

    StudentApplicationStatus status1 = new StudentApplicationStatus(1, 101, status);
    StudentApplicationStatus status2 = new StudentApplicationStatus(2, 102, status);
    List<StudentApplicationStatus> statusList = List.of(status1, status2);

    List<StudentDetail> expectedDetails = List.of(new StudentDetail());

    when(repository.search()).thenReturn(allStudents);
    when(repository.searchStudentCourseList()).thenReturn(allCourses);
    when(repository.findStudentsByStatus(status)).thenReturn(statusList);
    when(converter.convertStudentDetails(anyList(), anyList(), anyList()))
        .thenReturn(expectedDetails);

    List<StudentDetail> result = sut.findStudentsByStatus(status);

    assertEquals(expectedDetails, result);
    verify(repository).search();
    verify(repository).searchStudentCourseList();
    verify(repository).findStudentsByStatus(status);
    verify(converter).convertStudentDetails(anyList(), anyList(), anyList());
  }

  @Test
  void 仮申込_本申込_受講中_受講終了以外の申込状況で検索をかけた場合例外を発生させること() {
    String invalidStatus = "退会";

    StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
      sut.findStudentsByStatus(invalidStatus);
    });

    assertEquals("適切な申し込み状況を入力してください。", exception.getMessage());
    verifyNoInteractions(repository);
    verifyNoInteractions(converter);
  }

}
