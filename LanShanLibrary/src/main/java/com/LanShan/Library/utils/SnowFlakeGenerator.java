package com.LanShan.Library.utils;
//一个用雪花算法生成ID的工具类
//著作权归Chatgpt所有
public class SnowFlakeGenerator {

    private final long twepoch = 1420041600000L;

    private final long workerIdBits = 5L;

    private final long datacenterIdBits = 5L;

    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    private final long sequenceBits = 12L;

    private final long workerIdShift = sequenceBits;

    private final long datacenterIdShift = sequenceBits + workerIdBits;

    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long workerId;

    private long datacenterId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    public SnowFlakeGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
 
        long timestamp = timeGen();
        timestamp = generateId(timestamp);
        return ((timestamp - twepoch) << timestampLeftShift) //
                | (datacenterId << datacenterIdShift) //
                | (workerId << workerIdShift) //
                | sequence;
    }
 
    private long generateId(long timestamp){
        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if(timestamp < lastTimestamp){
            throw new RuntimeException(
                        String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
       }
        //如果是同一时间生成的，则进行毫秒内序列
        if(lastTimestamp == timestamp)
        {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if(sequence == 0)
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
        }
        else//时间戳改变，毫秒内序列重置
        {
            sequence = 0L;
        }
        //上次生成ID的时间截
        lastTimestamp = timestamp;
        return timestamp;
    }

    public synchronized String generateNextId() {
        long timestamp = timeGen();
        timestamp = generateId(timestamp);

        return String.valueOf(((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence);
    }


    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }


}