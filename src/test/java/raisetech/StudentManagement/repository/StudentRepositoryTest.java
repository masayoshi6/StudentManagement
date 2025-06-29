package raisetech.StudentManagement.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import raisetech.StudentManagement.data.Student;
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

    StudentCourse studentCourse1 = getStudentCourse("1", "Javaコース");

    StudentCourse studentCourse2 = getStudentCourse("2", "AWSコース");

    StudentCourse studentCourse3 = getStudentCourse("10", "Web制作コース");

    List<StudentCourse> actual = sut.searchStudentCourse("1");
    assertEquals(student.getId(), studentCourse1.getStudentId());
    assertEquals(student.getId(), studentCourse2.getStudentId());
    assertEquals(student.getId(), studentCourse3.getStudentId());
    List<String> courseNames = List.of(studentCourse1.getCourseName(),
        studentCourse2.getCourseName(), studentCourse3.getCourseName());
    assertEquals(courseNames.size(), actual.size());
    assertThat(actual.size()).isEqualTo(3);
  }

  private StudentCourse getStudentCourse(String number, String courseName) {
    StudentCourse studentCourse1 = new StudentCourse();
    studentCourse1.setId(number);
    studentCourse1.setStudentId("1");
    studentCourse1.setCourseName(courseName);
    return studentCourse1;
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

    StudentCourse studentCourse = getStudentCourse("1", "数学コース");
    studentCourse.setCourseStartAt(LocalDateTime.of(2023, 4, 1, 9, 0, 0));
    studentCourse.setCourseEndAt(LocalDateTime.of(2023, 7, 1, 15, 0, 0));

    sut.updateStudentCourse(studentCourse);

    assertEquals(student.getId(), studentCourse.getStudentId());

    assertEquals(studentCourse.getCourseName(), "数学コース");

  }

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
}