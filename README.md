# StudentManagement

## 概要

***
所属しているプログラミングスクール(RaiseTech)の課題として作成しております。

架空のプログラミングスクールの受講生の運営スタッフがプロフィールと受講コース情報を管理するためのAPIです。

このAPIは、以下の処理が可能です。

+ 受講生詳細の全件検索
+ 受講生詳細の個別検索（受講生ID指定）
+ 受講生詳細の新規登録
+ 受講生詳細の更新（論理削除を含む）

### 【GitHub】

https://github.com/masayoshi6/StudentManagement

詳細なAPI仕様は、以下のリンクからご確認できます。

[API仕様書.pdf](../../Desktop/API%E4%BB%95%E6%A7%98%E6%9B%B8.pdf)

## 詳細

---

### 開発環境

**使用技術**  
<img src="https://img.shields.io/badge/language-Java 21-007396.svg">
<img src="https://img.shields.io/badge/framework-springboot 3.4.3-6DB33F.svg?logo=springboot&logoColor=#000000">
<img src="https://img.shields.io/badge/-MySQL-4479A1.svg?logo=mysql&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-MyBatis-990000.svg">
<img src="https://img.shields.io/badge/-JUnit5-25A162.svg?logo=JUnit5&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-h2database-09476B.svg?logo=h2database&logoColor=FFFFFF">

**使用ツール**  
<img src="https://img.shields.io/badge/-IntelliJ IDEA-000000.svg?logo=intellijidea&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-Git-F05032.svg?logo=git&logoColor=F8A899">
<img src="https://img.shields.io/badge/-GitHub-181717.svg?logo=github&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-Postman-FF6C37.svg?logo=postman&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-OpenAPI-6BA539.svg?logo=openapiinitiative&logoColor=FFFFFF">

---

### E-R図(データモデル) Entity-Relation Diagram

受講生と受講生コースは1対多の関係を持ちます。受講生と、その受講生のすべてのコースの情報で受講生詳細(
studentDetail)を組み立てます。

```mermaid
erDiagram
    STUDENTS ||--|{ STUDENTS_COURSES: "has"
    STUDENTS {
        INT student_id PK
        VARCHAR fullname
        VARCHAR kana_name
        VARCHAR nickname
        VARCHAR email UK
        VARCHAR area
        VARCHAR telephone UK
        INT age
        VARCHAR sex
        VARCHAR remerk
        BOOL is_deleted
    }
    STUDENTS_COURSES {
        INT course_id PK
        VARCHAR course_name PK
        INT student_id FK "外部キー (STUDENTS.student_id)"
        DATE course_start_at
        DATE course_end_at
    }
```

---

### データ転送オブジェクト　Data Transfer Object

#### 背景

将来的な機能拡張を想定し、受講生ID(`studentId`)、受講生コースID(`courseId`)のため、バリューオブジェクト
`Id`を設けました。
現在はシンプルな整数値をIDとして設定していますが、UUIDやコース名のイニシャルを含む連番等への変更を検討しています。

この際、クライアントと安全に値を受け渡すことができるよう、フォームオブジェクトとレスポンスオブジェクトを設けました。

#### 役割

- フォームオブジェクトとレスポンスオブジェクトは、
  それぞれ自分自身のクラスにおいて、ドメインオブジェクトに変換するためのメソッド(`toDomain`,
  `fromDomain`)を持ちます。
- `toDomain`メソッド, `fromDomain`メソッドによって、`studentId`, `courseId`は`Integer`型から`Id`
  型へ、あるいはその反対方向に変換されます。

```mermaid
---
title: DTOとドメインオブジェクト
---
classDiagram
    note "アクセサメソッド等、lombok @Dataのメソッド等は割愛"

    namespace フォームオブジェクト {
        class StudentDetailForm {
            - StudentForm student
            - List~StudentCourseForm~ studentCourseList
        }
        class StudentForm {
            - Integer studentId;
            - String fullname;
            - String kanaName;
            - String nickname;
            - String email;
            - String area;
            - String telephone;
            - Integer age;
            - String sex;
            - String remark;
            - boolean isDeleted;
            ~ toDomain(StudentForm form) Student
        }
        class StudentCourseForm {
            - Integer courseId
            - String courseName
            - LocalDate courseEndAt
            ~ toDomain(StudentCourse form) StudentCourse
        }
    }

    namespace ドメインオブジェクト {
        class StudentDetail {
            - final Student student
            - final List~StudentCourse~ studentCourseList
        }

        class Student {
            - final Id studentId;
            - final String fullname;
            - final String kanaName;
            - final String nickname;
            - final String email;
            - final String area;
            - final String telephone;
            - final Integer age;
            - final String sex;
            - final String remark;
            - final boolean isDeleted;
        }

        class StudentCourse {
            - final Id courseId
            - final String courseName
            - final Id studentId
            - final LocalDate courseStartAt
            - final LocalDate courseEndAt
        }
        class Id {
            - Integer value
        }
    }
    Student *-- Id
    StudentCourse *-- Id

    namespace レスポンスオブジェクト {
        class StudentDetailResponse {
            - final StudentResponse student
            - final List~StudentCourseResponse~ studentCourseList
            - StudentDetailResponse(StudentDetail domain)
            + static fromDomain(StudentDetail domain) StudentDetailResponse
        }

        class StudentResponse {
            - final Integer studentId;
            - final String fullname;
            - final String kanaName;
            - final String nickname;
            - final String email;
            - final String area;
            - final String telephone;
            - final Integer age;
            - final String sex;
            - final String remark;
            - final boolean isDeleted;
            ~ StudentResponse(Student domain)
        }

        class StudentCourseResponse {
            - final Integer courseId
            - final String courseName
            - final LocalDate courseStartAt
            - final LocalDate courseEndAt
            ~ StudentCourseResponse(StudentCourse domain)
        }
    }

    StudentDetail "1" *.. "1..*" StudentCourse
    StudentDetail "1" *.. "1" Student
    StudentDetailForm "1" *.. "1..*" StudentCourseForm
    StudentDetailForm "1" *.. "1" StudentForm
    StudentDetailResponse "1" *.. "1..*" StudentCourseResponse
    StudentDetailResponse "1" *.. "1" StudentResponse
    StudentForm ..> Student: return
    StudentCourseForm ..> StudentCourse: convert
    StudentDetailResponse ..> StudentDetail: convert


```

#### 補足

- StudentCourseのstudentIdは、バッグエンド側でStudentのstudentIdから取得して設定するため、フォームオブジェクトではフィールドを持ちません。
- StudentCourseのcourseStartAtとcourseEndAtは、バッグエンド側で登録処理実行日とその6か月後の日付を取得して設定します。
    - courseStartAtは不変のため、常にクライアントから受け取らず、フォームオブジェクトはフィールドを持ちません。
    - ただし、courseEndAtは受講期間延長を想定して、更新処理時には値を受け取ることがあるため、フォームオブジェクト自体はフィールドを持ちます。
    - クライアントが値を確認できるよう、レスポンスオブジェクトはcourseStartAtとcourseEndAtのどちらのフィールドも持ちます。

---

### シーケンス図 Sequence Diagram

```mermaid
sequenceDiagram
    actor User
    participant API as API
    participant DB as Database
    Note over User, DB: 全件検索フロー
    User ->> API: GET /students (受講生詳細)
    API ->> DB: SELECT 受講生一覧
    API ->> DB: SELECT 受講生コース一覧
    DB -->> API: 受講生一覧を返す
    DB -->> API: 受講生コース一覧を返す
    API ->> API: 受講生詳細一覧をビルド
    API -->> User: 200 OK (受講生詳細一覧が返る)
    Note over User, DB: 個別検索フロー
    User ->> API: GET /students/{studentId}
    break studentIdの形式が正しくない場合
        API -->> User: 400 BadRequest
    end
    API ->> DB: SELECT 受講生<br>WHERE 受講生ID
    API ->> DB: SELECT 受講生コース<br>WHERE 受講生ID
    DB -->> API: 受講生を返す
    DB -->> API: 受講生コースを返す
    API ->> API: 受講生詳細をビルド
    break 受講生詳細が空の場合
        API -->> User: 404 NotFound:受講生IDが存在しません
    end
    API -->> User: 200 OK (受講生詳細詳細が返る)

```

```mermaid

sequenceDiagram
    actor User
    participant API as API
    participant DB as Database
    Note over User, DB: 受講生詳細の新規登録フロー
    User ->> API: POST /students
    break 受講生詳細の形式が正しくない場合
        API -->> User: 400 BadRequest
    end
    API ->> DB: INSERT 受講生
    API ->> DB: INSERT 受講生コース
    API -->> User: 200 OK (テーブルに登録された受講生詳細が返る)

```

```mermaid
sequenceDiagram
    actor User
    participant API as API
    participant DB as Database
    Note over User, DB: 受講生詳細の更新(論理削除)フロー
    User ->> API: PUT /students
    break 受講生詳細の形式が正しくない場合
        API -->> User: 400 BadRequest
    end
    API ->> DB: SELECT 受講生コースID一覧<br>WHERE 受講生ID
    DB -->> API: 受講生IDに紐づく<br>受講生コースID一覧を返す
    API ->> API: 更新したい受講生コースが<br>受講生と紐づいたものか判定
    break 受講生IDに紐づく<br>受講生コースID一覧が空の場合
        API -->> User: 404 NotFound:受講生IDが存在しません
    end
    break 受講生コースIDが受講生IDと紐づかない場合
        API -->> User: 404 NotFound:受講生コースIDが受講生IDと紐づきません
    end
    API ->> DB: UPDATE 受講生コース<br>WHERE 受講生ID
    API ->> DB: UPDATE 受講生<br>WHERE 受講生コースID
    API -->> User: 200 OK (テーブルで更新された受講生詳細が帰る)
```

---

### クラス図 Class Diagram

概観

```mermaid
---
title: Student Management System(REST API)
---
classDiagram
    class USER {
        <<Conmponent>>
    }
    class student_managementDB {
        <<Component>>
    }
    USER ..> StudentController: request
    direction TB
    StudentController ..> USER: response
    StudentController ..> StudentService: call
    StudentService ..> StudentRepository: call
    StudentRepository ..> student_management DB: query

    class StudentController {
        -StudentServise service
        +getActiveStudentDetailList() List~StudentDetail~
        +viewStudentDetail(int studentId) StudentDetail
        +registerStudentDetail(StudentDetail studentDetail) ResponseEntity~StudentDetail~
        +updateStudentDetail(StudentDetail studentDetail) ResponseEntity~StudentDetail~
    }
    class StudentService {
        -StudentRepository repository
        +searchActiveStudentDetailList() List~StudentDetail~
        +searchstudentDetail(Id studentId) StudentDetail
        +registerStudentDetail(StudentDetail studentDetail) StudentDetail
        +updateStudentDetail(StudentDetail studentDetail) StudentDetail
        ~ buildStudentDetail(int studentId) StudentDetail
        ~ isLinkedCourseIdWithStudentId(StudentCourse course) boolean
    }
    class StudentRepository {
        +searchActiveStudentList()
        +searchStudent(Id studentId) Student
        +searchCourses(Id studentId) List~StudentCourses~
        +searchStudentIdList() List~Id~
        +searchCourseIdListLinkedStudentId(Id studentId) List~Id~
        +registerStudent(Student student)
        +registerCourse(StudentCourse course)
        +updateStudent(Student student)
        +updateCourse(StudentCourse course)
    }
    namespace ExceptionHandling {
        class StudentExceptionHandler {
        }
    }

    note for StudentService "DTO⇔ドメインオブジェクトの変換を担う"
    StudentController ..> StudentExceptionHandler: throw exception
    USER <.. StudentExceptionHandler: error response


```

例外処理

```mermaid
---
title: Exception Handling
---
classDiagram
    class StudentExceptionHandler {
        +handleInvalidAccessException(InvalidAccessException ex) ResponseEntity~ErrorResponseBody~
        +handleInvalidIdException(InvalidIdException ex) ResponseEntity~ErrorResponseBody~
        +handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) ResponseEntity~ErrorResponseBody~
        +handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) ResponseEntity~ErrorResponseBody~
        +handleConstraintViolationException(ConstraintViolationException ex) ResponseEntity~ErrorResponseBody~
    }

    class ErrorResponseBody {
        - HttpStatus status
        - String message
        - List~Map~ String, String~~ error
    }

    class ErrorDetailBuilder {
        + buildErrorDetails(Exception ex) List~Map~ String, String~~
    }
    class error {
        <<Component>>
    }

    note for StudentExceptionHandler "catch exceptions"
    StudentExceptionHandler ..> ErrorDetailBuilder: uses
    ErrorDetailBuilder ..> error: returns
    StudentExceptionHandler ..> ErrorResponseBody: creates
    ErrorResponseBody *-- error: contains

```

---

## テスト

JUnitを用いて単体テストを実装しました。

#### テストを行ったクラス

- StudentController
- StudentService
- StudentRepository
- Id
    - equals, hashCode, toStringメソッドのテスト
    - コンストラクタのテスト
    - バリデーションのテスト
- StudentDetailForm：バリデーションのテスト
- StudentForm：toDomainメソッドのテスト
- StudentCourseForm：toDomainメソッドのテスト
- StudentDetailResponse：fromDomainメソッドのテスト

## 力を入れたところ

### DTO（Data Transfer Object）設計の導入による責務の分離

- クライアントから受け取る入力（フォーム）と、レスポンスとして返す出力を明確に分離するため、DTOを導入しました。

- これにより、サービス層以降では不完全なドメインオブジェクトが扱われることがなくなり、ビジネスロジックの安全性が向上しました。

## 今後の課題

- 受講生ID以外による検索機能の実装
    - 受講生コースに受講ステータス項目（申し込み中、受講中、受講終了 等）を追加し、ステータスごとのリストを取得できるようにする
    - フルネーム、ニックネーム等のフィールドに対する完全一致・部分一致検索を可能にする

- GitHub Actionsを用いたテストの自動化
- 認証認可機能の追加