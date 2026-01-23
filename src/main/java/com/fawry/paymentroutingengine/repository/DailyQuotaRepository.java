package com.fawry.paymentroutingengine.repository;

import com.fawry.paymentroutingengine.entity.DailyQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyQuotaRepository extends JpaRepository<DailyQuota, Integer> {

}
