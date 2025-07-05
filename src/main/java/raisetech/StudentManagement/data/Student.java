package raisetech.StudentManagement.data;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "受講生")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Student {

  @Schema(description = "受講生ID", example = "1001")
  @Pattern(regexp = "^\\d+$", message = "数字のみ入力するようにしてください。")
  private String id;

  @Schema(description = "受講生名", example = "田中太郎")
  @NotBlank
  private String name;

  @Schema(description = "受講生フリガナ", example = "タナカタロウ")
  @NotBlank
  private String kanaName;

  @Schema(description = "受講生ニックネーム", example = "タロ")
  @NotBlank
  private String nickname;

  @Schema(description = "メールアドレス", example = "taro@example.com")
  @NotBlank
  @Email
  private String email;

  @Schema(description = "現在住んでいる場所", example = "東京")
  @NotBlank
  private String area;

  @Schema(description = "年齢", example = "35")
  private int age;

  @Schema(description = "性別", example = "男性")
  @NotBlank
  private String sex;

  @Schema(description = "備考欄", example = "現在療養中")
  private String remark;

  @Schema(description = "キャンセルフラグ", example = "false")
  private boolean isDeleted;
}
