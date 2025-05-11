package raisetech.StudentManagement.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "受講生コース情報")
@Getter
@Setter
//@NoArgsConstructor
//@AllArgsConstructor
public class StudentCourse {

  //@NotBlank
  @Schema(description = "コースID", example = "5")
  @Pattern(regexp = "^\\d+$")
  private String id;

  //@NotBlank
  @Schema(description = "受講生ID", example = "7")
  @Pattern(regexp = "^\\d+$")
  private String studentId;

  @Schema(description = "受講コース名", example = "Javaコース")
  @NotBlank
  private String courseName;

  @Schema(description = "受講開始日", example = "2025.04.01")
  private LocalDateTime courseStartAt;

  @Schema(description = "サポート終了日", example = "2026.03.31")
  private LocalDateTime courseEndAt;


  /*@Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StudentCourse that = (StudentCourse) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(studentId, that.studentId) &&
        Objects.equals(courseName, that.courseName) &&
        Objects.equals(courseStartAt, that.courseStartAt) &&
        Objects.equals(courseEndAt, that.courseEndAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, studentId, courseName, courseStartAt, courseEndAt);
  }*/
}
