package com.example.judge.callback;

import cn.hutool.core.util.StrUtil;
import com.example.common.core.enums.CodeRunStatus;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DockerStartResultCallBack extends ExecStartResultCallback {

    private CodeRunStatus codeRunStatus;

    private String errorMessage;

    private String message;

    @Override
    public void onNext(Frame frame) {
        StreamType streamType = frame.getStreamType();
        //错误输出 认定执行出错
        if(StreamType.STDERR.equals(streamType)) {
            if(StrUtil.isEmpty(errorMessage)) {
                errorMessage = new String(frame.getPayload());
            } else {
                errorMessage = errorMessage + new String(frame.getPayload());
            }
            codeRunStatus = CodeRunStatus.FAILED;
        } else {
            message = new String(frame.getPayload());
            codeRunStatus = CodeRunStatus.SUCCEED;
        }
        super.onNext(frame);
    }

}
