package vn.dev.managementsystem.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty("id")
    @JsonIgnore
    private Integer id;

    @Column(name = "content")
    private String content;

    @Column(name = "link")
    private String link;

    @Column(name = "create_by")
    private String createBy;

    @Column(name = "create_date")
    private Date create_date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id")
//    @JsonIgnoreProperties(value = {"applications", "hibernateLazyInitializer"})
    private Topic topicReport;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "reportAttachment")
    private Set<Attachment> attachments = new HashSet<>();

    public void addRelationalAttachment(Attachment _attach) {
        attachments.add(_attach);
        _attach.setReportAttachment(this);
    }

    public void deleteRelationalAttachment(Attachment _attach) {
        attachments.remove(_attach);
        _attach.setReportAttachment(null);
    }
}
