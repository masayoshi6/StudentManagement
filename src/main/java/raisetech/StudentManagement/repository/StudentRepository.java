package raisetech.StudentManagement.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

  /**
   * 名前が「prefix」から始まる受講生リストを取得します。
   *
   * @param prefix 受講生のカナ名の先頭１文字目
   * @return 指定した文字から始まる受講生一覧
   */
  List<Student> findStudentsByNamePrefix(@Param("prefix") String prefix);

  /**
   * 引数で渡した「minAge」「maxAge」に対し、minAge以上maxAge以下の年齢の受講生を検索します。
   *
   * @param minAge 年齢検索の下限
   * @param maxAge 年齢検索の上限
   * @return minAge以上maxAge以下の年齢をもつ受講生一覧
   */
  List<Student> findStudentsByAgeRange(int minAge, int maxAge);

  /**
   * 受講生を性別で検索します。
   *
   * @param sex 受講生の性別
   * @return 該当する性別の受講生一覧
   */
  List<Student> findStudentsByGender(@Param("sex") String sex);

  /**
   * 受講生をコース名を用いて検索します。指定したコースに所属している受講生のコース情報のリストを返します。
   *
   * @param courseName 受講コース名
   * @return 指定したコース名に該当する受講生のコース情報のリスト
   */
  List<StudentCourse> findStudentsByCourse(@Param("courseName") String courseName);

  /**
   * 引数に指定する２つの日付の間に受講がスタートする受講生のコース情報のリストを返します。
   *
   * @param from 検索区間の始まりの日
   * @param to   検索区間の終わりの日
   * @return 該当区間の間に受講が開始される受講生コース情報のリスト
   */
  List<StudentCourse> findCoursesByStartDateRange(@Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  /**
   * 受講生コース情報のオブジェクトとそれに紐づく受講生オブジェクトを集めたリストを返します。
   *
   * @param ids 受講生コース情報のオブジェクト内の「studentId」のリスト
   * @return 引数の指定したidsに紐づいている受講生オブジェクトのリスト
   */
  List<Student> findStudentsByIds(@Param("ids") List<String> ids);

  /**
   * 申し込み状況（仮申込、本申込、受講中、受講終了のいずれか）で検索をし、 該当する受講生の申込状況オブジェクトのリストを返します。
   *
   * @param status 申し込み状況（仮申込、本申込、受講中、受講終了のいずれか）
   * @return 該当する受講生の申込状況オブジェクトのリスト
   */
  List<StudentApplicationStatus> findStudentsByStatus(@Param("status") String status);
}
