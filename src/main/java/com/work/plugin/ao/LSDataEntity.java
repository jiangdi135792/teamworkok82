package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Table("LSDATA_ENTITY")
public interface LSDataEntity extends Entity {

    @StringLength(200)
    String getCompany();
    void setCompany(String company);

    @StringLength(50)
    String getVv();
    void setVv(String vv);

    @NotNull
    @StringLength(200)
    String getPp();
    void setPp(String pp);

    @StringLength(StringLength.UNLIMITED)
    String getKeykey();
    void setKeykey(String keykey);

    enum COLUMN {
        ID,COMPANY,PP,VV,KEYKEY;
        COLUMN() {}
    }
}
