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
    student.setName("山田太郎");
    student.setKanaName("ヤマダタロウ");
    student.setNickname("タロ");
    student.setEmail("taro@example.com");
    student.setArea("東京");
    student.setAge(25);
    student.setSex("男性");
    student.setRemark("");
    student.setDeleted(false);

    StudentCourse studentCourse1 = new StudentCourse();
    studentCourse1.setId("1");
    studentCourse1.setStudentId("1");
    studentCourse1.setCourseName("Javaコース");
    studentCourse1.setCourseStartAt(LocalDateTime.of(2023, 4, 1, 9, 0, 0));
    studentCourse1.setCourseEndAt(LocalDateTime.of(2023, 7, 1, 15, 0, 0));

    StudentCourse studentCourse2 = new StudentCourse();
    studentCourse2.setId("2");
    studentCourse2.setStudentId("1");
    studentCourse2.setCourseName("AWSコース");
    studentCourse2.setCourseStartAt(LocalDateTime.of(2023, 5, 1, 10, 0, 0));
    studentCourse2.setCourseEndAt(LocalDateTime.of(2023, 8, 1, 16, 0, 0));

    StudentCourse studentCourse3 = new StudentCourse();
    studentCourse3.setId("3");
    studentCourse3.setStudentId("1");
    studentCourse3.setCourseName("Web制作コース");
    studentCourse3.setCourseStartAt(LocalDateTime.of(2024, 1, 1, 13, 0, 0));
    studentCourse3.setCourseEndAt(LocalDateTime.of(2024, 5, 1, 19, 0, 0));

    List<StudentCourse> actual = sut.searchStudentCourse("1");
    assertEquals(student.getId(), studentCourse1.getStudentId());
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

}