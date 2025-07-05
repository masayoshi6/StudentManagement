package raisetech.StudentManagement.repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentApplicationStatus;
import raisetech.StudentManagement.data.StudentCourse;

/**
 * 受講生テーブルと受講生コース情報テーブル、および、コース申し込み状況テーブルと紐づくRepositoryです。
 */
@Mapper
public interface StudentRepository {

  /**
   * 受講生の全件検索を行います。
   *
   * @return 受講生一覧（全件）
   */
  List<Student> search();

  /**
   * 受講生の検索を行います。
   *
   * @param id 受講生ID
   * @return 受講生
   */
  Student searchStudent(String id);

  /**
   * 受講生のコース情報の全件検索を行います。
   *
   * @return 受講生のコース情報（全件）
   */
  List<StudentCourse> searchStudentCourseList();

  /**
   * 受講生IDに紐づく受講生コース情報を検索します。
   *
   * @param studentId 受講生ID
   * @return 受講生IDに紐づく受講生コース情報
   */
  List<StudentCourse> searchStudentCourse(String studentId);

  /**
   * 受講生を新規登録します。IDに関しては自動採番を行う。
   *
   * @param student 受講生
   */
  //@Options(useGeneratedKeys = true, keyProperty = "id")
  //@OptionsはINSERTするときにだけ付ける！！
  void registerStudent(Student student);

  /**
   * 受講生コース情報を新規登録します。IDに関しては自動採番を行う。
   *
   * @param studentCourse 受講生コース情報
   */
  //@Options(useGeneratedKeys = true, keyProperty = "id")
  //@OptionsはINSERTするときにだけ付ける！！
  void registerStudentCourse(StudentCourse studentCourse);


  /**
   * 受講生を更新します。
   *
   * @param student 受講生
   */
  void updateStudent(Student student);

  /**
   * 受講生コース情報のコース名を更新します。
   *
   * @param studentCourse 受講生コース情報
   */
  void updateStudentCourse(StudentCourse studentCourse);

  /**
   * すべての受講生の申し込んでいるコースの申込状況の全件検索を行います。
   *
   * @return コースの申込状況一覧（全件）
   */
  List<StudentApplicationStatus> searchStudentApplicationStatus();

  /**
   * 受講生コース情報の申し込み状況を新規登録します。新規登録時は「仮申込」として登録されます。IDに関しては自動採番を行う。
   *
   * @param status 受講生コース情報の申込状況
   */
  void registerApplicationStatus(StudentApplicationStatus status);

  /**
   * 受講生コース情報の申込状況を更新します。
   *
   * @param status 受講生コース情報の申込状況
   */
  void updateApplicationStatus(StudentApplicationStatus status);

}
