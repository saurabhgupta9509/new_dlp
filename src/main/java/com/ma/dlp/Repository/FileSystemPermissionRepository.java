package com.ma.dlp.Repository;

import com.ma.dlp.model.FileSystemPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileSystemPermissionRepository extends JpaRepository<FileSystemPermission, Long> {

    @Query("SELECT p FROM FileSystemPermission p WHERE :filePath LIKE CONCAT(p.path, '%')")
    List<FileSystemPermission> findRelevantPermissions(@Param("filePath") String filePath);

    List<FileSystemPermission> findByPath(String path);

    @Query("SELECT p FROM FileSystemPermission p WHERE p.recursive = true AND :filePath LIKE CONCAT(p.path, '%')")
    List<FileSystemPermission> findRecursivePermissions(@Param("filePath") String filePath);
}