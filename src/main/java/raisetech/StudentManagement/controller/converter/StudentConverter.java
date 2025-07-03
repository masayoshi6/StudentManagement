package raisetech.StudentManagement.controller.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentApplicationStatus;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;

/**
 * 受講生詳細を受講生や受講生コース情報やコース申込状況詳細、もしくはその逆の変換を行うコンバーターです。
 */
@Component
public class StudentConverter {

  /**
   * 受講生に紐づく受講生コース情報をマッピングする。 受講生コース情報は受講生に対して複数存在するのでループを回して受講生詳細情報を組み立てる。
   * また、その受講生コース情報と申し込み状況もマッピングをし、最終的には受講生情報、その受講生が申し込みを行なったコース情報、
   * そのコースの申込状況の３点の情報をセットにした受講生詳細のオブジェクトのリストを返します。
   *
   * @param studentList                  受講生一覧
   * @param studentCourseList            受講生コース情報のリスト
   * @param studentApplicationStatusList 受講生コース情報の申込状況のリスト
   * @return 受講生詳細情報のリスト
   */
  public List<StudentDetail> convertStudentDetails(List<Student> studentList,
      List<StudentCourse> studentCourseList,
      List<StudentApplicationStatus> studentApplicationStatusList) {
    List<StudentDetail> studentDetails = new ArrayList<>();

    for (Student student : studentList) {
      StudentDetail studentDetail = new StudentDetail();
      studentDetail.setStudent(student);

      // この受講生のコースを抽出
      List<StudentCourse> matchedCourses = studentCourseList.stream()
          .filter(course -> student.getId().equals(course.getStudentId()))
          .collect(Collectors.toList());
      studentDetail.setStudentCourseList(matchedCourses);

      // この受講生の全コースに対応するステータスを集める
      List<StudentApplicationStatus> matchedStatuses = studentApplicationStatusList.stream()
          .filter(status -> matchedCourses.stream()
              .anyMatch(
                  course -> Integer.parseInt(course.getId()) == status.getStudentCourseId()))
          .collect(Collectors.toList());
      studentDetail.setStudentApplicationStatus(matchedStatuses);

      studentDetails.add(studentDetail);
    }

    return studentDetails;
  }

}


