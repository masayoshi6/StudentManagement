package raisetech.StudentManagement.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;

@Schema(description = "受講生詳細")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StudentDetail {

  //@Schema(description = "受講生", example = "田中太郎")
  @Valid
  private Student student;

  //@Schema(description = "一人の受講生が受講しているコース一覧", example = "[Javaコース, AWSコース]")
  @Valid
  private List<StudentCourse> studentCourseList;
}
