package raisetech.StudentManagement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.StudentManagement.controller.converter.StudentConverter;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentApplicationStatus;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.exception.StudentNotFoundException;
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
    studentCourse.stream().map(course -> course.getId()).forEach(courseId -> {
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
    // TODO 受講生情報を登録
    repository.registerStudent(student);

    List<StudentApplicationStatus> statusList = new ArrayList<>();

    // TODO コース情報と申込ステータスを登録
    studentDetail.getStudentCourseList().forEach(studentCourse -> {
      initStudentsCourse(studentCourse, student.getId());
      repository.registerStudentCourse(studentCourse);
      // TODO 申込ステータスを作成して「仮申込」に設定して登録
      StudentApplicationStatus status = new StudentApplicationStatus();
      status.setStudentCourseId(studentCourse.getId());
      status.setStatus("仮申込");

      repository.registerApplicationStatus(status);

      // 登録したstatusをリストに追加
      statusList.add(status);
    });

    // ← 最後にセットする
    studentDetail.setStudentApplicationStatus(statusList);

    return studentDetail;
  }

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

  /**
   * カナ名が引数の全角カタカナから始まる受講生を検索します。そして該当する受講生を集めたリストを返します。 該当する受講生が見つからなかった場合はその旨を伝えるメッセージを表示させます。
   *
   * @param prefix 受講生の名前の１文字目（この１文字は全角カタカナとします）
   * @return 該当する受講生詳細のリスト
   */
  public List<StudentDetail> findStudentsByNamePrefix(String prefix) {
    List<Student> studentsByNamePrefix = repository.findStudentsByNamePrefix(prefix);

    if (studentsByNamePrefix == null || studentsByNamePrefix.isEmpty()) {
      throw new StudentNotFoundException("該当する受講生が見つかりませんでした。");
    }

    List<StudentCourse> studentCourseList = repository.searchStudentCourseList();
    List<StudentApplicationStatus> studentApplicationStatusList = repository.searchStudentApplicationStatus();

    return converter.convertStudentDetails(
        studentsByNamePrefix,
        studentCourseList,
        studentApplicationStatusList
    );
  }

  /**
   * 引数で設定した２つの整数の間の年齢の受講生を検索します。引数に整数以外のものを設定したり、 minAge > maxAge となった場合は例外メッセージを返します。
   *
   * @param minAge 年齢検索の下限
   * @param maxAge 年齢検索の上限
   * @return minAge以上 maxAge以下の年齢の受講生のリスト
   */
  public List<StudentDetail> findStudentsByAgeRange(int minAge, int maxAge) {
    List<Student> students = repository.findStudentsByAgeRange(minAge, maxAge);

    if (students == null || students.isEmpty()) {
      throw new StudentNotFoundException(
          "指定された年齢範囲に該当する受講生が見つかりませんでした。");
    }

    List<StudentCourse> courses = repository.searchStudentCourseList();
    List<StudentApplicationStatus> statuses = repository.searchStudentApplicationStatus();

    return converter.convertStudentDetails(students, courses, statuses);
  }

  /**
   * 性別で受講生を検索するメソッドです。「男性」または「女性」の受講生一覧を取得します。
   *
   * @param sex 受講生の性別
   * @return 該当する性別の受講生のリスト
   */
  public List<StudentDetail> findStudentsByGender(String sex) {
    if (!Objects.equals(sex, "男性") && !Objects.equals(sex, "女性")) {
      throw new StudentNotFoundException("「男性」または「女性」と入力して検索をしてください。");
    }
    List<Student> students = repository.findStudentsByGender(sex);

    if (students == null || students.isEmpty()) {
      throw new StudentNotFoundException("指定された性別に該当する受講生が見つかりませんでした。");
    }

    List<StudentCourse> courses = repository.searchStudentCourseList();
    List<StudentApplicationStatus> statuses = repository.searchStudentApplicationStatus();

    return converter.convertStudentDetails(students, courses, statuses);
  }

  /**
   * コース名を指定し、そのコースに所属している受講生一覧を返すメソッドです。
   *
   * @param courseName 受講コース名
   * @return 指定したコースに所属している受講生一覧
   */
  public List<StudentDetail> findStudentsByCourse(String courseName) {
    if (!Objects.equals(courseName, "Javaコース") && !Objects.equals(courseName, "AWSコース")
        && !Objects.equals(courseName, "Pythonコース")
        && !Objects.equals(courseName, "英会話コース")) {
      throw new StudentNotFoundException("適切なコース名を入力してください。");
    }

    List<Student> studentList = repository.search();
    List<Student> students = new ArrayList<>();

    List<StudentCourse> courses = repository.findStudentsByCourse(courseName);
    if (courses == null || courses.isEmpty()) {
      throw new StudentNotFoundException("指定されたコースに所属する受講生が見つかりません。");
    }

    courses.forEach(studentCourse -> {
      studentList.stream().filter(student -> studentCourse.getStudentId().equals(student.getId()))
          .forEach(students::add);
    });

    List<StudentApplicationStatus> statuses = repository.searchStudentApplicationStatus();

    return converter.convertStudentDetails(students, courses, statuses);
  }

  /**
   * コースの受講開始日で受講生検索を行うメソッドです。 引数に指定する２つの日付の間に受講開始となる受講生詳細のリストを返します。
   *
   * @param from 検索区間の始まりの日
   * @param to   検索区間の終わりの日
   * @return 該当区間の間に受講が開始される受講生詳細のリスト
   */
  public List<StudentDetail> findStudentsByCourseStartDateRange(LocalDateTime from,
      LocalDateTime to) {
    List<StudentCourse> filteredCourses = repository.findCoursesByStartDateRange(from, to);

    if (filteredCourses == null || filteredCourses.isEmpty()) {
      throw new StudentNotFoundException(
          "指定された受講開始日範囲に該当する受講生は見つかりませんでした。");
    }

    // 学生IDを抽出して該当するStudentを検索
    List<String> studentIds = filteredCourses.stream()
        .map(StudentCourse::getStudentId)
        .distinct()//同じstudentIdが２回以上登場する可能性があり、その重複を防ぐため(同じものを２回以上リスト化する必要はないから)
        .toList();

    List<Student> students = repository.findStudentsByIds(studentIds);
    List<StudentApplicationStatus> statuses = repository.searchStudentApplicationStatus();

    return converter.convertStudentDetails(students, filteredCourses, statuses);
  }

  /**
   * 申込状況から受講生を検索するメソッドです。
   *
   * @param status 仮申込、本申込、受講中、受講終了のいずれか
   * @return 該当する受講生詳細のリスト
   */
  public List<StudentDetail> findStudentsByStatus(String status) {
    // ステータスチェック
    List<String> validStatuses = List.of("仮申込", "本申込", "受講中", "受講終了");
    if (!validStatuses.contains(status)) {
      throw new StudentNotFoundException("適切な申し込み状況を入力してください。");
    }

    // データ取得
    List<Student> allStudents = repository.search();
    Map<String, Student> studentMap = allStudents.stream()
        .collect(Collectors.toMap(Student::getId, Function.identity()));

    List<StudentCourse> allCourses = repository.searchStudentCourseList();
    List<StudentApplicationStatus> statusList = repository.findStudentsByStatus(status);

    // 関連コースと受講生の抽出
    Set<Integer> targetCourseIds = statusList.stream()
        .map(StudentApplicationStatus::getStudentCourseId)
        .collect(Collectors.toSet());

    List<StudentCourse> matchedCourses = allCourses.stream()
        .filter(course -> targetCourseIds.contains(course.getId()))
        .collect(Collectors.toList());

    Set<String> targetStudentIds = matchedCourses.stream()
        .map(StudentCourse::getStudentId)
        .collect(Collectors.toSet());

    List<Student> matchedStudents = targetStudentIds.stream()
        .map(studentMap::get)
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(Student::getId)) // ← ここで受講生ID順にソート（並べ替え）
        .collect(Collectors.toList());

    return converter.convertStudentDetails(matchedStudents, matchedCourses, statusList);
  }

}

