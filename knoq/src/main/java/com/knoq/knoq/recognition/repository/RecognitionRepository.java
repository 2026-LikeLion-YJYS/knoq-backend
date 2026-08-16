package com.knoq.knoq.recognition.repository;

import com.knoq.knoq.recognition.entity.Recognition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecognitionRepository extends JpaRepository<Recognition, String> {
}