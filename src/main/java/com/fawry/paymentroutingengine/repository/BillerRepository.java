package com.fawry.paymentroutingengine.repository;

import com.fawry.paymentroutingengine.entity.Biller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillerRepository extends JpaRepository<Biller, Integer> {
}