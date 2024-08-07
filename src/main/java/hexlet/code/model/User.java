package hexlet.code.model;


import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import static jakarta.persistence.GenerationType.IDENTITY;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    private String firstName;

    private String lastName;

    @NotNull
    @Email
    private String email;

    @NotBlank
    private String passwordDigest;

    @CreatedDate
    private Timestamp createdAt;

    @LastModifiedDate
    private Timestamp updatedAt;

    @Override
    public String getPassword() {
        return passwordDigest;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<GrantedAuthority>();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
