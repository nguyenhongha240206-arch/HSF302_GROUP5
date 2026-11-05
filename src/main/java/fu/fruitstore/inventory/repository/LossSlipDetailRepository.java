package fu.fruitstore.inventory.repository;

import fu.fruitstore.inventory.entity.LossSlipDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LossSlipDetailRepository extends JpaRepository<LossSlipDetail, Long> {
    List<LossSlipDetail> findByLossSlipId(Long lossSlipId);
}

