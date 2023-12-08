package pm2_5.studypartner.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.print.Doc;

@Entity
@Getter
@NoArgsConstructor
public class Keyword extends BaseEntity {   // 키워드 추출

    // 키워드 식별 아이디
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long id;

    // 문서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    // 키워드
    private String keyword;

    // 키워드 설명
    private String description;

    // 중요도 체크
    private boolean checked;

    public Keyword(Document document, String keyword, String description)
    {
        this.document = document;
        this.keyword = keyword;
        this.description = description;
        this.checked = false;
    }
}
