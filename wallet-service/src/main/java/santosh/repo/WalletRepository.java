package santosh.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import santosh.entity.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
