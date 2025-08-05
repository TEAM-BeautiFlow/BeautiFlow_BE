package com.beautiflow.user.domain;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_styles")
public class UserStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @Column(length = 100)
    private String description;

    @OneToMany(mappedBy = "userStyle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStyleImage> images = new ArrayList<>();


    private LocalDateTime createdAt;

}

