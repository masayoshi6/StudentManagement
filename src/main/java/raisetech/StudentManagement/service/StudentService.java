package raisetech.StudentManagement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.StudentManagement.controller.converter.StudentConverter;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentApplicationStatus;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.repository.StudentRepository;

/**
 * 受講生情報を取り扱うサービスです。 受講生の検索や登録・更新処理を行います。
 */
@Service
public class StudentService {

  private StudentRepository repository;
  private StudentConverter converter;

  @Autowired
  public StudentService(StudentRepository repository, StudentConverter converter) {
    this.repository = repository;
    this.converter = converter;
  }

  /**
   * 受講生詳細の一覧検索です。 全件検索を行うので、条件指定は行いません。
   *
   * @return 受講生詳細一覧（全件）
   */
  public List<StudentDetail> searchStudentList() {
    List<Student> studentList = repository.search();
    List<StudentCourse> studentCourseList = repository.searchStudentCourseList();
    List<StudentApplicationStatus> studentApplicationStatusList = repository.searchStudentApplicationStatus();
    return converter.convertStudentDetails(studentList, studentCourseList,
        studentApplicationStatusList);
  }

  /**
   * 受講生詳細検索です。 IDに紐づく受講生情報を取得した後、その受講生に紐づく受講生コース情報、およびコースの申込状況を取得して設定します。
   *
   * @param id 受講生ID
   * @return 受講生詳細
   */
  public StudentDetail searchStudent(String id) {
    Student student = repository.searchStudent(id);
    List<StudentCourse> studentCourse = repository.searchStudentCourse(student.getId());
    List<StudentApplicationStatus> statusList = new ArrayList<>();
    studentCourse.stream().map(course -> Integer.valueOf(course.getId())).forEach(courseId -> {
      List<StudentApplicationStatus> studentApplicationStatusList = repository.searchStudentApplicationStatus();
      studentApplicationStatusList.stream()
          .filter(status -> courseId.equals(status.getStudentCourseId())).forEach(statusList::add);
    });
    return new StudentDetail(student, studentCourse, statusList);
  }

  /**
   * 受講生詳細の登録を行います。受講生と受講生コース情報およびコースの申込状況を個別に登録し、 受講生コース情報には受講生情報を紐づける値とコース開始日、コース終了日を設定します。
   * コースの申込状況については、初期段階では「仮申込」として登録を行います。
   *
   * @param studentDetail 受講生詳細
   * @return 登録情報を付与した受講生詳細
   */
  @Transactional
  public StudentDetail registerStudent(StudentDetail studentDetail) {
    Student student = studentDetail.getStudent();
    repository.registerStudent(student);

    List<StudentApplicationStatus> statusList = new ArrayList<>();

    studentDetail.getStudentCourseList().forEach(studentCourse -> {
      initStudentsCourse(studentCourse, student.getId());
      repository.registerStudentCourse(studentCourse);

      StudentApplicationStatus status = new StudentApplicationStatus();
      status.setStudentCourseId(studentCourse.getId());
      status.setStatus("仮申込");

      repository.registerApplicationStatus(status);

      // ← 登録したstatusをリストに追加
      statusList.add(status);
    });

    // ← 最後にセットする
    studentDetail.setStudentApplicationStatus(statusList);

    return studentDetail;
  }

  /*@Transactional
  public StudentDetail registerStudent(StudentDetail studentDetail) {
    Student student = studentDetail.getStudent();

    // TODO 受講生情報を登録
    repository.registerStudent(student);

    // TODO コース情報と申込ステータスを登録
    studentDetail.getStudentCourseList().forEach(studentCourse -> {
      // 初期情報（受講生ID、開始日・終了日）を設定
      initStudentsCourse(studentCourse, student.getId());

      // TODO コース登録
      repository.registerStudentCourse(studentCourse);

      // TODO 申込ステータスを作成して「仮申込」に設定して登録
      /*studentDetail.getStudentApplicationStatus().forEach(studentApplicationStatus -> {
        studentApplicationStatus.setStudentCourseId(studentCourse.getId());
        studentApplicationStatus.setStatus("仮申込");

        repository.registerApplicationStatus(studentApplicationStatus);
      });*/
  //最初の回答
     /* StudentApplicationStatus status = new StudentApplicationStatus();
      status.setStudentCourseId(studentCourse.getId());
      status.setStatus("仮申込");
      repository.registerApplicationStatus(status);
    });
    return studentDetail;
  }*/

  /**
   * 受講生コース情報を登録する際の初期情報を設定する。
   *
   * @param studentCourse 受講生コース情報
   * @param id            受講生ID
   */
  void initStudentsCourse(StudentCourse studentCourse, String id) {
    LocalDateTime now = LocalDateTime.now();

    studentCourse.setStudentId(id);
    studentCourse.setCourseStartAt(now);
    studentCourse.setCourseEndAt(now.plusYears(1));
  }

  /**
   * 受講生詳細の更新を行います。　受講生と受講生コース情報およびコースの申し込み状況の３点のそれぞれを更新します。
   *
   * @param studentDetail 受講生詳細
   */
  @Transactional
  public void updateStudent(StudentDetail studentDetail) {
    // TODO 受講生情報を更新
    repository.updateStudent(studentDetail.getStudent());

    // TODO コース情報を更新
    studentDetail.getStudentCourseList()
        .forEach(studentCourse -> repository.updateStudentCourse(studentCourse));

    // TODO 申込ステータス情報を更新
    if (studentDetail.getStudentApplicationStatus() != null) {
      studentDetail.getStudentApplicationStatus()
          .forEach(status -> repository.updateApplicationStatus(status));
    }
  }
}

