package com.beautiflow.ManagedCustomer.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.beautiflow.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "customer_group",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_group_designer_code",
        columnNames = {"designer_id","code"}
    ),
    indexes = @Index(name = "idx_designer", columnList = "designer_id")
)
public class CustomerGroup {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "designer_id")
  private User designer;

  @Column(nullable = false, length = 30)
  private String code;

  @Column(nullable = false)
  private boolean isSystem;

  public static CustomerGroup custom(User designer, String code) {
    return CustomerGroup.builder()
        .designer(designer)
        .code(code)
        .isSystem(false)
        .build();
  }
}
