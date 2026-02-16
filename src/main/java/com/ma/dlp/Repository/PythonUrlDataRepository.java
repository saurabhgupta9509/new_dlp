package com.ma.dlp.Repository;

import com.ma.dlp.model.PythonUrlData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PythonUrlDataRepository extends JpaRepository<PythonUrlData, Long> {
    List<PythonUrlData> findByDeviceIdOrderByTimestampDesc(String deviceId);
    List<PythonUrlData> findTop10ByDeviceIdOrderByTimestampDesc(String deviceId);
}