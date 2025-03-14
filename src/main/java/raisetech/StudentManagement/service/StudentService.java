package raisetech.StudentManagement.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentsCourses;
import raisetech.StudentManagement.repository.StudentRepository;

@Service
public class StudentService {

  private StudentRepository repository;


  @Autowired
  public StudentService(StudentRepository repository) {
    this.repository = repository;
  }

  public List<Student> searchStudentList() {
    return repository.search();

    // ↓課題２４の解答
    /*List<Student> students = repository.search();

    return students.stream()
        .filter(v -> v.getAge() >= 30 && v.getAge() < 40)
        .collect(Collectors.toList()); //左のtoListの部分は別にtoUnmodifiableListでもよい
    */
    // ↑ ここまでが課題２４の解答（その１）
  }

  public List<StudentsCourses> searchStudentsCourseList() {
    return repository.searchStudentsCourses();

    // ↓課題２４の解答
    /*List<StudentsCourses> courses = repository.searchStudentsCourses();

    return courses.stream()
        .filter(v -> (v.getCourseName()).equals("Javaコース"))
        .collect(Collectors.toUnmodifiableList()); //左のtoUnmodifiableListの部分は別にtoListでもよい
    */
    // ここまでが課題２４の解答（その２）
  }
}
