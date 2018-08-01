package com.flphoto;

import java.io.File;

/**
 * Created by Administrator on 2018/7/31 0031.
 */

public interface FlResult {

    void flStart();

    void flSuccess(File file);

    void flError(Throwable e);
}
