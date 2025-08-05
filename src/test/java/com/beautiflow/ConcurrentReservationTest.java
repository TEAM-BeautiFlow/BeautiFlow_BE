package com.beautiflow;

import com.beautiflow.reservation.dto.request.DateTimeDesignerReq;
import com.beautiflow.reservation.dto.request.SelectedOptionReq;
import com.beautiflow.reservation.dto.request.TmpReservationReq;
import com.beautiflow.reservation.dto.request.TreatOptionReq;
import com.beautiflow.reservation.service.ReservationService;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ConcurrentReservationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationService reservationService;

    private static final int THREAD_COUNT = 10;

    @Test
    public void testConcurrentReservation() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        Long shopId = 1L;
        User user = getMockUser();

        TreatOptionReq treatOptionReq = getMockTreatOptionReq();
        DateTimeDesignerReq dateTimeDesignerReq = DateTimeDesignerReq.builder()
                .date(LocalDate.of(2025, 8, 13))
                .time(LocalTime.of(18, 0))
                .designerId(2L)
                .build();

        // 1) 임시 저장용 요청: saveFinalReservation=false
        TmpReservationReq tmpSaveReq = TmpReservationReq.builder()
                .tempSaveData(treatOptionReq)
                .dateTimeDesignerData(dateTimeDesignerReq)
                .saveFinalReservation(false)
                .build();

        // 2) 최종 저장용 요청: saveFinalReservation=true, 다른 필드는 null로 둠
        TmpReservationReq finalSaveReq = TmpReservationReq.builder()
                .saveFinalReservation(true)
                .build();

        java.util.concurrent.atomic.AtomicInteger finalSaveSuccessCount = new java.util.concurrent.atomic.AtomicInteger();

        // 1차: 임시 저장은 한 번만 하면 됨. (중복 저장 막는 용도)
        reservationService.processReservationFlow(shopId, user, tmpSaveReq);

        // 2차: 여러 스레드에서 최종 저장 시도
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.processReservationFlow(shopId, user, finalSaveReq);
                    System.out.println("Final save success: " + Thread.currentThread().getName());
                    finalSaveSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Final save failed: " + Thread.currentThread().getName() + " | " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        System.out.println("All threads finished");
        System.out.println("Total final save success count: " + finalSaveSuccessCount.get());
        org.junit.jupiter.api.Assertions.assertEquals(1, finalSaveSuccessCount.get(), "Exactly one final reservation should succeed.");
    }

    private User getMockUser() {
        return userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User with id=1 not found"));
    }

    private TreatOptionReq getMockTreatOptionReq() {
        SelectedOptionReq option = SelectedOptionReq.builder()
                .optionGroupId(1L)
                .optionItemId(2L)
                .build();

        return TreatOptionReq.builder()
                .treatmentId(1L)
                .selectedOptions(List.of(option))
                .build();
    }
}

