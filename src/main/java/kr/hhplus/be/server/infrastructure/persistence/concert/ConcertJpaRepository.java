package kr.hhplus.be.server.infrastructure.persistence.concert;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaRepository extends JpaRepository<ConcertEntity, Long> {
}
