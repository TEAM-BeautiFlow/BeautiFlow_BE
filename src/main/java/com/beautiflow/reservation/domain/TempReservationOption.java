package com.beautiflow.reservation.domain;

import com.beautiflow.treatment.domain.OptionGroup;
import com.beautiflow.treatment.domain.OptionItem;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "temp_reservation_options")
public class TempReservationOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private TempReservation tempReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    private OptionGroup optionGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    private OptionItem optionItem;
}