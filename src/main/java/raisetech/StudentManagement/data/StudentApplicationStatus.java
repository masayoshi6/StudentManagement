package raisetech.StudentManagement.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "受講生コース申し込み状況")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StudentApplicationStatus {

  @Schema(description = "ステータスID", example = "1")
  private Integer id;

  @Schema(description = "受講生コースID", example = "10")
  private Integer studentCourseId;

  @Schema(description = "申込状況", example = "仮申込")
  @NotBlank
  private String status;

}
