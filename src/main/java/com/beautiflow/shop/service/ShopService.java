package com.beautiflow.shop.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.shop.converter.ShopConverter;
import com.beautiflow.shop.dto.ShopDetailRes;
import com.beautiflow.reservation.dto.response.TreatmentDetailWithOptionResponse;
import com.beautiflow.reservation.dto.response.TreatmentResponse;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.reservation.repository.TreatmentRepository;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.treatment.domain.Treatment;
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

    public ShopDetailRes getShopDetail(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        return ShopConverter.toDto(shop);
    }

    public List<TreatmentResponse> getTreatmentsByShopAndCategory(Long shopId, String category) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        List<Treatment> treatments;

        if (category == null || category.isEmpty()) {
            treatments = treatmentRepository.findByShop(shop);
        } else {
            TreatmentCategory catEnum = Arrays.stream(TreatmentCategory.values())
                    .filter(c -> c.name().equalsIgnoreCase(category))
                    .findFirst()
                    .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.INVALID_TREATMENT_PARAMETER));
            treatments = treatmentRepository.findByShopAndCategory(shop, catEnum);
        }

        return treatments.stream()
                .map(TreatmentResponse::from)
                .collect(Collectors.toList());
    }

    public TreatmentResponse getTreatmentDetail(Long shopId, Long treatmentId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        Treatment treatment = treatmentRepository.findByShopAndId(shop, treatmentId)
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

        return TreatmentResponse.from(treatment);
    }
    public TreatmentDetailWithOptionResponse getTreatmentDetailWithOptions(Long shopId, Long treatmentId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        Treatment treatment = treatmentRepository.findWithOptionsByShopAndId(shop, treatmentId)
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

        return ShopConverter.toTreatmentDetailWithOptionResponse(treatment);
    }

}