package com.cooper.wheellog.utils.ninebot;

import java.io.ByteArrayOutputStream;

import kotlin.Pair;
import timber.log.Timber;

public class NinebotUnpacker {

    enum UnpackerState {
        unknown,
        started,
        collecting,
        done
    }

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int oldc = 0;
    int len = 0;
    UnpackerState state = UnpackerState.unknown;

    byte[] getBuffer() {
        return buffer.toByteArray();
    }

    Pair<Boolean, Integer> addChar(int c, int updateStep) {

        switch (state) {
            case collecting:
                buffer.write(c);
                if (buffer.size() == len + 6) {
                    state = UnpackerState.done;
                    updateStep = 0;
                    Timber.i("Len %d", len);
                    Timber.i("Step reset");
                    return new Pair<>(true, updateStep);
                }
                break;
            case started:
                buffer.write(c);
                len = c & 0xff;
                state = UnpackerState.collecting;
                break;
            default:
                if (c == (byte) 0xAA && oldc == (byte) 0x55) {
                    Timber.i("Find start");
                    buffer = new ByteArrayOutputStream();
                    buffer.write(0x55);
                    buffer.write(0xAA);
                    state = UnpackerState.started;
                }
                oldc = c;
        }
        return new Pair<>(false, updateStep);
    }

    void reset() {
        buffer = new ByteArrayOutputStream();
        oldc = 0;
        state = UnpackerState.unknown;

    }
}
