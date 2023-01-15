package com.moon.exchange.common.uuid;

public class OurUuid {

    private static final OurUuid ourInstance = new OurUuid();

    public static OurUuid getInstance() {
        return ourInstance;
    }

    private OurUuid() {
    }

    public void init(long centerId, long workerId) {
        idWorker = new SnowflakeIdWorker(workerId, centerId);
    }

    private SnowflakeIdWorker idWorker;

    public long getUUID() {
        return idWorker.nextId();
    }


}
