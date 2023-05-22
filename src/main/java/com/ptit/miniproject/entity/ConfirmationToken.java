package com.ptit.miniproject.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "confirmation_token")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String token;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public ConfirmationToken(User user) {
        this.user = user;
        this.date = new Date();
        this.token = UUID.randomUUID().toString();
    }
}
