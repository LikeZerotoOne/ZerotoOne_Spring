package pm2_5.studypartner.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Context extends BaseEntity {   // 문단 요약

    // 문단 식별 아이디
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "context_id")
    private Long id;

    // 문서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    // 문단 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    // 문단 요약
    @Column(columnDefinition = "TEXT")
    private String summary;

    // 중요도 체크
    private boolean checked;

    public Context(Document document, String content, String summary)
    {
        this.document = document;
        this.content = content;
        this.summary = summary;
        this.checked = false;
    }
}
