package com.example.judge.callback;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;
import lombok.Data;

import java.io.Closeable;
import java.io.IOException;

@Data
public class StatisticsCallback implements ResultCallback<Statistics> {

    private Long maxMemory = 0L;

    @Override
    public void onStart(Closeable closeable) {

    }

    @Override
    public void onNext(Statistics statistics) {
        Long usage = statistics.getMemoryStats().getMaxUsage();
        if(usage != null) {
            maxMemory = Math.max(maxMemory, usage);
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void close() throws IOException {

    }
}
