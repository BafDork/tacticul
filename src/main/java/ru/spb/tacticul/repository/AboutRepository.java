package ru.spb.tacticul.repository;

import org.springframework.stereotype.Repository;
import ru.spb.tacticul.model.About;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AboutRepository extends JpaRepository<About, Long> {}