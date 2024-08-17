package hexlet.code.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;

import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "labels")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Label implements BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    @Column(unique = true)
    @Size(min = 3, max = 1000)
    private String name;

    @CreatedDate
    private Timestamp createdAt;

    @ManyToMany(mappedBy = "labels")
    private Set<Task> tasks = new HashSet<>();

    public Label(String name) {
        this.name = name;
    }
}
