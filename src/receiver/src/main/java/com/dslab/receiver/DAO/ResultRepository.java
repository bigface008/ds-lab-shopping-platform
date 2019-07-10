package com.dslab.receiver.DAO;

import com.dslab.receiver.model.ResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<ResultEntity, String> {
}
