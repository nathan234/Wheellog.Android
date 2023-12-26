package com.cooper.wheellog.utils.inmotion;

import java.io.ByteArrayOutputStream;

import timber.log.Timber;

public class InmotionUnpackerV2 {

    enum UnpackerState {
        unknown,
        flagsearch,
        lensearch,
        collecting,
        done
    }


    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int oldc = 0;
    int len = 0;
    int flags = 0;
    UnpackerState state = UnpackerState.unknown;

    byte[] getBuffer() {
        return buffer.toByteArray();
    }

    boolean addChar(int c) {
        if (c != (byte)0xA5 || oldc == (byte)0xA5){

            switch (state) {

                case collecting:

                    buffer.write(c);
                    if (buffer.size() == len + 5) {
                        state = UnpackerState.done;
//                        updateStep = 0;
                        oldc = 0;
                        Timber.i("Len %d", len);
                        Timber.i("Step reset");
                        return true;
                    }
                    break;

                case lensearch:
                    buffer.write(c);
                    len = c & 0xff;
                    state = UnpackerState.collecting;
                    oldc = c;
                    break;

                case flagsearch:
                    buffer.write(c);
                    flags = c & 0xff;
                    state = UnpackerState.lensearch;
                    oldc = c;
                    break;

                default:
                    if (c == (byte) 0xAA && oldc == (byte) 0xAA) {
                        buffer = new ByteArrayOutputStream();
                        buffer.write(0xAA);
                        buffer.write(0xAA);
                        state = UnpackerState.flagsearch;
                    }
                    oldc = c;
            }

        } else {
            oldc = c;
        }
        return false;
    }

    void reset() {
        buffer = new ByteArrayOutputStream();
        oldc = 0;
        state = UnpackerState.unknown;

    }
}