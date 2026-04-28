package com.necom.service.promotion;

import com.necom.dto.promotion.PromotionRequest;
import com.necom.dto.promotion.PromotionResponse;
import com.necom.entity.product.Product;
import com.necom.entity.promotion.Promotion;
import com.necom.mapper.promotion.PromotionMapper;
import com.necom.repository.promotion.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private PromotionMapper promotionMapper;
    @InjectMocks
    private PromotionServiceImpl promotionService;

    @Test
    void saveShouldThrowWhenAnyProductHasOverlappingPromotion() {
        Instant startDate = Instant.parse("2026-01-01T00:00:00Z");
        Instant endDate = Instant.parse("2026-01-10T00:00:00Z");
        Product product = new Product();
        product.setId(101L);
        PromotionRequest request = new PromotionRequest();
        Promotion promotion = new Promotion()
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setProducts(Set.of(product));

        when(promotionMapper.requestToEntity(request)).thenReturn(promotion);
        when(promotionRepository.findByProductId(101L, startDate, endDate)).thenReturn(List.of(new Promotion()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> promotionService.save(request));

        assertEquals("Overlap promotion with product id: 101", exception.getMessage());
        verify(promotionRepository, never()).save(promotion);
    }

    @Test
    void checkCanCreatePromotionForProductShouldReturnTrueWhenNoOverlap() {
        Instant startDate = Instant.parse("2026-02-01T00:00:00Z");
        Instant endDate = Instant.parse("2026-02-10T00:00:00Z");
        when(promotionRepository.findByProductId(55L, startDate, endDate)).thenReturn(List.of());

        boolean canCreate = promotionService.checkCanCreatePromotionForProduct(55L, startDate, endDate);

        assertTrue(canCreate);
    }

    @Test
    void saveShouldReturnMappedResponseWhenPromotionIsValid() {
        Instant startDate = Instant.parse("2026-03-01T00:00:00Z");
        Instant endDate = Instant.parse("2026-03-10T00:00:00Z");
        Product product = new Product();
        product.setId(202L);
        Promotion promotion = new Promotion()
                .setName("Spring Promo")
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setProducts(Set.of(product));
        Promotion savedPromotion = new Promotion();
        savedPromotion.setId(999L);
        savedPromotion.setName("Spring Promo");
        PromotionResponse response = new PromotionResponse();
        response.setId(999L);

        PromotionRequest request = new PromotionRequest();
        when(promotionMapper.requestToEntity(request)).thenReturn(promotion);
        when(promotionRepository.findByProductId(202L, startDate, endDate)).thenReturn(List.of());
        when(promotionRepository.save(promotion)).thenReturn(savedPromotion);
        when(promotionMapper.entityToResponse(savedPromotion)).thenReturn(response);

        PromotionResponse actual = promotionService.save(request);

        assertEquals(999L, actual.getId());
    }
}
