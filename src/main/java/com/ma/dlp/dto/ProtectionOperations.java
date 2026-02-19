package com.ma.dlp.dto;
import lombok.Data;

@Data
public class ProtectionOperations {
    private Boolean read;
    private Boolean write;
    private Boolean delete;
    private Boolean rename;
    private Boolean create;


    public Boolean getCreate() {
        return create;
    }

    public void setCreate(Boolean create) {
        this.create = create;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }


    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getRename() {
        return rename;
    }

    public void setRename(Boolean rename) {
        this.rename = rename;
    }

    public Boolean getWrite() {
        return write;
    }

    public void setWrite(Boolean write) {
        this.write = write;
    }
}
