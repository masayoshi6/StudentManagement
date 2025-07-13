package raisetech.StudentManagement.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentApplicationStatus;
import raisetech.StudentManagement.data.StudentCourse;

@MybatisTest
class StudentRepositoryTest {

  @Autowired
  private StudentRepository sut;

  @Test
  void 受講生の全件検索が行えること() {
    List<Student> actual = sut.search();
    assertThat(actual.size()).isEqualTo(5);
  }

  @Test
  void 受講生の登録が行えること() {
    Student student = new Student();
    student.setName("江並公史");
    student.setKanaName("エナミコウジ");
    student.setNickname("エナミ");
    student.setEmail("test@example.com");
    student.setArea("奈良県");
    student.setSex("男性");
    student.setRemark("");
    student.setDeleted(false);

    sut.registerStudent(student);

    List<Student> actual = sut.search();
    assertThat(actual.size()).isEqualTo(6);
  }

  @Test
  void 受講生の単体検索ができること() {
    Student student = getStudent();

    Student actual = sut.searchStudent("1");
    assertEquals(student.getId(), actual.getId());
    assertEquals(student.getName(), actual.getName());
    assertEquals(student.getKanaName(), actual.getKanaName());
    assertEquals(student.getNickname(), actual.getNickname());
    assertEquals(student.getEmail(), actual.getEmail());
    assertEquals(student.getArea(), actual.getArea());
    assertEquals(student.getAge(), actual.getAge());
    assertEquals(student.getSex(), actual.getSex());
  }


  @Test
  void 受講生のコース情報の全件検索が行えること() {
    List<StudentCourse> actual = sut.searchStudentCourseList();
    assertThat(actual.size()).isEqualTo(10);
  }

  @Test
  void 受講生IDに紐づく受講生コース情報の検索ができること() {
    Student student = new Student();
    student.setId("1");

    StudentCourse studentCourse1 = getStudentCourse(1, "Javaコース");

    StudentCourse studentCourse2 = getStudentCourse(2, "AWSコース");

    StudentCourse studentCourse3 = getStudentCourse(10, "Web制作コース");

    List<StudentCourse> actual = sut.searchStudentCourse("1");
    assertEquals(student.getId(), studentCourse1.getStudentId());
    assertEquals(student.getId(), studentCourse2.getStudentId());
    assertEquals(student.getId(), studentCourse3.getStudentId());
    List<String> courseNames = List.of(studentCourse1.getCourseName(),
        studentCourse2.getCourseName(), studentCourse3.getCourseName());
    assertEquals(courseNames.size(), actual.size());
    assertThat(actual.size()).isEqualTo(3);
  }


  @Test
  void 受講生のコース情報が登録できること() {
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setStudentId("1");
    studentCourse.setCourseName("数学コース");
    studentCourse.setCourseStartAt(LocalDateTime.of(2023, 4, 1, 9, 0, 0));
    studentCourse.setCourseEndAt(LocalDateTime.of(2023, 7, 1, 15, 0, 0));

    sut.registerStudentCourse(studentCourse);

    List<StudentCourse> actual = sut.searchStudentCourseList();
    assertThat(actual.size()).isEqualTo(11);
  }

  @Test
  void 受講生更新ができること() {

    //ためしにメールアドレス、住んでいる地域、年齢の３つの更新テストを実行してみます。
    Student student = new Student();
    student.setId("1");
    student.setName("山田太郎");
    student.setKanaName("ヤマダタロウ");
    student.setNickname("タロ");
    student.setEmail("taroyama@example.com");
    student.setArea("名古屋");
    student.setAge(26);
    student.setSex("男性");
    student.setRemark("");
    student.setDeleted(false);

    sut.updateStudent(student);

    Student actual = sut.searchStudent("1");
    assertEquals(student.getEmail(), actual.getEmail());
    assertEquals(student.getArea(), actual.getArea());
    assertEquals(student.getAge(), actual.getAge());
  }

  @Test
  void 受講生コース情報のコース名が更新できること() {
    Student student = getStudent();

    StudentCourse studentCourse = getStudentCourse(1, "数学コース");
    studentCourse.setCourseStartAt(LocalDateTime.of(2023, 4, 1, 9, 0, 0));
    studentCourse.setCourseEndAt(LocalDateTime.of(2023, 7, 1, 15, 0, 0));

    sut.updateStudentCourse(studentCourse);

    assertEquals(student.getId(), studentCourse.getStudentId());

    assertEquals("数学コース", studentCourse.getCourseName());

  }

  @Test
  void すべての受講生の申し込んでいるコースの申し込み状況が全件検索できること() {
    List<StudentApplicationStatus> actual = sut.searchStudentApplicationStatus();
    assertThat(actual.size()).isEqualTo(10);
  }

  @Test
  void 受講生のコース申込状況の登録ができること() {
    StudentApplicationStatus studentApplicationStatus = new StudentApplicationStatus();
    studentApplicationStatus.setStudentCourseId(8);
    studentApplicationStatus.setStatus("仮申込");

    sut.registerApplicationStatus(studentApplicationStatus);

    List<StudentApplicationStatus> actual = sut.searchStudentApplicationStatus();
    assertThat(actual.size()).isEqualTo(11);
  }

  @Test
  void 受講生コース申し込み状況が更新できること() {
    Student student = getStudent();
    StudentCourse studentCourse = getStudentCourse(1, "数学コース");
    StudentApplicationStatus studentApplicationStatus = new StudentApplicationStatus(1, 1,
        "受講中");

    sut.updateApplicationStatus(studentApplicationStatus);

    assertEquals(student.getId(), studentCourse.getStudentId());
    assertEquals(studentCourse.getId(),
        studentApplicationStatus.getStudentCourseId());

    assertEquals("受講中", studentApplicationStatus.getStatus());
  }

  //以下は、「異常系」のテストです！
  @Test
  void 存在しない受講生IDを用いて検索をかけたときnullが返ってくること() {
    String id = "9999"; // 存在しないIDと仮定

    Student result = sut.searchStudent(id);

    assertThat(result).isNull(); // null が返ることを確認
  }

  @Test
  void 存在しない受講生IDを用いてコース情報の検索をかけたとき空のリストが返ってくること() {
    String id = "9999"; // 存在しないIDと仮定

    List<StudentCourse> result = sut.searchStudentCourse(id);

    assertThat(result).isEmpty(); // リスト内が空であることを確認
  }

  @Test
  void 新規で受講生登録を行なう際名前が入力されていない場合は例外を発生させること() {
    Student student = new Student();
    student.setName(null); // nameは必須(@NotBlankにより) → nullに設定
    student.setKanaName("ヤマモト");
    student.setNickname("ヤマ");
    student.setEmail("yama@example.com");
    student.setArea("東京");
    student.setAge(25);
    student.setSex("男性");
    student.setRemark("");
    student.setDeleted(false);

    assertThrows(DataIntegrityViolationException.class, () -> {
      sut.registerStudent(student);
    });
  }

  @Test
  void コース情報の登録の際コース名が入力されていなかった場合は例外を発生させること() {
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setStudentId("3"); // 存在するID
    studentCourse.setCourseName(null); // ← course_name を null に
    studentCourse.setCourseStartAt(LocalDateTime.of(2024, 1, 1, 9, 0));
    studentCourse.setCourseEndAt(LocalDateTime.of(2024, 3, 1, 17, 0));

    assertThrows(DataIntegrityViolationException.class, () -> {
      sut.registerStudentCourse(studentCourse);
    });
  }

  @Test
  void コース申込状況の登録の際申し込み状況が入力されていなかった場合は例外を発生させること() {
    StudentApplicationStatus studentApplicationStatus = new StudentApplicationStatus();
    studentApplicationStatus.setStudentCourseId(3); // 存在するID
    studentApplicationStatus.setStatus(null); // ← status を null に

    assertThrows(DataIntegrityViolationException.class, () -> {
      sut.registerApplicationStatus(studentApplicationStatus);
    });
  }

  @Test
  void 受講生更新の際に存在しない受講生IDを指定した場合エラーにはならず更新されないことを確認() {
    Student student = new Student();
    student.setId("9999"); // 存在しないID
    student.setName("田中次郎");
    student.setKanaName("タナカジロウ");
    student.setNickname("ジロ");
    student.setEmail("jiro@example.com");
    student.setArea("大阪");
    student.setAge(28);
    student.setSex("男性");
    student.setRemark("特になし");
    student.setDeleted(false);

    sut.updateStudent(student); // 例外にはならない

    Student result = sut.searchStudent("9999");
    assertThat(result).isNull(); // データが存在しないまま
  }

  @Test
  void 受講生IDがnullの状態で更新をかけようとすると例外が発生すること() {
    // まず正しいデータで登録
    Student student = new Student(null, "鈴木三郎", "スズキサブロウ", "サブ", "saburo@example.com",
        "名古屋", 40, "男性", "", false);
    sut.registerStudent(student);

    // 登録された ID を使って更新準備
    student.setName(null); // name を null に

    assertThrows(DataIntegrityViolationException.class, () -> {
      sut.updateStudent(student);
    });
  }

  @Test
  void コース情報の更新をする際にコース名がnullなら例外を発生させること() {
    // まず有効なデータを登録
    Student student = new Student(null, "高橋五郎", "タカハシゴロウ", "ゴロー", "goro@example.com",
        "福岡", 33, "男性", "", false);
    sut.registerStudent(student);

    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setStudentId(student.getId());
    studentCourse.setCourseName("Javaコース");
    studentCourse.setCourseStartAt(LocalDateTime.of(2024, 5, 1, 9, 0));
    studentCourse.setCourseEndAt(LocalDateTime.of(2024, 8, 1, 17, 0));
    sut.registerStudentCourse(studentCourse);

    // nullを設定
    studentCourse.setCourseName(null);

    assertThrows(DataIntegrityViolationException.class, () -> {
      sut.updateStudentCourse(studentCourse);
    });
  }

  @Test
  void コース申し込み状況の更新をする際に申し込み状況がnullなら例外を発生させること() {
    // まず有効なデータを登録
    Student student = new Student(null, "高橋五郎", "タカハシゴロウ", "ゴロー", "goro@example.com",
        "福岡", 33, "男性", "", false);
    sut.registerStudent(student);

    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setStudentId(student.getId());
    studentCourse.setCourseName("Javaコース");
    studentCourse.setCourseStartAt(LocalDateTime.of(2024, 5, 1, 9, 0));
    studentCourse.setCourseEndAt(LocalDateTime.of(2024, 8, 1, 17, 0));
    sut.registerStudentCourse(studentCourse);

    StudentApplicationStatus studentApplicationStatus = new StudentApplicationStatus(8,
        studentCourse.getId(), "本申込");

    // nullを設定
    studentApplicationStatus.setStatus(null);

    assertThrows(DataIntegrityViolationException.class, () -> {
      sut.updateApplicationStatus(studentApplicationStatus);
    });
  }

  //以下は、メソッド抽出を行なったメソッドです！　
  //DTOクラスのインスタンス生成を行なっております！
  private Student getStudent() {
    Student student = new Student();
    student.setId("1");
    student.setName("山田太郎");
    student.setKanaName("ヤマダタロウ");
    student.setNickname("タロ");
    student.setEmail("taro@example.com");
    student.setArea("東京");
    student.setAge(25);
    student.setSex("男性");
    student.setRemark("");
    student.setDeleted(false);
    return student;
  }

  private StudentCourse getStudentCourse(Integer number, String courseName) {
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setId(number);
    studentCourse.setStudentId("1");
    studentCourse.setCourseName(courseName);
    return studentCourse;
  }

  @Test
  void カナ名がアで始まる受講生が正しく取得できること() {
    // 準備
    String prefix = "タ";

    // 実行
    List<Student> result = sut.findStudentsByNamePrefix(prefix);

    // 検証
    assertThat(result).isNotEmpty();
    assertThat(result.getFirst().getKanaName()).startsWith("タ");
  }

  @Test
  void 一致する受講生がいない場合空リストが返されること() {
    String prefix = "ン"; // 想定：存在しないカナ名の先頭文字

    List<Student> result = sut.findStudentsByNamePrefix(prefix);

    assertThat(result).isEmpty();
  }

  @Test
  void 年齢が20歳から29歳の受講生を取得できること() {
    int minAge = 20;
    int maxAge = 29;

    List<Student> result = sut.findStudentsByAgeRange(minAge, maxAge);

    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(student ->
        student.getAge() >= minAge && student.getAge() <= maxAge
    );
  }

  @Test
  void 該当する年齢の受講生がいない場合は空リストが返ること() {
    int minAge = 100;
    int maxAge = 110;

    List<Student> result = sut.findStudentsByAgeRange(minAge, maxAge);

    assertThat(result).isEmpty();
  }

  @Test
  void 性別が男性の受講生を取得できること() {
    List<Student> result = sut.findStudentsByGender("男性");

    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(student -> "男性".equals(student.getSex()));
  }

  @Test
  void 性別が女性の受講生を取得できること() {
    List<Student> result = sut.findStudentsByGender("女性");

    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(student -> "女性".equals(student.getSex()));
  }

  @Test
  void コース名がJavaコースの受講情報が取得できること() {
    List<StudentCourse> result = sut.findStudentsByCourse("Javaコース");

    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(course -> "Javaコース".equals(course.getCourseName()));
  }

  @Test
  void 該当するコース名が存在しない場合は空リストが返ること() {
    List<StudentCourse> result = sut.findStudentsByCourse("Pythonコース");

    assertThat(result).isEmpty();
  }

  @Test
  void 開始日が2023年4月1日から2025年9月30日の間にあるコースを取得できること() {
    LocalDateTime from = LocalDateTime.of(2023, 4, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2025, 9, 30, 23, 59);

    List<StudentCourse> result = sut.findCoursesByStartDateRange(from, to);

    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(course ->
        !course.getCourseStartAt().isBefore(from) &&
            !course.getCourseStartAt().isAfter(to)
    );
  }

  @Test
  void 該当する開始日がない場合は空リストが返る() {
    LocalDateTime from = LocalDateTime.of(2030, 1, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2030, 12, 31, 23, 59);

    List<StudentCourse> result = sut.findCoursesByStartDateRange(from, to);

    assertThat(result).isEmpty();
  }

  @Test
  void 指定した複数のIDに対応する受講生を取得できること() {
    List<String> ids = List.of("1", "2");

    List<Student> result = sut.findStudentsByIds(ids);

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Student::getId).containsExactlyInAnyOrder("1", "2");
  }

  @Test
  void 存在しない受講生IDを指定してfindStudentsByIdsメソッドを使用した場合は空のリストが返ること() {
    List<String> ids = List.of("999", "888");

    List<Student> result = sut.findStudentsByIds(ids);

    assertThat(result).isEmpty();
  }

  @Test
  void 指定した申込状況のレコードが取得できること() {
    List<StudentApplicationStatus> result = sut.findStudentsByStatus("仮申込");

    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(s -> s.getStatus().equals("仮申込"));
  }

  @Test
  void 存在しない申込状況を指定すると空リストが返ること() {
    List<StudentApplicationStatus> result = sut.findStudentsByStatus("存在しない申込状況");

    assertThat(result).isEmpty();
  }
}
