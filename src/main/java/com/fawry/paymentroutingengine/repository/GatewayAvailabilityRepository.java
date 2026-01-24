package com.fawry.paymentroutingengine.repository;

import com.fawry.paymentroutingengine.entity.GatewayAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.fawry.paymentroutingengine.constant.DayType;

import java.util.List;
import java.util.Optional;

@Repository
public interface GatewayAvailabilityRepository extends JpaRepository<GatewayAvailability, Integer> {

    @Query("SELECT ga FROM GatewayAvailability ga " +
            "WHERE ga.gatewayId = :gatewayId " +
            "AND (ga.dayOfWeek = :dayOfWeek OR ga.dayOfWeek = 'ALL')")
    List<GatewayAvailability> findAvailabilityForDay(
            @Param("gatewayId") Long gatewayId,
            @Param("dayOfWeek") DayType dayOfWeek
    );

    void deleteByGatewayId(Long gatewayId);


    List<GatewayAvailability> findByGatewayId(Long gatewayId);
}