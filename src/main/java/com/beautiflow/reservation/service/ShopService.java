package com.beautiflow.reservation.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.reservation.converter.ShopConverter;
import com.beautiflow.reservation.dto.response.ShopDetailResponse;
import com.beautiflow.reservation.dto.response.ShopDetailResponse.BusinessHourDto;
import com.beautiflow.reservation.dto.response.ShopDetailResponse.NoticeDto;
import com.beautiflow.reservation.dto.response.ShopDetailResponse.TreatmentDto;
import com.beautiflow.reservation.dto.response.TreatmentResponse;
import com.beautiflow.reservation.repository.ShopRepository;
import com.beautiflow.reservation.repository.TreatmentRepository;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.treatment.domain.Treatment;
import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final TreatmentRepository treatmentRepository;

    public ShopDetailResponse getShopDetail(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ShopErrorCode.SHOP_NOT_FOUND.getMessage()));

        return ShopConverter.toDto(shop);
    }

    public List<TreatmentResponse> getTreatmentsByShopAndCategory(Long shopId, String category) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ShopErrorCode.SHOP_NOT_FOUND.getMessage()));

        List<Treatment> treatments;

        if (category == null || category.isEmpty()) {
            treatments = treatmentRepository.findByShop(shop);
        } else {
            TreatmentCategory catEnum = Arrays.stream(TreatmentCategory.values())
                    .filter(c -> c.name().equalsIgnoreCase(category))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(TreatmentErrorCode.INVALID_TREATMENT_PARAMETER.getMessage()));
            treatments = treatmentRepository.findByShopAndCategory(shop, catEnum);
        }

        return treatments.stream()
                .map(TreatmentResponse::from)
                .collect(Collectors.toList());
    }

    public TreatmentResponse getTreatmentDetail(Long shopId, Long treatmentId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException(ShopErrorCode.SHOP_NOT_FOUND.getMessage()));

        Treatment treatment = treatmentRepository.findByShopAndId(shop, treatmentId)
                .orElseThrow(() -> new EntityNotFoundException(TreatmentErrorCode.TREATMENT_NOT_FOUND.getMessage()));

        return TreatmentResponse.from(treatment);
    }
}
