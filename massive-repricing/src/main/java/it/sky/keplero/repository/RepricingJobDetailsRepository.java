package it.sky.keplero.repository;

import it.sky.keplero.entity.RepricingJobDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepricingJobDetailsRepository extends JpaRepository<RepricingJobDetails, Integer> {
}
