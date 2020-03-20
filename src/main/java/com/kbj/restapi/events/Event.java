package com.kbj.restapi.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.kbj.restapi.accounts.Account;
import com.kbj.restapi.accounts.AccountSerializer;
import lombok.*;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;


//@Data >> equalsAndHashCode를 구현해주지만 모든 파라미터를 참조하므로 무한참조 현상이 발생하므로 Entity에서는 사용하지 말것.
@Builder @AllArgsConstructor @NoArgsConstructor
@Getter @Setter @EqualsAndHashCode(of = "id")
@Entity
public class Event {

  @Id @GeneratedValue
  private Integer id;
  private String name;
  private String description;
  private LocalDateTime beginEnrollmentDateTime;
  private LocalDateTime closeEnrollmentDateTime;
  private LocalDateTime beginEventDateTime;
  private LocalDateTime endEventDateTime;
  private String location;
  private int basePrice;
  private int maxPrice;
  private int limitOfEnrollment;
  private boolean offline;
  private boolean free;
  @Enumerated(EnumType.STRING)
  private EventStatus eventStatus = EventStatus.DRAFT;
  @ManyToOne
  @JsonSerialize(using = AccountSerializer.class)
  private Account manager;

  public void update() {
    // Update free
    if (this.basePrice == 0 && this.maxPrice == 0) {
      this.free = true;
    } else {
      this.free = false;
    }

    if (StringUtils.isEmpty(this.location)) {
      this.offline = false;
    } else {
      this.offline = true;
    }
  }
}